package com.wearablesensor.aura.device_pairing;

import com.wearablesensor.aura.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by lecoucl on 30/03/17.
 */

@Singleton
@Component(modules = {DevicePairingModule.class, ApplicationModule.class})
public interface DevicePairingComponent {
    BluetoothDevicePairingService devicePairingService();
}
