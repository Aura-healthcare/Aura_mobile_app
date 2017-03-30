package com.wearablesensor.aura.data;

import android.content.Context;
import android.util.Log;

import javax.inject.Singleton;

/**
 * Created by lecoucl on 29/03/17.
 */
@Singleton
public class RemoteDataRepository {
    private final String TAG = this.getClass().getSimpleName();

    public RemoteDataRepository(Context iContext) {
        Log.d(TAG, "Init LocalDataRepository");
    }
}
