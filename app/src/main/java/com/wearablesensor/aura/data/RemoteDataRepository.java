package com.wearablesensor.aura.data;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Singleton;

/**
 * Created by lecoucl on 29/03/17.
 */
@Singleton
public interface RemoteDataRepository {

    void saveRRSample(final ArrayList<SampleRRInterval> iRrSamples) throws Exception;

    Date queryLastSync() throws Exception;
    void saveLastSync(final Date iLastSync) throws Exception;
}
