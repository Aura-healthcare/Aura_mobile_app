package com.wearablesensor.aura.data;

import com.wearablesensor.aura.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by lecoucl on 29/03/17.
 */
@Singleton
@Component(modules = {DataRepositoryModule.class, ApplicationModule.class})
public interface DataRepositoryComponent{

    DataRepository getDataRepository();
}
