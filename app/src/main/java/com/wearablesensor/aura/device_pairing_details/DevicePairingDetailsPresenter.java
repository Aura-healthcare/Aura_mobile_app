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

package com.wearablesensor.aura.device_pairing_details;

import android.util.Log;

import com.wearablesensor.aura.device_pairing.DeviceInfo;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingBatteryLevelNotification;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingSessionDurationNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingDetailsPresenter implements DevicePairingDetailsContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothDevicePairingService mBluetoothDevicePairingService;

    private final DevicePairingDetailsContract.View mView;

    public DevicePairingDetailsPresenter(BluetoothDevicePairingService iBluetoothDevicePairingService, DevicePairingDetailsContract.View iView){
        mBluetoothDevicePairingService = iBluetoothDevicePairingService;
        mView = iView;

        mView.setPresenter(this);

        EventBus.getDefault().register(this);
    }

    @Override
    public void start() {

        // no service binded to the SeizureMonitoringActivity
        if(mBluetoothDevicePairingService == null){
            mView.failParing();
            return;
        }

        if(mBluetoothDevicePairingService.isPaired()){
            LinkedList<DeviceInfo> lDeviceList = mBluetoothDevicePairingService.getDeviceList();
            mView.successPairing(lDeviceList);

            // initialize battery state
            for (Map.Entry<String, DeviceInfo> lDevice : mBluetoothDevicePairingService.getCachedDevicesInfo().entrySet()) {
                mView.refreshDeviceBatteryLevel(lDevice.getValue());
            }

            mView.refreshSessionDuration(mBluetoothDevicePairingService.getCachedSessionDuration());
        }
        else{
            mView.failParing();
        }
    }


    /**
     * @brief method executed by observer class when receiving a device pairing notification event
     *
     * @param iDevicePairingNotification notification to be processed by observer class
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePairingEvent(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();
        Log.d(TAG, "DevicePairing onNotificationReceived " + lStatus);

        if(lStatus == DevicePairingStatus.DEVICE_CONNECTED || lStatus == DevicePairingStatus.DEVICE_DISCONNECTED){
            if(mBluetoothDevicePairingService == null){
                return;
            }

            LinkedList<DeviceInfo> lDeviceList = mBluetoothDevicePairingService.getDeviceList();
            if(lDeviceList.size() == 0){
                mView.failParing();
            }
            else{
                mView.successPairing(lDeviceList);
            }
        }
        else if(lStatus == DevicePairingStatus.RECEIVED_BATTERY_LEVEL){
            DevicePairingBatteryLevelNotification lDevicePairingNotification = (DevicePairingBatteryLevelNotification) iDevicePairingNotification;
            mView.refreshDeviceBatteryLevel(lDevicePairingNotification.getDeviceInfo());
        }
        else if(lStatus == DevicePairingStatus.SESSION_DURATION){
            DevicePairingSessionDurationNotification lDevicePairingNotification = (DevicePairingSessionDurationNotification) iDevicePairingNotification;
            mView.refreshSessionDuration(lDevicePairingNotification.getSessionDuration());
        }
    }

    @Override
    public void startScanning(){
        EventBus.getDefault().post(new NavigationNotification(NavigationConstants.NAVIGATION_DEVICE_SCANNING));
    }

    @Override
    public void finalize(){
        EventBus.getDefault().unregister(this);
    }

    public void setDevicePairingService(BluetoothDevicePairingService iDevicePairingService) {
        mBluetoothDevicePairingService = iDevicePairingService;
    }
}
