package com.wearablesensor.aura;

import android.app.Application;
import android.util.Log;

import com.wearablesensor.aura.data.DataRepositoryComponent;
import com.wearablesensor.aura.data.DaggerDataRepositoryComponent;

/**
 * Created by lecoucl on 29/03/17.
 */
public class AuraApplication extends Application{
    private DataRepositoryComponent mDataRepositoryComponent;

    @Override
    public void onCreate() {
        Log.d("AuraApplication", "Init");
        super.onCreate();

        mDataRepositoryComponent = DaggerDataRepositoryComponent.builder()
                .applicationModule(new ApplicationModule((getApplicationContext())))
                .build();
    }

    public DataRepositoryComponent getDataRepositoryComponent() {
        return mDataRepositoryComponent;
    }
}
