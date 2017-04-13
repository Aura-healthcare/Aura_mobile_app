package com.wearablesensor.aura.device_pairing_details;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lecoucl on 07/04/17.
 */
@Module
public class DevicePairingDetailsPresenterModule {
    private final DevicePairingDetailsContract.View mView;

    public DevicePairingDetailsPresenterModule(DevicePairingDetailsContract.View iView){
        mView = iView;
    }

    @Provides
    DevicePairingDetailsContract.View provideDevicePairingInfoView(){
        return mView;
    }
}


