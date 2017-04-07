package com.wearablesensor.aura.data;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lecoucl on 29/03/17.
 */

@Module
public class DataRepositoryModule{

    @Singleton
    @Provides
    RemoteDataRepository providesRemoteDataRepository(Context context) {
        return new RemoteDataDynamoDBRepository(context);
    }

    @Singleton
    @Provides
    LocalDataRepository providesLocalDataRepository(Context context) {
        return new LocalDataCouchbaseRepository(context);
    }
}
