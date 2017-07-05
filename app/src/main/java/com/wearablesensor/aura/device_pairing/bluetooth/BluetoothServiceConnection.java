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
 */

package com.wearablesensor.aura.device_pairing.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.wearablesensor.aura.AuraApplication;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.GattManager;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattSetNotificationOperation;

import java.util.LinkedList;

public class BluetoothServiceConnection implements ServiceConnection {
    private final String TAG = this.getClass().getSimpleName();

    private BluetoothLeService mBluetoothLeService;
    private LinkedList<BluetoothDevice> mDeviceList;

    private BluetoothDevicePairingService mDevicePairingService;

    public BluetoothServiceConnection(BluetoothDevicePairingService iDevicePairingService){
        mDevicePairingService = iDevicePairingService;
        mBluetoothLeService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        if (!mBluetoothLeService.initialize()) {
            mDevicePairingService.endPairing();
        }
        // Automatically connects to the devices upon successful start-up initialization.
        mBluetoothLeService.connect(mDeviceList);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBluetoothLeService.close();

        mDevicePairingService.endPairing();
    }

    public void setDeviceList(LinkedList<BluetoothDevice> iDeviceList) {
        mDeviceList = iDeviceList;
    }

}
