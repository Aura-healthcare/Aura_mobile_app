package com.wearablesensor.aura;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.wearablesensor.aura.data.DataRepositoryComponent;
import com.wearablesensor.aura.data.DaggerDataRepositoryComponent;
import com.wearablesensor.aura.device_pairing.DaggerDevicePairingComponent;
import com.wearablesensor.aura.device_pairing.DevicePairingComponent;
import com.wearablesensor.aura.device_pairing.DevicePairingModule;

/**
 * Created by lecoucl on 29/03/17.
 */
public class AuraApplication extends Application{
    private DataRepositoryComponent mDataRepositoryComponent;
    private DevicePairingComponent mDevicePairingComponent;

    @Override
    public void onCreate() {
        Log.d("AuraApplication", "Init");
        super.onCreate();

        boolean lIsBluetoothLeFeatureSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        BluetoothManager lBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mDataRepositoryComponent = DaggerDataRepositoryComponent.builder()
                .applicationModule(new ApplicationModule((getApplicationContext())))
                .build();

        mDevicePairingComponent = DaggerDevicePairingComponent.builder()
                .applicationModule(new ApplicationModule(getApplicationContext()))
                .devicePairingModule(new DevicePairingModule(lIsBluetoothLeFeatureSupported, lBluetoothManager))
                .build();
    }

    public DataRepositoryComponent getDataRepositoryComponent() {
        return mDataRepositoryComponent;
    }

    public DevicePairingComponent getDevicePairingComponent(){ return mDevicePairingComponent; }
}
