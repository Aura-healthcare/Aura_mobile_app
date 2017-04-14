package com.wearablesensor.aura.device_pairing;

import android.bluetooth.BluetoothManager;

import com.wearablesensor.aura.utils.ApplicationScoped;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lecoucl on 30/03/17.
 */

@Module
public class DevicePairingModule {

    private final boolean mIsBluetoothLeFeatureSupported;
    private final BluetoothManager mBluetoothManager;

    public DevicePairingModule(boolean iIsBluetoothFeatureLeSupported, BluetoothManager iBluetoothManager){
        mIsBluetoothLeFeatureSupported = iIsBluetoothFeatureLeSupported;
        mBluetoothManager = iBluetoothManager;
    }

    @Provides
    boolean providesIsBluetoothLeFeatureSupported() {
        return mIsBluetoothLeFeatureSupported;
    }

    @Provides
    @Singleton
    BluetoothManager providesBluetoothManager(){
        return mBluetoothManager;
    }
}
