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

package com.wearablesensor.aura.device_pairing.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;

/**
 * Created by lecoucl on 31/03/17.
 */
public class BluetoothServiceConnection implements ServiceConnection {
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAdress;

    private BluetoothDevicePairingService mDevicePairingService;

    public BluetoothServiceConnection(BluetoothDevicePairingService iDevicePairingService){
        mDevicePairingService = iDevicePairingService;
        mBluetoothLeService = null;
        mDeviceAdress = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        if (!mBluetoothLeService.initialize()) {
            mDevicePairingService.endPairing();
        }
        // Automatically connects to the device upon successful start-up initialization.
        mBluetoothLeService.connect(mDeviceAdress);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBluetoothLeService = null;
        mDeviceAdress = null;
        mDevicePairingService.endPairing();
    }

    public void startHeartProfileMonitoring(){
        BluetoothGattCharacteristic lBleHeartRateCharacteristic = mBluetoothLeService.getBluetoothGattHeartRateCharacteristic();
        mBluetoothLeService.setCharacteristicNotification(lBleHeartRateCharacteristic, true);
    }

    public void setDeviceAdress(String iDeviceAdress){
        mDeviceAdress = iDeviceAdress;
    }

}
