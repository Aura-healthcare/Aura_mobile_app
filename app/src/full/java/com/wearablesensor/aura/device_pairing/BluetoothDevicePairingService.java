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

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.Uuids;

import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattCustomGSRTemperatureCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattHeartRateCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattMetaWearMovementCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattMovementCharacteristicReader;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingBatteryLevelNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDeviceDiscoveredNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingEndDiscoveryNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingSessionDurationNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class BluetoothDevicePairingService extends DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();

    // Stops scanning after 20 seconds.
    private static final long SCAN_PERIOD = 20000;

    // Bluetooth scanning members
    private Handler mScanningHandler;
    private BleManager.DiscoveryListener mDiscoveryListener;

    private ConcurrentHashMap<String, DeviceInfo> mCachedDevicesInfo;

    private CountDownTimer mSessionTimer;
    private long mCachedSessionDuration; // in milliseconds
    // a enum to describe action types that can be applied to a specific GATT characteristic
    public enum StateListenerAction{
        READ,
        WRITE,
        ENABLE_NOTIFICATION
    }

    /**
     * @class an inner class used to described actions to realise on GATT channel (read/write/notification) and
     * associated callbacks
     *
     */
    class StateListenerConfig
    {
        private UUID mGattService; // selected GATT service
        private UUID mGattCharacteristic; // selected GATT characteristic
        private BleDevice.ReadWriteListener mReadWriteListener; // action callback
        private StateListenerAction mAction; // action type
        private byte[] mGattAttributes;

        StateListenerConfig(UUID iGattService, UUID iGattCharacteristic, BleDevice.ReadWriteListener  iReadWriteListener, StateListenerAction iAction, byte[] iGattAttributes) {
            mGattService = iGattService;
            mGattCharacteristic = iGattCharacteristic;
            mReadWriteListener = iReadWriteListener;
            mAction = iAction;
            mGattAttributes = iGattAttributes;
        }

        public UUID getGattService(){
            return mGattService;
        }

        public UUID getGattCharacteristic(){
            return mGattCharacteristic;
        }

        public BleDevice.ReadWriteListener getReadWriteListener(){
            return mReadWriteListener;
        }

        public StateListenerAction getAction(){
            return mAction;
        }

        public byte[] getGattAttributes() { return mGattAttributes; }
    }

    //Bluetooth data streaming callbacks
    private BleDevice.ReadWriteListener mHeartRateReadWriteListener;
    private BleDevice.ReadWriteListener mCustomMAXREFDES73ReadWriteListener;
    private BleDevice.ReadWriteListener mMotionMovuinoReadWriteListener;
    private BleDevice.ReadWriteListener mMetaWearReadWriteListener;

    private BleDevice.ReadWriteListener mBatteryReadWriteListener;

    private ConcurrentHashMap<String, BleDevice> mConnectedDevices; // hashmap storing the currently connected devices list

    public BluetoothDevicePairingService(Context iContext){
        super(iContext);

        // callback used to handle standard Heart rate profile
        mHeartRateReadWriteListener = new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent e) {
                if(e.wasSuccess() && e.type() == Type.NOTIFICATION){
                    PhysioEvent data = new PhysioEvent(e);
                    GattHeartRateCharacteristicReader lGattCharacteristicReader = new GattHeartRateCharacteristicReader();
                    lGattCharacteristicReader.read(data);

                    Calendar c = Calendar.getInstance();
                    String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());

                    Integer[] lRrIntervals = lGattCharacteristicReader.getRrInterval();
                    if(lRrIntervals == null){
                        return;
                    }

                    for(Integer lRrInterval : lRrIntervals) {
                        RRIntervalModel lRrIntervalModel = new RRIntervalModel(e.device().getMacAddress(), lCurrentTimestamp, lRrInterval);
                        Log.d(TAG, lRrIntervalModel.getTimestamp() + " " + lRrIntervalModel.getUuid() + " " + lRrInterval + " " + lRrIntervalModel.getUser());
                        receiveData(lRrIntervalModel);
                    }
                }
            }
        };

        // Custom callback to handle data stream from Maxim Integrated MAXREFDES73 notifications
        // listen to Heart Rate Measurement Caracteristic with private data format
        mCustomMAXREFDES73ReadWriteListener = new BleDevice.ReadWriteListener(){
            @Override
            public void onEvent(ReadWriteEvent e) {
                if(e.wasSuccess() && e.type() == Type.NOTIFICATION){
                    PhysioEvent data = new PhysioEvent(e);
                    GattCustomGSRTemperatureCharacteristicReader lGattCharacteristicReader = new GattCustomGSRTemperatureCharacteristicReader();
                    lGattCharacteristicReader.read(data);

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

        mBatteryReadWriteListener = new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent e) {
                if (e.wasSuccess() && e.type() == Type.NOTIFICATION || e.type() == Type.READ) {
                   int lBatteryPercentage = e.data_byte();
                    Log.d(TAG, "Battery  Event"  + e.data_byte() + " "+ e.data().length);
                    DeviceInfo lDeviceInfo = new DeviceInfo(e.device().getMacAddress(), e.device().getName_native(), lBatteryPercentage);
                    receiveBatteryLevel(lDeviceInfo);
                }
            }
        };

        mMotionMovuinoReadWriteListener = new BleDevice.ReadWriteListener(){

            @Override
            public void onEvent(ReadWriteEvent e) {
                Log.d(TAG, e.toString());
                if (e.wasSuccess() && e.type() == Type.NOTIFICATION) {
                    PhysioEvent data = new PhysioEvent(e);
                    Log.d(TAG, "Motion Movuino Event" + e.data_utf8() +" " + e.data().length);
                    GattMovementCharacteristicReader lGattMovementCharacteristic = new GattMovementCharacteristicReader();
                    Boolean lStatus = lGattMovementCharacteristic.read(data);

                    if(lStatus) {
                        Calendar c = Calendar.getInstance();
                        String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());
                        String lDeviceAddress = e.device().getMacAddress();

                        MotionAccelerometerModel lAccelerometerModel = new MotionAccelerometerModel(lDeviceAddress, lCurrentTimestamp, lGattMovementCharacteristic.getAccelerometer(), "2G");
                        MotionGyroscopeModel lGyroscopeModel = new MotionGyroscopeModel(lDeviceAddress, lCurrentTimestamp, lGattMovementCharacteristic.getGyroscope());

                        receiveData(lAccelerometerModel);
                        receiveData(lGyroscopeModel);
                    }
                }
            }
        };

        mMetaWearReadWriteListener = new BleDevice.ReadWriteListener(){

            @Override
            public void onEvent(ReadWriteEvent e) {
                if (e.wasSuccess() && e.type() == Type.NOTIFICATION) {
                    PhysioEvent data = new PhysioEvent(e);
                    Log.d(TAG, "Motion MetaWear Event" + e.data_utf8() +" " + e.data().length);
                    GattMetaWearMovementCharacteristicReader lGattMetaWearCharacteristicReader = new GattMetaWearMovementCharacteristicReader();
                    Boolean lStatus = lGattMetaWearCharacteristicReader.read(data);

                    if(lStatus) {
                        Calendar c = Calendar.getInstance();
                        String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());
                        String lDeviceAddress = e.device().getMacAddress();

                        if(lGattMetaWearCharacteristicReader.isAcceleration()) {
                            MotionAccelerometerModel lAccelerometerModel = new MotionAccelerometerModel(lDeviceAddress, lCurrentTimestamp, lGattMetaWearCharacteristicReader.getAccelerometer(), "2G");

                            receiveData(lAccelerometerModel);
                        }
                        else{
                            MotionGyroscopeModel lGyroscopeModel = new MotionGyroscopeModel(lDeviceAddress, lCurrentTimestamp, lGattMetaWearCharacteristicReader.getGyroscope());

                            receiveData(lGyroscopeModel);
                        }
                    }
                }
            }
        };

        //Callback use to handle Bluetooth scanning
        mDiscoveryListener = new BleManager.DiscoveryListener() {
            @Override
            public void onEvent(DiscoveryEvent e) {
                if (e.was(LifeCycle.DISCOVERED)) {
                    Log.d(TAG, "Discovery Event - "+ e.device().getName_native());
                    if(AuraDevicePairingCompatibility.isCompatibleDevice(e.device().getName_native())) {
                        LinkedList<BleDevice> lDeviceList = getDiscoveredDeviceList();
                        EventBus.getDefault().post(new DevicePairingDeviceDiscoveredNotification(lDeviceList));
                    }
                }
            }
        };

        mConnectedDevices = new ConcurrentHashMap<String, BleDevice>();
        mCachedDevicesInfo = new ConcurrentHashMap<String, DeviceInfo>();

        mSessionTimer = new CountDownTimer(1000 * 60 * 60 * 20, 1000 * 60) {
            @Override
            public void onTick(long l) {
                long lElapsedTime = 20 * 60 * 60 * 1000 - l;
                mCachedSessionDuration = lElapsedTime;
                EventBus.getDefault().post(new DevicePairingSessionDurationNotification(DevicePairingStatus.SESSION_DURATION, lElapsedTime));
            }

            @Override
            public void onFinish() {
                mCachedSessionDuration = 0l;
            }
        };
        mCachedSessionDuration = 0l;
    }

    @Override
    public void automaticScan(final Context iContext){
        super.automaticScan(iContext);

        Log.d(TAG, "automaticScan");
        BluetoothEnabler.start(iContext, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {

            private Boolean mHasBeenCanceled = false;

            @Override
            public Please onEvent(BluetoothEnablerEvent e) {
                Log.d(TAG, "Bluetooth Enabler Event - " + e);

                if (e.status().isCancelled()) {
                    mHasBeenCanceled = true;
                    LinkedList<BleDevice> lDeviceList = getDiscoveredDeviceList();
                    EventBus.getDefault().post(new DevicePairingEndDiscoveryNotification(lDeviceList));
                }
                if (e.isDone()) {
                    scan(iContext);
                }

                return super.onEvent(e);
            }
        });

    }

    public void scan(final Context iContext){
        mScanningHandler = new Handler();

        mScanningHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.get(iContext).stopScan();

                LinkedList<BleDevice> lDeviceList = getDiscoveredDeviceList();
                EventBus.getDefault().post(new DevicePairingEndDiscoveryNotification(lDeviceList));
            }
        }, SCAN_PERIOD);
        BleManager.get(iContext).startScan(mDiscoveryListener);
    }

    @Override
    public void configureAndConnectDevice(BleDevice iDevice){
        if(AuraDevicePairingCompatibility.isHeartRateCompatibleDevice(iDevice.getName_native())){
            ArrayList<StateListenerConfig> lStateListeners = new ArrayList<>();
            lStateListeners.add(new StateListenerConfig(Uuids.HEART_RATE_SERVICE_UUID, Uuids.HEART_RATE_MEASUREMENT, mHeartRateReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));
            lStateListeners.add(new StateListenerConfig(Uuids.BATTERY_SERVICE_UUID, Uuids.BATTERY_LEVEL, mBatteryReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION,null));
            connectDevice(iDevice, lStateListeners);
        }
        else if(AuraDevicePairingCompatibility.isGSRTemperatureCustomCompatibleDevice(iDevice.getName_native())){
            ArrayList<StateListenerConfig> lStateListeners = new ArrayList<>();
            lStateListeners.add(new StateListenerConfig(Uuids.HEART_RATE_SERVICE_UUID, Uuids.HEART_RATE_MEASUREMENT, mCustomMAXREFDES73ReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));
            lStateListeners.add(new StateListenerConfig(Uuids.BATTERY_SERVICE_UUID, Uuids.BATTERY_LEVEL, mBatteryReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));
            connectDevice(iDevice, lStateListeners);
        }
        else if(AuraDevicePairingCompatibility.isMotionMovuinoCompatibleDevice(iDevice.getName_native())){
            ArrayList<StateListenerConfig> lStateListeners = new ArrayList<>();

            //lStateListeners.add(new StateListenerConfig(Uuids.RX_SERVICE_UUID, Uuids.RX_CHAR_UUID, mMotionMovuinoReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));
            //lStateListeners.add(new StateListenerConfig(Uuids.RX_SERVICE_UUID, Uuids.TX_CHAR_UUID, mMotionMovuinoReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));

            connectDevice(iDevice, lStateListeners);
        }
        else if(AuraDevicePairingCompatibility.isMetaWearCompatibleDevice(iDevice.getName_native())){
            ArrayList<StateListenerConfig> lStateListeners = new ArrayList<>();

            lStateListeners.add(new StateListenerConfig(Uuids.BATTERY_SERVICE_UUID, Uuids.BATTERY_LEVEL, mBatteryReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));
            lStateListeners.add(new StateListenerConfig(Uuids.BATTERY_SERVICE_UUID, Uuids.BATTERY_LEVEL, mBatteryReadWriteListener, StateListenerAction.READ, null));

            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[] {(byte)0x1, (byte) 0x1, (byte) 0x1}));

            byte[] lAccConfig = MetaFirmware.Acceleration.getConfig(MetaFirmware.Acceleration.OutputDataRate.ODR_50_HZ, MetaFirmware.Acceleration.Range.AR_2G);
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[] {MetaFirmware.Module.ACCELERATION.id, MetaFirmware.Acceleration.Register.DATA_CONFIG.id, lAccConfig[0]/*(byte)0x27*/, lAccConfig[1]/*(byte)0x3*/}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[] {MetaFirmware.Module.ACCELERATION.id, MetaFirmware.Acceleration.Register.DATA_INTERRUPT.id, (byte) 0x1}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[]{MetaFirmware.Module.ACCELERATION.id, MetaFirmware.Acceleration.Register.DATA_INTERRUPT_ENABLE.id, (byte) 0x1, (byte) 0x0}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[]{MetaFirmware.Module.ACCELERATION.id, MetaFirmware.Acceleration.Register.POWER_MODE.id, (byte) 0x1, (byte) 0x1}));

            byte[] lGyroConfig = MetaFirmware.Gyroscope.getConfig(MetaFirmware.Gyroscope.OutputDataRate.ODR_50_HZ, MetaFirmware.Gyroscope.Range.FSR_500, MetaFirmware.Gyroscope.FilterMode.NORMAL);
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[] {MetaFirmware.Module.GYROSCOPE.id, MetaFirmware.Gyroscope.Register.CONFIG.id, lGyroConfig[0]/*(byte)0x26*/, lGyroConfig[1]/*(byte)0x04*/}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[] {MetaFirmware.Module.GYROSCOPE.id, MetaFirmware.Gyroscope.Register.DATA.id, (byte) 0x1}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[]{MetaFirmware.Module.GYROSCOPE.id, MetaFirmware.Gyroscope.Register.DATA_INTERRUPT_ENABLE.id, (byte) 0x1, (byte) 0x0}));
            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_CONFIG_CHARACTERISTIC, null, StateListenerAction.WRITE, new byte[]{MetaFirmware.Module.GYROSCOPE.id, MetaFirmware.Gyroscope.Register.POWER_MODE.id, (byte) 0x1, (byte) 0x1}));

            lStateListeners.add(new StateListenerConfig(MetaFirmware.META_GATT_SERVICE, MetaFirmware.META_GATT_NOTFICATIONS_CHARACTERISTIC, mMetaWearReadWriteListener, StateListenerAction.ENABLE_NOTIFICATION, null));

            connectDevice(iDevice, lStateListeners);
        }
    }

    /**
     * @brief method to handle device connect logic - service profile, measurement characteristic, incomming data parsing
     *
     * @param iDevice device to connect
     * @param iStateListeners
     */
    private void connectDevice(BleDevice iDevice, final ArrayList<StateListenerConfig> iStateListeners) {

        EventBus.getDefault().post(new DevicePairingNotification(DevicePairingStatus.START_CONNECTING));

        iDevice.connect(new BleDevice.StateListener(){
            @Override
            public void onEvent(BleDevice.StateListener.StateEvent e) {

                Log.d(TAG, "ConnectionEvent - " + e);

                if(e.didEnter(BleDeviceState.INITIALIZED)){
                    Log.d(TAG, "deviceConnected");
                    mConnectedDevices.put(e.device().getMacAddress(), e.device());
                    if(!mPaired) {
                        mSessionTimer.start();
                    }
                    deviceConnected();


                    for(StateListenerConfig iStateListener : iStateListeners) {
                        if (iStateListener.getAction() == StateListenerAction.ENABLE_NOTIFICATION) {
                            e.device().enableNotify(iStateListener.getGattService(), iStateListener.getGattCharacteristic(), iStateListener.getReadWriteListener());
                        } else if (iStateListener.getAction() == StateListenerAction.WRITE) {
                            e.device().write(iStateListener.getGattService(), iStateListener.getGattCharacteristic(), iStateListener.getGattAttributes());
                        } else if (iStateListener.getAction() == StateListenerAction.READ){
                            e.device().read(iStateListener.getGattService(), iStateListener.getGattCharacteristic(), iStateListener.getReadWriteListener());
                        }

                    }
                }

                else if (e.didEnter(BleDeviceState.DISCONNECTED)){
                    Log.d(TAG, "deviceDisconnected");
                    // workaround - disconnect only previously connected devices
                    // do not process every disconnected events
                    if(e.device().is(BleDeviceState.CONNECTING) || e.device().is(BleDeviceState.CONNECTED) || e.didExit(BleDeviceState.CONNECTED)) {
                        mConnectedDevices.remove(e.device().getMacAddress());
                        mCachedDevicesInfo.remove(e.device().getMacAddress());

                        if (allDevicesDisconnected()) {
                            mPaired = false;
                            mSessionTimer.cancel();
                        }

                        deviceDisconnected();
                    }
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

        EventBus.getDefault().post(new DevicePairingReceivedDataNotification(iPhysioSignal));
    }

    /**
     * @brief receive Battery level update for a single device
     *
     * @param iDeviceInfo input updated device info
     */
    private void receiveBatteryLevel(DeviceInfo iDeviceInfo) {
        mCachedDevicesInfo.put(iDeviceInfo.getId(), iDeviceInfo);
        EventBus.getDefault().post(new DevicePairingBatteryLevelNotification(iDeviceInfo));
    }

    public ConcurrentHashMap<String, DeviceInfo> getCachedDevicesInfo() {
        return mCachedDevicesInfo;
    }

    /**
     * @brief get cache session duration
     *
     * @return session duration if session started otherwise 0
     */

    public long getCachedSessionDuration(){
        return mCachedSessionDuration;
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
        BleManager.get(mContext).turnOff();
        super.close();
    }

    @Override
    public boolean disconnectDevices(){
        if(mConnectedDevices.size() == 0){
            return false;
        }

        for(BleDevice lBleDevice: mConnectedDevices.values()){
            lBleDevice.disconnect();
        }

        return super.disconnectDevices();
    }

}
