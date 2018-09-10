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

import java.util.LinkedList;
import java.util.Observable;

import android.os.Vibrator;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceIterator;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStartDiscoveryNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import org.greenrobot.eventbus.EventBus;

public class DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();

    private final static int PHONE_VIBRATION_DURATION = 1500; // in milliseconds
    protected Context mContext;

    protected Boolean mPaired;

    public DevicePairingService(Context iContext){
        mContext = iContext;

        mPaired = false;
    }

    /**
     * @param iContext activity context used to pop dialog box
     */
    public void automaticScan(Context iContext){
        Log.d(TAG, "start automaticScan ");

        LinkedList<BleDevice> lDeviceList = getDiscoveredDeviceList();
        EventBus.getDefault().post(new DevicePairingStartDiscoveryNotification(lDeviceList));
    }

    public void deviceConnected(){
        Log.d(TAG, "deviceConnected");

        mPaired = true;
        EventBus.getDefault().post(new DevicePairingNotification(DevicePairingStatus.DEVICE_CONNECTED));

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(PHONE_VIBRATION_DURATION);
    }

    public void deviceDisconnected(){
        Log.d(TAG, "deviceDisconnected");

        EventBus.getDefault().post(new DevicePairingNotification(DevicePairingStatus.DEVICE_DISCONNECTED));

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(PHONE_VIBRATION_DURATION);
    }

    public Boolean isPaired(){
        return mPaired;
    }

    /**
     * @brief discovered device list getter
     *
     * @return discovered device list
     */
    public LinkedList<BleDevice> getDiscoveredDeviceList(){
        LinkedList<BleDevice> lDeviceList = new LinkedList<>();

        BleDeviceIterator it = BleManager.get(mContext).getDevices(BleDeviceState.DISCOVERED);
        for (; it.hasNext(); ) {
            BleDevice lDevice = it.next();
            if (lDevice.is(BleDeviceState.CONNECTED) || lDevice.is(BleDeviceState.CONNECTING) || lDevice.is(BleDeviceState.DISCOVERED) || lDevice.is(BleDeviceState.DISCONNECTED)) {
                if(AuraDevicePairingCompatibility.isCompatibleDevice(lDevice.getName_native())) {
                    lDeviceList.add(lDevice);
                }
            }
        }

        return lDeviceList;
    }

    public LinkedList<DeviceInfo> getDeviceList(){
        return new LinkedList<>();
    }

    public void close() {
        Log.d(TAG, "close Service");
    }

    public void configureAndConnectDevice(BleDevice iBleDevice) {
        Log.d(TAG, "configureAndConnectDevice");
    }

    public boolean disconnectDevices(){
        Log.d(TAG, "disconnect devices");
        return true;
    }
}
