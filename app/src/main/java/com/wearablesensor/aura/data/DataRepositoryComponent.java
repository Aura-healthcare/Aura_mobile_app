package com.wearablesensor.aura.data;

import com.wearablesensor.aura.ApplicationModule;
import com.wearablesensor.aura.utils.ApplicationScoped;

import dagger.Component;

/**
 * Created by lecoucl on 29/03/17.
 */

@ApplicationScoped
@Component(modules = {DataRepositoryModule.class, ApplicationModule.class})
public interface DataRepositoryComponent{

    LocalDataRepository localDataRepository();
    RemoteDataRepository remoteDataRepository();
}
