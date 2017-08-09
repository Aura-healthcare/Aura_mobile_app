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
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingConnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import java.util.LinkedList;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingDetailsPresenter extends DevicePairingServiceObserver implements DevicePairingDetailsContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final BluetoothDevicePairingService mBluetoothDevicePairingService;

    private final DevicePairingDetailsContract.View mView;

    public DevicePairingDetailsPresenter(BluetoothDevicePairingService iBluetoothDevicePairingService, DevicePairingDetailsContract.View iView){
        mBluetoothDevicePairingService = iBluetoothDevicePairingService;
        mView = iView;

        mView.setPresenter(this);

        listenDevicePairingService();
    }

    @Override
    public void start() {
        if(mBluetoothDevicePairingService.isPaired()){
            LinkedList<DeviceInfo> lDeviceList = mBluetoothDevicePairingService.getDeviceList();
            mView.successPairing(lDeviceList);
        }
        else{
            mView.failParing();
        }
    }

    @Override
    public void listenDevicePairingService() {
        mBluetoothDevicePairingService.addObserver(this);
    }

    @Override
    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();
        Log.d(TAG, "DevicePairing onNotificationReceived " + lStatus);

        if(lStatus == DevicePairingStatus.CONNECTED){
            DevicePairingConnectedNotification lDevicePairingNotification = (DevicePairingConnectedNotification) iDevicePairingNotification;
            mView.successPairing(mBluetoothDevicePairingService.getDeviceList());
        }
        else if(lStatus == DevicePairingStatus.DISCONNECTED){
            mView.failParing();
        }
    }
}
