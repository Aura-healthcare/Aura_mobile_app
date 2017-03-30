package com.wearablesensor.aura.device_pairing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ServiceConnection;

import com.wearablesensor.aura.bluetooth.BluetoothLeService;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by lecoucl on 30/03/17.
 */
public class DevicePairingService {
    private String mPairedDeviceName;
    private String mPairedDeviceAddress;

    private ServiceConnection mDevicePairingService;
    private BroadcastReceiver mDataReceiver;

    private Boolean mConnected;
    private Boolean mScanning;

    // Bluetooth pairing members
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBluetoothDeviceList;

    @Inject
    public DevicePairingService(/*ServiceConnection iServiceConnection, BroadcastReceiver iDataReceiver, BluetoothAdapter iBluetoothAdapter, BluetoothLeService iBluetoothLeService*/){
        mPairedDeviceName = null;
        mPairedDeviceAddress = null;

        mConnected = false;
        mScanning = false;

        mBluetoothDeviceList = new ArrayList<BluetoothDevice>();
    }

    public void scanDevices(){

    }

    public void connectDevice(){

    }
}
