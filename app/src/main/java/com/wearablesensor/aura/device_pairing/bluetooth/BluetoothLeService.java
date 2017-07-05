/**
 * @file BluetoothLeService.java
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 *
 * BluetoothLeService is inspired by Android Sample on BluetoothLe
 */


package com.wearablesensor.aura.device_pairing.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.GattManager;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattDisconnectOperation;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattSetNotificationOperation;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattHeartRateCharacteristicReader;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private GattManager mGattManager; //manager handling gatt connection with devices

    private ConcurrentHashMap<String, BluetoothLeConstant.DeviceConnectionState> mConnectedDeviceStatus; // hashmap storing connection status for every devices
    private ConcurrentHashMap<String, BluetoothDevice> mConnectedDevices; // hashmap storing the to-be connected device list
    private final static android.os.Handler mHandler = new android.os.Handler();

    public final static String ACTION_GATT_START_PAIRING =
            "com.wearablesensor.aura.bluetooth.ACTION_GATT_START_PAIRING";
    public final static String ACTION_GATT_END_PAIRING =
            "com.wearablesensor.aura.bluetooth.ACTION_GATT_END_PAIRING";
    public final static String ACTION_DATA_AVAILABLE =
            "com.wearablesensor.aura.bluetooth.ACTION_DATA_AVAILABLE";

    public final static String DEVICEADRESS_EXTRA_DATA = "com.example.bluetooth.le.DEVICEADRESS_EXTRA_DATA";
    public final static String TIMESTAMP_EXTRA_DATA = "com.example.bluetooth.le.TIMESTAMP_EXTRA_DATA";;
    public final static String RR_EXTRA_DATA = "com.example.bluetooth.le.RR_EXTRA_DATA";;


    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     *@brief Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mGattManager = new GattManager(this);

        mConnectedDevices = new ConcurrentHashMap<>();
        mConnectedDeviceStatus = new ConcurrentHashMap<>();

        return true;
    }

    /**
     * @brief method used to initialize Gatt connection between the mobile application and the
     * device list
     *
     * @param iDeviceList device list to be connected
     */
    public void connect(LinkedList<BluetoothDevice> iDeviceList){
        BluetoothDevice lDevice;

        for(int i = 0; i < iDeviceList.size(); i++) {
            lDevice = iDeviceList.get(i);
            mConnectedDevices.put(lDevice.getAddress(), lDevice);
            mConnectedDeviceStatus.put(lDevice.getAddress(), BluetoothLeConstant.DeviceConnectionState.Connecting);
            mGattManager.queue(new GattSetNotificationOperation(mGattManager, lDevice, BluetoothLeConstant.UUID_HEART_RATE_SERVICE, BluetoothLeConstant.UUID_HEART_RATE_MEASUREMENT, BluetoothLeConstant.UUID_CLIENT_CHARACTERISTIC_CONFIG));
            Log.d(TAG, "Device Connecting :" + lDevice.getAddress() + " " + lDevice.getName());
        }
    }

    /**
     * @brief close every Gatt connections with the devices
     */
    public void close() {

        BluetoothDevice lDevice;
        BluetoothLeConstant.DeviceConnectionState lDeviceStatus;

        for ( String lDeviceAdress : mConnectedDevices.keySet() ) {
            lDevice = mConnectedDevices.get(lDeviceAdress);
            lDeviceStatus = mConnectedDeviceStatus.get(lDeviceAdress);

            if( !(lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Disconnecting) || lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Disconnected)) ){
                mConnectedDeviceStatus.put(lDeviceAdress, BluetoothLeConstant.DeviceConnectionState.Disconnecting);
                mGattManager.queue(new GattDisconnectOperation(lDevice));
                Log.d(TAG, "Device Disconnecting :" + lDevice.getAddress() + " " + lDevice.getName());
            }
        }
    }

    /**
     * @brief new device connected and providing services
     *
     * @param iDevice newly connected device
     */
    public void deviceConnected(BluetoothDevice iDevice){
        mConnectedDeviceStatus.put(iDevice.getAddress(), BluetoothLeConstant.DeviceConnectionState.Connected);

        if(allDevicesAreConnected()){
            broadcastUpdate(ACTION_GATT_START_PAIRING);
        }
        Log.d(TAG, "Device Connected :" + iDevice.getAddress() + " " + iDevice.getName());
    }

    /**
     * @brief device disconnected
     *
     * @param iDevice newly disconnected device
     */
    public void deviceDisconnected(BluetoothDevice iDevice){
        mConnectedDeviceStatus.put(iDevice.getAddress(), BluetoothLeConstant.DeviceConnectionState.Disconnected);
        Log.d(TAG, "Device Disconnected :" + iDevice.getAddress() + " " + iDevice.getName());

        // once a device is disconnected force close service
        if(!allDevicesAreDisconnecting()) {
            close();
        }
        else if(allDevicesAreDisconnected()){
            broadcastUpdate(ACTION_GATT_END_PAIRING);
        }
    }

    /**
     * @brief check if all devices are connected
     *
     * @return true if all devices are connected
     */

    private Boolean allDevicesAreConnected() {
        BluetoothLeConstant.DeviceConnectionState lDeviceStatus;

        for ( String lDeviceAdress : mConnectedDevices.keySet() ) {
            lDeviceStatus = mConnectedDeviceStatus.get(lDeviceAdress);

            if( !lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Connected) ){
                Log.d(TAG, "AllDevicesNotConnected");
                return false;
            }
        }
        Log.d(TAG, "AllDevicesConnected");
        return true;
    }

    /**
     * @brief all device are disconnected
     *
     * @return true if every devices are disconnected otherwise false
     */
    private Boolean allDevicesAreDisconnected() {
        BluetoothLeConstant.DeviceConnectionState lDeviceStatus;

        for ( String lDeviceAdress : mConnectedDevices.keySet() ) {
            lDeviceStatus = mConnectedDeviceStatus.get(lDeviceAdress);

            if( !lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Disconnected) ){
                Log.d(TAG, "AllDevicesNotDisconnected");
                return false;
            }
        }
        Log.d(TAG, "AllDevicesDisconnected");
        return true;
    }

    /**
     * @brief all devices are disconnecting or disconnected
     *
     * @return true if every devices are disconnecting or disconnected
     */
    private Boolean allDevicesAreDisconnecting() {
        BluetoothLeConstant.DeviceConnectionState lDeviceStatus;

        for ( String lDeviceAdress : mConnectedDevices.keySet() ) {
            lDeviceStatus = mConnectedDeviceStatus.get(lDeviceAdress);

            if( !(lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Disconnected) ||  lDeviceStatus.equals(BluetoothLeConstant.DeviceConnectionState.Disconnecting)) ){
                Log.d(TAG, "AllDevicesNotDisconnecting/Disconnected");
                return false;
            }
        }
        Log.d(TAG, "AllDevicesDisconnecting/Disconnected");
        return true;
    }

    /**
     * @brief new Characteristic update received
     *
     * @param iGattCharacteristic updated characteristic
     * @param iDevice device hosting the characteristic
     */
    public void receiveCharacteristicNotification(final BluetoothGattCharacteristic iGattCharacteristic,
                                                  final BluetoothDevice iDevice){
        // emit data only once every devices has been connected
        if(allDevicesAreConnected()){
            broadcastUpdate(ACTION_DATA_AVAILABLE, iGattCharacteristic, iDevice);
        }
    }

    /**
     * @brief broadcast pairing status to application
     *
     * @param action pairing status
     */
    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * @brief broadcast data to application
     *
     * @param action action status
     * @param iGattCharacteristic updated characteristic
     * @param iDevice device hosting the characteristic
     */
    public void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic iGattCharacteristic,
                                 final BluetoothDevice iDevice) {
        final Intent intent = new Intent(action);

        if (BluetoothLeConstant.UUID_HEART_RATE_MEASUREMENT.equals(iGattCharacteristic.getUuid())) {

            GattHeartRateCharacteristicReader lGattCharacteristicReader = new GattHeartRateCharacteristicReader();
            lGattCharacteristicReader.read(iGattCharacteristic);

            Calendar c = Calendar.getInstance();
            String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());
            Log.d(TAG, "BROADCAST UPDATE " + iDevice + " " + lGattCharacteristicReader);

            //TODO:replace by Parcelable
            intent.putExtra(BluetoothLeService.DEVICEADRESS_EXTRA_DATA, iDevice.getAddress());
            intent.putExtra(BluetoothLeService.TIMESTAMP_EXTRA_DATA, lCurrentTimestamp);
            intent.putExtra(BluetoothLeService.RR_EXTRA_DATA, lGattCharacteristicReader.getRrInterval());
            sendBroadcast(intent);
        }
    }

    /**
     * @brief wrapper method use to force a process to run on UI Thread
     * @details used to force Gatt connection on UI Thread
     *
     * @param runnable
     */
    public static final void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }
}
