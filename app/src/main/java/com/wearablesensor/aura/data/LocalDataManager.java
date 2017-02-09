package com.wearablesensor.aura.data;

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


import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lecoucl on 13/12/16.
 */

public class LocalDataManager
{
    private final static String TAG = LocalDataManager.class.getSimpleName();
    private Manager mCouchBaseManager;
    private Database mDB;
    private DatabaseOptions mDBOptions;

    private View mRRSamplesView;

    private final static String DB_NAME = "dblocalepi";
    private final static String RR_DOCUMENT_NAME = "rrDocumentV2";

    private final static String USER_PREFS_DOCUMENT = "userPrefsDocument";
    private final static String LAST_SYNCHRO_PREF = "lastSync";

    private final static String RR_SAMPLES_VIEW_NAME = "rrSamplesView";

    /** private constructor */
    public LocalDataManager() {}

    protected void init(Context iApplicationContext) {
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

        mRRSamplesView = mDB.getView(RR_SAMPLES_VIEW_NAME);
        mRRSamplesView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                for (Map.Entry<String, Object> entry : document.entrySet())
                {
                    if(entry.getValue() instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> rr = (LinkedHashMap<String, Object>) entry.getValue();
                        if (rr.get("timestamp") instanceof String) {
                            emitter.emit(DateIso8601Mapper.getDate((String) rr.get("timestamp")), rr);
                        }
                    }
                }
            }
        },"1.0");
    }

    protected void saveRRData(final SampleRRInterval iSampleRR){
        Document rrDocument = null;
        try {
            rrDocument = mDB.getDocument(RR_DOCUMENT_NAME);
            android.util.Log.d(TAG, "Get Document - id:" + rrDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
        }

        android.util.Log.d(TAG, "Start Recording - iSample:" + iSampleRR.getUuid() + " " + iSampleRR.getTimestamp());

        try {
            rrDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(iSampleRR.getUuid(), iSampleRR);

                    newRevision.setUserProperties(properties);
                    android.util.Log.d(TAG, "RecordSuccess");
                    return true;
                }
            });
        } catch (CouchbaseLiteException e) {
            android.util.Log.d(TAG, "RecordFail " + e.getMessage());
        }

        android.util.Log.d(TAG, "RecordHistory nbItems:" + rrDocument.getProperties().size());
    }

    protected ArrayList<SampleRRInterval> getRRData(Date iStartDate, Date iEndDate) throws CouchbaseLiteException {

        android.util.Log.d(TAG, "get RR data");
        Document rrDocument = null;
        try {
            rrDocument = mDB.getDocument(RR_DOCUMENT_NAME);
            android.util.Log.d(TAG, "Get Document - id:" + rrDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        ArrayList<SampleRRInterval> samplesRr = new ArrayList<SampleRRInterval>();

        try {
            Query query = mRRSamplesView.createQuery();
            query.setStartKey(iStartDate);
            query.setEndKey(iEndDate);
            QueryEnumerator queryEnum = query.run();
            android.util.Log.d(TAG,"Query size: " + queryEnum.getCount());

            for (Iterator<QueryRow> it = queryEnum; it.hasNext(); ) {
                QueryRow row=it.next();
                //TODO: implement a mapper to SampleRRInterval
                SampleRRInterval sampleRr;
                if(row.getValue() instanceof  LinkedHashMap) {
                    LinkedHashMap<String, Object> jsonMap = (LinkedHashMap<String, Object>) row.getValue();
                    sampleRr = new SampleRRInterval((String) jsonMap.get("uuid"), (String) jsonMap.get("user"), (String) jsonMap.get("deviceAdress"), (String) jsonMap.get("timestamp"), (Integer) jsonMap.get("rr"));
                }
                else if(row.getValue() instanceof LazyJsonObject) {
                    LazyJsonObject jsonMap = (LazyJsonObject) row.getValue();
                    sampleRr = new SampleRRInterval((String) jsonMap.get("uuid"), (String) jsonMap.get("user"), (String) jsonMap.get("deviceAdress"), (String) jsonMap.get("timestamp"), (Integer) jsonMap.get("rr"));
                }
                else{
                    sampleRr = (SampleRRInterval) row.getValue();
                }
                samplesRr.add(sampleRr);
                android.util.Log.d(TAG,"Document contents: " + row.getKey() + " "+ row.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        android.util.Log.d(TAG, "Samples count - " + samplesRr.size());
        return samplesRr;
    }

    protected void saveLastSync(Date iLastSync) throws CouchbaseLiteException {

        Document userPrefsDocument = null;

        final String iLastSyncStr = DateIso8601Mapper.getString(iLastSync);
        try {
            userPrefsDocument = mDB.getDocument(USER_PREFS_DOCUMENT);
            android.util.Log.d(TAG, "Get Document - id:" + userPrefsDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        try {
            userPrefsDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(LAST_SYNCHRO_PREF, iLastSyncStr);

                    newRevision.setUserProperties(properties);
                    android.util.Log.d(TAG, "RecordSuccess");
                    return true;
                }
            });
        } catch (CouchbaseLiteException e) {
            android.util.Log.d(TAG, "RecordFail " + e.getMessage());
            throw e;
        }

    }

    protected Date getLastSync(){
        Document userPrefsDocument = null;

        try {
            userPrefsDocument = mDB.getDocument(USER_PREFS_DOCUMENT);
            android.util.Log.d(TAG, "Get Document - id:" + userPrefsDocument.getId());
            return  DateIso8601Mapper.getDate((String)userPrefsDocument.getProperty(LAST_SYNCHRO_PREF));

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }


    }

    protected void clean(){
        Document lRrDocument = null;
        Document lUserPrefsDocument = null;

        try {
            lRrDocument = mDB.getDocument(RR_DOCUMENT_NAME);
            lUserPrefsDocument = mDB.getDocument(USER_PREFS_DOCUMENT);

        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            lRrDocument.delete();
            lUserPrefsDocument.delete();
            android.util.Log.d(TAG, "Documents deleted" );

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}