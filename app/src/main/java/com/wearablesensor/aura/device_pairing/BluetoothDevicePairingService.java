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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.bluetooth.BluetoothLeService;
import com.wearablesensor.aura.device_pairing.bluetooth.BluetoothServiceConnection;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BluetoothDevicePairingService extends DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();

    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    private Boolean mScanning;

    // Bluetooth members
    private boolean mIsBluetoothLeFeatureSupported;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // Bluetooth scanning members
    private Handler mScanningHandler;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ArrayList<BluetoothDevice> mBluetoothDeviceList;

    //Bluetooth connection members
    private BluetoothServiceConnection mDeviceServiceConnection;
    private BroadcastReceiver mGattUpdateReceiver;

    public BluetoothDevicePairingService(boolean iIsBluetoothFeatureLeSupported, BluetoothManager iBluetoothManager, Context iContext){
        super(iContext);

        mDeviceServiceConnection = null;

        mScanning = false;

        mIsBluetoothLeFeatureSupported = iIsBluetoothFeatureLeSupported;

        mBluetoothManager = iBluetoothManager;
        mBluetoothAdapter = null;
        mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

        mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_START_PAIRING.equals(action)) {
                    startPairing();
                } else if (BluetoothLeService.ACTION_GATT_END_PAIRING.equals(action)) {
                    endPairing();
                    mContext.unbindService(mDeviceServiceConnection);
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    String lDeviceAddress = intent.getStringExtra(BluetoothLeService.DEVICEADRESS_EXTRA_DATA);
                    String lTimestamp = intent.getStringExtra(BluetoothLeService.TIMESTAMP_EXTRA_DATA);
                    Integer lRr = intent.getIntExtra(BluetoothLeService.RR_EXTRA_DATA, 0);

                    RRIntervalModel lRRIntervalModel = new RRIntervalModel(lDeviceAddress, lTimestamp, lRr);
                    Log.d(TAG, lRRIntervalModel.getTimestamp() + " " + lRRIntervalModel.getUuid() + " " + lRRIntervalModel.getRrInterval() + " " + lRRIntervalModel.getUser());
                    receiveData(lRRIntervalModel);
                }
            }
        };

        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                mBluetoothDeviceList.add(device);
            }
        };

        mDeviceServiceConnection = new BluetoothServiceConnection(this);
    }

    public Boolean checkBluetoothIsEnabled(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!mIsBluetoothLeFeatureSupported) {
            return false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return false;
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }

        return true;
    }

    public void automaticPairing(){
        super.automaticPairing();

        mScanningHandler = new Handler();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!checkBluetoothIsEnabled()) {
            endPairing();
            return;
        }
        else{
            scanLeDevices();
        }
    }

    public void startPairing(){
        super.startPairing();
    }

    public void endPairing(){
        super.endPairing();

    }


    private void scanLeDevices() {

        mBluetoothDeviceList.clear();

        // Stops scanning after a pre-defined scan period.
        mScanningHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);


                LinkedList<BluetoothDevice> lDeviceList = new LinkedList<BluetoothDevice>();
                BluetoothDevice lDevice =  null;

                for(int i = 0;i < mBluetoothDeviceList.size(); i++){
                    if(mBluetoothDeviceList.get(i).getName() != null && (mBluetoothDeviceList.get(i).getName().contains("RHYTHM") || mBluetoothDeviceList.get(i).getName().contains("Polar")) ){
                        lDeviceList.add(mBluetoothDeviceList.get(i));
                    }
                }

                if(lDeviceList.size() == 0){
                    endPairing();
                    return;
                }

                mDeviceServiceConnection.setDeviceList(lDeviceList);
                mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
                mContext.bindService(gattServiceIntent, mDeviceServiceConnection, Context.BIND_AUTO_CREATE);

            }
        }, SCAN_PERIOD);

        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private void receiveData(RRIntervalModel iRrIntervalModel){
        // filter corrupted cardiac R-R intervals
        if(iRrIntervalModel.getRrInterval() == 0 || iRrIntervalModel.getTimestamp() == null
                || iRrIntervalModel.getTimestamp() == ""){
            return;
        }

        this.setChanged();
        this.notifyObservers(new DevicePairingReceivedDataNotification(iRrIntervalModel));
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_START_PAIRING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_END_PAIRING);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * @brief get connected devices though Bluetooth LE
     *
     * @return device info list
     */
    @Override
    public LinkedList<DeviceInfo> getDeviceList(){
        LinkedList<DeviceInfo> oDeviceList = new LinkedList<>();

        ConcurrentHashMap<String, BluetoothDevice> lDeviceList = mDeviceServiceConnection.getBluetoothLeService().getDeviceList();
        for ( Map.Entry<String, BluetoothDevice> lEntry : lDeviceList.entrySet() ) {
            oDeviceList.add(new DeviceInfo(lEntry.getValue().getAddress(), lEntry.getValue().getName()));
        }

        return oDeviceList;
    }

    /**
     * @brief close service in the application exit
     */
    @Override
    public void close(){
        if(mDeviceServiceConnection != null && mDeviceServiceConnection.getBluetoothLeService() != null) {
            mDeviceServiceConnection.getBluetoothLeService().close();
        }
        super.close();
    }


}
