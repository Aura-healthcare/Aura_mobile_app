package com.wearablesensor.aura.data;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.support.LazyJsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Created by lecoucl on 01/04/17.
 */

@Singleton
public class LocalDataCouchbaseRepository implements LocalDataRepository {
    private final String TAG = this.getClass().getSimpleName();

    private Manager mCouchBaseManager;
    private Database mDB;
    private DatabaseOptions mDBOptions;

    private View mRRSamplesView;

    private final static String DB_NAME = "dbaura";

    private final static String PHYSIO_SIGNAL_DOCUMENT= "physioSignalDocument";
    private final static String UUID_PARAM= "uuid";
    private final static String TIMESTAMP_PARAM = "timestamp";
    private final static String USER_PARAM = "user";
    private final static String RR_PARAM = "rr";
    private final static String DEVICE_ADRESS_PARAM = "deviceAdress";

    private final static String USER_PREFS_DOCUMENT = "userPrefsDocument";
    private final static String LAST_SYNC_PARAM = "lastSync";

    private final static String RR_SAMPLES_VIEW = "rrSamplesView";

    @Inject
    public LocalDataCouchbaseRepository(Context iApplicationContext){
        Log.d(TAG, "Local data CouchBase repository init");
        mDBOptions = new DatabaseOptions();
        mDBOptions.setCreate(true);

        //TODO:implement encryption
        /*if (mEncryptionEnabled) {
            options.setEncryptionKey(key);
        }*/

        try {
            mCouchBaseManager = new Manager(new AndroidContext(iApplicationContext), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mDB = mCouchBaseManager.openDatabase(DB_NAME, mDBOptions);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // create Couchbase view used to query RRSamples
        mRRSamplesView = mDB.getView(RR_SAMPLES_VIEW);
        mRRSamplesView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                for (Map.Entry<String, Object> entry : document.entrySet())
                {
                    if(entry.getValue() instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> rr = (LinkedHashMap<String, Object>) entry.getValue();
                        if (rr.get(TIMESTAMP_PARAM) instanceof String) {
                            emitter.emit(DateIso8601Mapper.getDate((String) rr.get(TIMESTAMP_PARAM)), rr);
                        }
                    }
                }
            }
        },"1.0");
    }


    @Override
    public ArrayList<SampleRRInterval> queryRRSample(Date iStartDate, Date iEndDate) throws Exception {
        Log.d(TAG, "start query RR Samples");
        Document lRrDocument = null;
        try {
            lRrDocument = mDB.getDocument(PHYSIO_SIGNAL_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + lRrDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        ArrayList<SampleRRInterval> lRrSamples = new ArrayList<SampleRRInterval>();

        try {
            Query query = mRRSamplesView.createQuery();
            query.setStartKey(iStartDate);
            query.setEndKey(iEndDate);
            QueryEnumerator queryEnum = query.run();
            Log.d(TAG,"Query size: " + queryEnum.getCount());

            for (Iterator<QueryRow> it = queryEnum; it.hasNext(); ) {
                QueryRow row=it.next();
                //TODO: implement a mapper to SampleRRInterval - need to understand why Couchbase does not systematically return same object type
                SampleRRInterval lRrSample;
                if(row.getValue() instanceof  LinkedHashMap) {
                    LinkedHashMap<String, Object> jsonMap = (LinkedHashMap<String, Object>) row.getValue();
                    lRrSample = new SampleRRInterval((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(DEVICE_ADRESS_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (Integer) jsonMap.get(RR_PARAM));
                }
                else if(row.getValue() instanceof LazyJsonObject) {
                    LazyJsonObject jsonMap = (LazyJsonObject) row.getValue();
                    lRrSample = new SampleRRInterval((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(DEVICE_ADRESS_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (Integer) jsonMap.get(RR_PARAM));
                }
                else{
                    lRrSample = (SampleRRInterval) row.getValue();
                }
                lRrSamples.add(lRrSample);
                Log.d(TAG,"Document contents: " + row.getKey() + " "+ row.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        Log.d(TAG, "Samples count - " + lRrSamples.size());
        return lRrSamples;
    }

    @Override
    public void saveRRSample(final SampleRRInterval iSampleRR) throws Exception{
        Document rrDocument = null;
        try {
            rrDocument = mDB.getDocument(PHYSIO_SIGNAL_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + rrDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        Log.d(TAG, "Start Recording - iSample:" + iSampleRR.getUuid() + " " + iSampleRR.getTimestamp());

        try {
            rrDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(iSampleRR.getUuid(), iSampleRR);

                    newRevision.setUserProperties(properties);
                    Log.d(TAG, "RecordSuccess");
                    return true;
                }
            });
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "RecordFail " + e.getMessage());
            throw e;
        }

        Log.d(TAG, "RecordHistory nbItems:" + rrDocument.getProperties().size());
    }

    @Override
    public Date queryLastSync() throws Exception{
        Document userPrefsDocument = null;

        try {
            userPrefsDocument = mDB.getDocument(USER_PREFS_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + userPrefsDocument.getId());
            return  DateIso8601Mapper.getDate((String)userPrefsDocument.getProperty(LAST_SYNC_PARAM));

        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void saveLastSync(final Date iLastSync) throws Exception {
        Document userPrefsDocument = null;

        final String iLastSyncStr = DateIso8601Mapper.getString(iLastSync);
        try {
            userPrefsDocument = mDB.getDocument(USER_PREFS_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + userPrefsDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        try {
            userPrefsDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(LAST_SYNC_PARAM, iLastSyncStr);

                    newRevision.setUserProperties(properties);
                    Log.d(TAG, "RecordSuccess");
                    return true;
                }
            });
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "RecordFail " + e.getMessage());
            throw e;
        }
    }
}
