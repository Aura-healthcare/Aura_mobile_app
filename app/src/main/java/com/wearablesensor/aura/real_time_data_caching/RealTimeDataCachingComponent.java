package com.wearablesensor.aura.real_time_data_caching;

import com.wearablesensor.aura.ApplicationModule;
import com.wearablesensor.aura.data.DataRepositoryModule;
import com.wearablesensor.aura.device_pairing.DevicePairingComponent;
import com.wearablesensor.aura.utils.ApplicationScoped;

import dagger.Component;

/**
 * Created by lecoucl on 13/04/17.
 */

@ApplicationScoped
@Component(dependencies = {DevicePairingComponent.class}, modules = {DataRepositoryModule.class, ApplicationModule.class})
public interface RealTimeDataCachingComponent {
    RealTimeDataCachingService realTimeDataCachingService();
}

