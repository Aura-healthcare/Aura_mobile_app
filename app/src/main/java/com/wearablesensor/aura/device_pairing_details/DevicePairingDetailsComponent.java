package com.wearablesensor.aura.device_pairing_details;

import com.wearablesensor.aura.device_pairing.DevicePairingComponent;
import com.wearablesensor.aura.utils.FragmentScoped;
import com.wearablesensor.aura.SeizureMonitoringActivity;

import dagger.Component;

/**
 * Created by lecoucl on 07/04/17.
 */

@FragmentScoped
@Component(dependencies = DevicePairingComponent.class, modules = DevicePairingDetailsPresenterModule.class)
public interface DevicePairingDetailsComponent {
    void inject(SeizureMonitoringActivity activity);
}
