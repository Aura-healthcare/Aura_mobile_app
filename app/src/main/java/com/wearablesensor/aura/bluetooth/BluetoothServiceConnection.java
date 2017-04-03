package com.wearablesensor.aura.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.wearablesensor.aura.bluetooth.BluetoothLeService;
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
