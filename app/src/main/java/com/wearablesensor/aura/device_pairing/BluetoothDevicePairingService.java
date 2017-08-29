/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.device_pairing;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattCustomGSRTemperatureCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattHeartRateCharacteristicReader;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;


import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class BluetoothDevicePairingService extends DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();

    // Stops scanning after 20 seconds.
    private static final long SCAN_PERIOD = 20000;

    // Bluetooth scanning members
    private Handler mScanningHandler;
    private BleManager.DiscoveryListener mDiscoveryListener;

    //Bluetooth data streaming callbacks
    private BleDevice.ReadWriteListener mHeartRateReadWriteListener;
    private BleDevice.ReadWriteListener mCustomMAXREFDES73ReadWriteListener;

    private ConcurrentHashMap<String, BleDevice> mConnectedDevices; // hashmap storing the currently connected devices list

    private Activity mActivity;

    public BluetoothDevicePairingService(Context iContext){
        super(iContext);

        // callback used to handle standard Heart rate profile
        mHeartRateReadWriteListener = new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent e) {
                if(e.wasSuccess() && e.type() == Type.NOTIFICATION){
                    GattHeartRateCharacteristicReader lGattCharacteristicReader = new GattHeartRateCharacteristicReader();
                    lGattCharacteristicReader.read(e.characteristic());

                    Calendar c = Calendar.getInstance();
                    String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());

                    RRIntervalModel lRrIntervalModel = new RRIntervalModel(e.device().getMacAddress(), lCurrentTimestamp, lGattCharacteristicReader.getRrInterval());
                    Log.d(TAG, lRrIntervalModel.getTimestamp() + " " + lRrIntervalModel.getUuid() + " " + lRrIntervalModel.getRrInterval() + " " + lRrIntervalModel.getUser());
                    receiveData(lRrIntervalModel);
                }
            }
        };

        // Custom callback to handle data stream from Maxim Integrated MAXREFDES73 notifications
        // listen to Heart Rate Measurement Caracteristic with private data format
        mCustomMAXREFDES73ReadWriteListener = new BleDevice.ReadWriteListener(){
            @Override
            public void onEvent(ReadWriteEvent e) {
                if(e.wasSuccess() && e.type() == Type.NOTIFICATION){
                    GattCustomGSRTemperatureCharacteristicReader lGattCharacteristicReader = new GattCustomGSRTemperatureCharacteristicReader();
                    lGattCharacteristicReader.read(e.characteristic());

                    Calendar c = Calendar.getInstance();
                    String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());
                    String lDeviceAddress = e.device().getMacAddress();

                    SkinTemperatureModel lSkinTemperatureModel = new SkinTemperatureModel(lDeviceAddress, lCurrentTimestamp, lGattCharacteristicReader.getSkinTemperature());
                    ElectroDermalActivityModel lElectroDermalActivityModel = new ElectroDermalActivityModel(lDeviceAddress, lCurrentTimestamp, 7812, lGattCharacteristicReader.getElectroDermalActivity());
                    Log.d(TAG, lSkinTemperatureModel.getTimestamp() + " " + lSkinTemperatureModel.getUuid() + " " + lSkinTemperatureModel.getTemperature() + " " + lSkinTemperatureModel.getUser());
                    Log.d(TAG, lElectroDermalActivityModel.getTimestamp() + " " + lElectroDermalActivityModel.getUuid() + " " + lElectroDermalActivityModel.getElectroDermalActivity() + " " + lElectroDermalActivityModel.getUser());


                    receiveData(lSkinTemperatureModel);
                    receiveData(lElectroDermalActivityModel);
                }
            }
        };

        //Callback use to handle Bluetooth scanning
        mDiscoveryListener = new BleManager.DiscoveryListener() {
            @Override
            public void onEvent(DiscoveryEvent e) {
                if (e.was(LifeCycle.DISCOVERED)) {
                    Log.d(TAG, "Discovery Event - "+ e);
                    if(isHeartRateCompatibleDevice(e.device())){
                        connectDevice(e.device(), mHeartRateReadWriteListener);
                    }
                    else if(isGSRTemperatureCustomCompatibleDevice(e.device())){
                        connectDevice(e.device(), mCustomMAXREFDES73ReadWriteListener);
                    }
                }
            }
        };

        mConnectedDevices = new ConcurrentHashMap<String, BleDevice>();
    }

    /**
     * @brief method to handle device connect logic - service profile, measurement characteristic, incomming data parsing
     *
     * @param iDevice device to connect
     * @param iReadWriteListener data parsing callback
     */
    private void connectDevice(BleDevice iDevice, final BleDevice.ReadWriteListener iReadWriteListener) {
        iDevice.connect(new BleDevice.StateListener(){
            @Override
            public void onEvent(BleDevice.StateListener.StateEvent e) {

                Log.d(TAG, "ConnectionEvent - " + e);

                if(e.didEnter(BleDeviceState.INITIALIZED)){
                    Log.d(TAG, "deviceConnected");
                    mConnectedDevices.put(e.device().getMacAddress(), e.device());
                    startPairing();

                    e.device().enableNotify(Uuids.HEART_RATE_SERVICE_UUID, Uuids.HEART_RATE_MEASUREMENT, iReadWriteListener);

                }
                else if (e.didEnter(BleDeviceState.DISCONNECTED)){

                    Log.d(TAG, "deviceDisconnected");
                    mConnectedDevices.remove(e.device().getMacAddress());

                    if(allDevicesDisconnected()){
                        endPairing();
                    }

                    BleManager.get(mActivity).turnOff();

                }
            }
        });
    }

    /**
     * @brief check if all devices are disconnected
     *
     * @return true if all devices are disconnected, false otherwise
     */
    private boolean allDevicesDisconnected() {
        return mConnectedDevices.size() == 0;
    }

    /**
     * @brief check if available bluetooth devices are compatibles with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isCompatibleDevice(BleDevice device) {

        if(isHeartRateCompatibleDevice(device) || isGSRTemperatureCustomCompatibleDevice(device) ){
            return true;
        }

        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for heart rate data streaming with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isHeartRateCompatibleDevice(BleDevice device) {

        String lDeviceName = device.getName_native();

        if(lDeviceName != null) {
            String lDeviceUpperName = lDeviceName.toUpperCase();

            if ((lDeviceUpperName.contains("RHYTHM") || lDeviceUpperName.contains("POLAR") || lDeviceUpperName.contains("MIO"))) {
                return true;
            }

            return false;
        }
        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for temperature and electro dermal activity
     * data streaming with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isGSRTemperatureCustomCompatibleDevice(BleDevice device) {
        String lDeviceName = device.getName_native();

        if(lDeviceName != null) {
            String lDeviceUpperName = lDeviceName.toUpperCase();

            if( lDeviceUpperName.contains("MAXREFDES73")) {
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * @brief start automatic pairing
     *
     * @param iActivity activity context, mandatory since Marshmallow
     */
    public void automaticPairing(Activity iActivity){

        super.automaticPairing();
        mActivity = iActivity;

        mConnectedDevices.clear();
        mScanningHandler = new Handler();

        BluetoothEnabler.start(iActivity, new BluetoothEnabler.DefaultBluetoothEnablerFilter()
        {
            @Override public Please onEvent(BluetoothEnablerEvent e)
            {
                Log.d(TAG, "Bluetooth Enabler Event - " + e);
                if( e.isDone() )
                {
                    final BluetoothEnablerEvent eFinal = e;
                    mScanningHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            eFinal.bleManager().stopScan();
                            if(mConnectedDevices.size() < 1){
                                endPairing();
                            }
                        }
                    }, SCAN_PERIOD);

                    eFinal.bleManager().startScan(mDiscoveryListener);
                }
                else if( e.status().isCancelled() ){
                    endPairing();
                }

                return super.onEvent(e);
            }
        });
    }

    public void startPairing(){
        super.startPairing();
    }

    public void endPairing(){
        super.endPairing();

    }

    /**
     * @brief receive a physiological data sample and filter corrupted values
     *
     * @param iPhysioSignal input physiological data sample
     */
    private void receiveData(PhysioSignalModel iPhysioSignal){
        // filter corrupted cardiac R-R intervals
        if( iPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE) ){
            RRIntervalModel lRrIntervalModel = ((RRIntervalModel) iPhysioSignal);
            if(lRrIntervalModel.getRrInterval() == 0 || lRrIntervalModel.getTimestamp() == null
                    || lRrIntervalModel.getTimestamp() == ""){
                return;
            }
        }

        this.setChanged();
        this.notifyObservers(new DevicePairingReceivedDataNotification(iPhysioSignal));
    }

    /**
     * @brief get connected devices though Bluetooth LE
     *
     * @return device info list
     */
    @Override
    public LinkedList<DeviceInfo> getDeviceList(){
        LinkedList<DeviceInfo> oDeviceList = new LinkedList<>();

        for ( Map.Entry<String, BleDevice> lEntry : mConnectedDevices.entrySet() ) {
            oDeviceList.add(new DeviceInfo(lEntry.getValue().getMacAddress(), lEntry.getValue().getName_native()));
        }

        return oDeviceList;
    }

    /**
     * @brief close service in the application exit
     */
    @Override
    public void close(){
        BleManager.get(mActivity).turnOff();
        super.close();
    }


}
