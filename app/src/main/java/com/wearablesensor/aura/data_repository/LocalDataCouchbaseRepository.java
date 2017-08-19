/**
 * @file
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 *
 * LocalDataCouchbaseRepository is a local data storage implementation relying on Couchbase mobile
 * framework <https://developer.couchbase.com/documentation/mobile/current/installation/index.html>
 * The framework saves data in a NoSql database with a multiple documents architecture.
 * It provides good read/write performances, data encryption using sql_cipher library and automatic
 * syncing with remote database using Couchbase server (not implemented here).
 *
 */

package com.wearablesensor.aura.data_repository;

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
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class LocalDataCouchbaseRepository implements LocalDataRepository {
    private final String TAG = this.getClass().getSimpleName();

    private Manager mCouchBaseManager; /** couchbase manager */

    private Database mPhysioSignalDB; /** local couchbase database */
    private Database mSensitiveEventDB;
    private DatabaseOptions mDBOptions; /** local couchbase database options */

    private View mPhysioSignalSamplesView; /** pre-formatted view to query physiological data samples on a date interval */
    private View mSensitiveEventsView; /** pre-formatted view to sensitive event on a date interval */

    private final static String DB_PHYSIO_SIGNAL_NAME = "dbaura_physio_signal"; /** couchbase physio signal database name */
    private final static String DB_SESSION_NAME = "dbaura_session"; /** couchbase session database name */
    private final static String DB_SENSITIVE_EVENT_NAME = "dbaura_sensitive_event"; /** couchbase sensitive event DB name */

    private final static String MY_DOCUMENT="myDocument"; /** document storing reported sensitive events */

    private final static String UUID_PARAM= "uuid"; /** UUID param used to identify model */
    private final static String TYPE_PARAM = "type"; /** type param used to specify model  */
    private final static String TIMESTAMP_PARAM = "timestamp"; /** timestamp param used to map couchbase object to model */
    private final static String USER_PARAM = "user"; /** user param used to map couchbase object to model */

    private final static String RR_INTERVAL_PARAM = "rrInterval"; /** rrInterval param used to map couchbase json to PhysioSignal model */
    private final static String DEVICE_ADRESS_PARAM = "deviceAdress"; /** deviceAdress param used to map couchbase json to PhysioSignal model */

    private final static String TEMPERATURE_PARAM = "temperature";
    private final static String SENSOR_OUTPUT_FREQUENCY_PARAM = "sensorOutputFrequency";
    private final static String ELETRO_DERMAL_ACTIVITY_PARAM = "electroDermalActivity";

    private final static String SENSITIVE_EVENT_TIMESTAMP_PARAM = "sensitiveEventTimestamp"; /** event timestamp */
    private final static String COMMENT_PARAM = "comments"; /** comments giving more details about sensitive events */

    private final static String RR_SAMPLES_VIEW = "rrSamplesView"; /** RR Sample view name */
    private final static String SENSITIVE_EVENTS_VIEW = "sensitiveEventView"; /** sensitive event view */
    private ArrayList<PhysioSignalModel> mPhysioSignalCache; /* temporary storage in an array to avoid call overhead to local data repository */

    /**
     * @brief constructor
     *
     * @param iApplicationContext application context
     */

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
            mPhysioSignalDB = mCouchBaseManager.openDatabase(DB_PHYSIO_SIGNAL_NAME, mDBOptions);
            mSensitiveEventDB= mCouchBaseManager.openDatabase(DB_SENSITIVE_EVENT_NAME, mDBOptions);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // create Couchbase view used to query RRSamples
        mPhysioSignalSamplesView = mPhysioSignalDB.getView(RR_SAMPLES_VIEW);
        mPhysioSignalSamplesView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                for (Map.Entry<String, Object> entry : document.entrySet())
                {
                    if(entry.getValue() instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> lPhysioSignal = (LinkedHashMap<String, Object>) entry.getValue();
                        emitter.emit(DateIso8601Mapper.getDate((String) lPhysioSignal.get(TIMESTAMP_PARAM)), lPhysioSignal);
                    }
                }
            }
        },"1.0");

        mSensitiveEventsView = mSensitiveEventDB.getView(SENSITIVE_EVENTS_VIEW);
        mSensitiveEventsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    if (entry.getValue() instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> lSensitiveEvent = (LinkedHashMap<String, Object>) entry.getValue();
                        emitter.emit(DateIso8601Mapper.getDate((String) lSensitiveEvent.get(TIMESTAMP_PARAM)), lSensitiveEvent);
                    }
                }
            }
        },"1.0");

        mPhysioSignalCache = new ArrayList<PhysioSignalModel>();
    }

    /**
     * @brief query a list of physiological data samples in a time range [iStartDate, iEndDate]
     *
     * @param iStartDate collected data samples timestamps are newer than iStartData
     * @param iEndDate collected data samples timestamp is older than iEndData
     *
     * @return a list of physiological data samples
     *
     * @throws Exception
     */

    @Override
    public ArrayList<PhysioSignalModel> queryPhysioSignalSamples(Date iStartDate, Date iEndDate) throws Exception {

        ArrayList<PhysioSignalModel> lPhysioSignalSamples = new ArrayList<PhysioSignalModel>();

        try {
            Query query = mPhysioSignalSamplesView.createQuery();

            query.setStartKey(iStartDate);
            query.setEndKey(iEndDate);
            QueryEnumerator queryEnum = query.run();
            Log.d(TAG,"Query size: " + queryEnum.getCount());

            for (Iterator<QueryRow> it = queryEnum; it.hasNext(); ) {
                QueryRow row=it.next();
                //TODO: implement a mapper to RRIntervalModel - need to understand why Couchbase does not systematically return same object type
                PhysioSignalModel lPhysioSignalSample;
                if(row.getValue() instanceof LazyJsonObject){
                    LazyJsonObject jsonMap = (LazyJsonObject) row.getValue();
                    if( ((String) jsonMap.get(TYPE_PARAM)).equals(RRIntervalModel.RR_INTERVAL_TYPE) ){
                        lPhysioSignalSample = new RRIntervalModel((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(DEVICE_ADRESS_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (Integer) jsonMap.get(RR_INTERVAL_PARAM));
                        lPhysioSignalSamples.add(lPhysioSignalSample);
                    }
                    else if( ((String) jsonMap.get(TYPE_PARAM)).equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE) ){
                        lPhysioSignalSample = new SkinTemperatureModel((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(DEVICE_ADRESS_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), ((Double) jsonMap.get(TEMPERATURE_PARAM)).floatValue());
                        lPhysioSignalSamples.add(lPhysioSignalSample);
                    }
                    else if( ((String) jsonMap.get(TYPE_PARAM)).equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY) ){
                        lPhysioSignalSample = new ElectroDermalActivityModel((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(DEVICE_ADRESS_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (int) jsonMap.get(SENSOR_OUTPUT_FREQUENCY_PARAM), (int) jsonMap.get(ELETRO_DERMAL_ACTIVITY_PARAM));
                        lPhysioSignalSamples.add(lPhysioSignalSample);
                    }
                }
                Log.d(TAG,"Document contents: " + row.getKey() + " "+ row.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        Log.d(TAG, "Samples count - " + lPhysioSignalSamples.size());
        return lPhysioSignalSamples;
    }

    /**
     * @brief save a batch of physiological signal sample in the local storage
     *
     * @param iPhysioSignalSamples physiological data list to be stored
     *
     * @throws Exception
     */
    @Override
    public void savePhysioSignalSamples(final ArrayList<PhysioSignalModel> iPhysioSignalSamples) throws Exception{
        Document rrDocument = null;
        try {
            rrDocument = mPhysioSignalDB.getDocument(MY_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + rrDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        Log.d(TAG, "Start Recording - iSamples:" + iPhysioSignalSamples.size());

        try {
            rrDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    for(int i = 0; i < iPhysioSignalSamples.size(); i++){
                        properties.put(iPhysioSignalSamples.get(i).getUuid(), iPhysioSignalSamples.get(i));
                    }

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

    /**
     * @brief cache a physiological data sample in the heap and periodically clear cache and save data to local
     * storage
     *
     * @param iPhysioSignal physiological data sample to be cached
     */
    public void cachePhysioSignalSample(PhysioSignalModel iPhysioSignal) throws Exception{
        if(mPhysioSignalCache.size() < 100) {
            mPhysioSignalCache.add(iPhysioSignal);
        }
        else {
            try {
                clearCache();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }
    }

    /**
     * @brief query a list of seizure events in a time range [iStartDate, iEndDate]
     *
     * @param iStartDate collected data sample timestamps are newer than iStartData
     * @param iEndDate collected data samples timestamp is older than iEndData
     *
     * @return a list of seizure events
     *
     * @throws Exception
     */
    @Override
    public ArrayList<SeizureEventModel> querySeizures(Date iStartDate, Date iEndDate) throws Exception {

        ArrayList<SeizureEventModel> lSensitiveEventSamples = new ArrayList<SeizureEventModel>();

        try {
            Query query = mSensitiveEventsView.createQuery();

            query.setStartKey(iStartDate);
            query.setEndKey(iEndDate);

            QueryEnumerator queryEnum = query.run();
            Log.d(TAG,"Query size: " + queryEnum.getCount());

            for (Iterator<QueryRow> it = queryEnum; it.hasNext(); ) {
                QueryRow row=it.next();
                //TODO: implement a mapper to SeizureEventModel - need to understand why Couchbase does not systematically return same object type
                SeizureEventModel lSensitiveEvent;
                if(row.getValue() instanceof  LinkedHashMap) {
                    LinkedHashMap<String, Object> jsonMap = (LinkedHashMap<String, Object>) row.getValue();
                    lSensitiveEvent = new SeizureEventModel((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (String) jsonMap.get(SENSITIVE_EVENT_TIMESTAMP_PARAM), (String) jsonMap.get(COMMENT_PARAM));
                }
                else if(row.getValue() instanceof LazyJsonObject) {
                    LazyJsonObject jsonMap = (LazyJsonObject) row.getValue();
                    lSensitiveEvent = new SeizureEventModel((String) jsonMap.get(UUID_PARAM), (String) jsonMap.get(USER_PARAM), (String) jsonMap.get(TIMESTAMP_PARAM), (String) jsonMap.get(SENSITIVE_EVENT_TIMESTAMP_PARAM), (String) jsonMap.get(COMMENT_PARAM));
                }
                else{
                    lSensitiveEvent = (SeizureEventModel) row.getValue();
                }
                lSensitiveEventSamples.add(lSensitiveEvent);
                Log.d(TAG,"Document contents: " + row.getKey() + " "+ row.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        Log.d(TAG, "Samples count - " + lSensitiveEventSamples.size());
        return lSensitiveEventSamples;
    }

    /**
     * @brief save a seizure event
     *
     * @param iSeizureEventModel seizure event
     *
     * @throws Exception
     */
    @Override
    public void saveSeizure(final SeizureEventModel iSeizureEventModel) throws Exception {
        Document lSensitiveEventDocument = null;
        try {
            lSensitiveEventDocument = mSensitiveEventDB.getDocument(MY_DOCUMENT);
            Log.d(TAG, "Get Document - id:" + lSensitiveEventDocument.getId());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        try {
            lSensitiveEventDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(iSeizureEventModel.getUuid(), iSeizureEventModel);

                    newRevision.setUserProperties(properties);
                    Log.d(TAG, "RecordSuccess");
                    return true;
                }
            });
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "RecordFail " + e.getMessage());
            throw e;
        }

        Log.d(TAG, "RecordHistory nbItems:" + lSensitiveEventDocument.getProperties().size());
    }

    /**
     * @brief clear cache and store data from heap to local data storage
     *
     */
    public void clearCache() throws Exception{
        savePhysioSignalSamples(mPhysioSignalCache);
        mPhysioSignalCache.clear();
    }

    /**
     * @brief clear entirely the local data storage
     */
    @Override
    public void clear() {
        Document lPhysioSignalDocument = null;
        Document lSensitiveEventDocument = null;
        try {
            lPhysioSignalDocument = mPhysioSignalDB.getDocument(MY_DOCUMENT);
            lSensitiveEventDocument = mSensitiveEventDB.getDocument(MY_DOCUMENT);


        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            lPhysioSignalDocument.delete();
            lSensitiveEventDocument.delete();
            android.util.Log.d(TAG, "Documents deleted" );

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
