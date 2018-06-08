/**
 * @file DeviceScanDetailsPresenter
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
 */
package com.wearablesensor.aura.device_scan_details;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDeviceDiscoveredNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingEndDiscoveryNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStartDiscoveryNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DeviceScanDetailsPresenter implements DeviceScanDetailsContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private DevicePairingService mBluetoothDevicePairingService;

    private final DeviceScanDetailsContract.View mView;

    private Activity mActivity;

    private boolean mIsScanning;
    private boolean mIsConnecting;

    public DeviceScanDetailsPresenter(DevicePairingService iBluetoothDevicePairingService, Activity iActivity,  DeviceScanDetailsContract.View iView) {
        mBluetoothDevicePairingService = iBluetoothDevicePairingService;
        mView = iView;

        mIsConnecting = false;
        mIsScanning = false;

        mActivity = iActivity;

        mView.setPresenter(this);

        EventBus.getDefault().register(this);
    }


    @Override
    public void start() {

    }

    @Override
    public void finalize() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void startScan() {
        mBluetoothDevicePairingService.automaticScan(mActivity);
    }

    @Override
    public void goToSeizureMonitoring() {
        Intent intent = new Intent(mActivity, SeizureMonitoringActivity.class);
        mActivity.startActivity(intent);

        mActivity.finish();
    }

    @Override
    public void connectDevice(BleDevice iBleDevice) {

        if(mIsConnecting || mIsScanning){
            return;
        }
        Log.d(TAG, "connect Device in presenter");
        mBluetoothDevicePairingService.configureAndConnectDevice(iBleDevice);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePairingEvent(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();
        Log.d(TAG, "DevicePairing onNotificationReceived " + lStatus);

        if (lStatus == DevicePairingStatus.START_DISCOVERY) {
            mIsScanning = true;
            LinkedList<BleDevice> lDeviceList = ((DevicePairingStartDiscoveryNotification) iDevicePairingNotification).getDeviceList();
            mView.displayStartScan(lDeviceList);
        } else if (lStatus == DevicePairingStatus.DEVICE_DISCOVERED) {
            LinkedList<BleDevice> lDeviceList = ((DevicePairingDeviceDiscoveredNotification) iDevicePairingNotification).getDeviceList();
            mView.displayDeviceDiscovered(lDeviceList);
        } else if (lStatus == DevicePairingStatus.END_DISCOVERY) {
            mIsScanning = false;
            LinkedList<BleDevice> lDeviceList = ((DevicePairingEndDiscoveryNotification) iDevicePairingNotification).getDeviceList();
            mView.displayEndScan(lDeviceList);

        } else if (lStatus == DevicePairingStatus.START_CONNECTING) {
            mIsConnecting = true;
            mView.displayStartConnecting();
        } else if (lStatus == DevicePairingStatus.DEVICE_CONNECTED) {
            mIsConnecting = false;
            mView.displayDeviceConnected();

            mView.enableStartMonitoring();
        } else if (lStatus == DevicePairingStatus.DEVICE_DISCONNECTED) {
            mView.displayDeviceDisconnected();

            if(!mBluetoothDevicePairingService.isPaired()){
                mView.disableStartMonitoring();
            }
        }
    }

    public void setDevicePairingService(DevicePairingService iDevicePairingService) {
        mBluetoothDevicePairingService = iDevicePairingService;
    }
}