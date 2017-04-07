package com.wearablesensor.aura.data;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Singleton;

/**
 * Created by lecoucl on 29/03/17.
 */

public interface LocalDataRepository {

    ArrayList<SampleRRInterval> queryRRSample(Date iStartDate, Date iEndDate) throws Exception;
    void saveRRSample(final SampleRRInterval iSampleRR) throws Exception;

    Date queryLastSync() throws Exception;
    void saveLastSync(final Date iLastSync) throws Exception;

}
