package com.wearablesensor.aura;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lecoucl on 29/03/17.
 */
@Module
public class ApplicationModule {

    private final Context mContext;

    public ApplicationModule(Context iContext) {
        mContext = iContext;
    }

    @Provides
    Context providesContext(){
        return mContext;
    }
}