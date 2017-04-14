package com.wearablesensor.aura.data;

import android.content.Context;

import com.wearablesensor.aura.utils.ApplicationScoped;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lecoucl on 29/03/17.
 */

@Module
public class DataRepositoryModule{

    @Provides
    @ApplicationScoped
    RemoteDataRepository providesRemoteDataRepository(Context context) {
        return new RemoteDataDynamoDBRepository(context);
    }

    @Provides
    @ApplicationScoped
    LocalDataRepository providesLocalDataRepository(Context context) {
        return new LocalDataCouchbaseRepository(context);
    }
}
