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
 * LocalDataFileRepository is a local data storage implementation relying on Couchbase mobile
 * framework <https://developer.couchbase.com/documentation/mobile/current/installation/index.html>
 * The framework saves data in a NoSql database with a multiple documents architecture.
 * It provides good read/write performances, data encryption using sql_cipher library and automatic
 * syncing with remote database using Couchbase server (not implemented here).
 *
 */

package com.wearablesensor.aura.data_repository;

import android.content.Context;
import android.util.Log;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


public class LocalDataFileRepository implements LocalDataRepository {
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private CacheStorage mCache; /* temporary storage in a cache to avoid call overhead to local data repository */

    private SeizureEventModel mSeizureCache; /* temporary storage of a seizure */
    private FileStorage mFileStorage; /* file storage for permanent storage on mobile device */

    /**
     * @brief constructor
     *
     * @param iApplicationContext application context
     */

    public LocalDataFileRepository(Context iApplicationContext){
        Log.d(TAG, "Local data file repository init");

        mApplicationContext = iApplicationContext;
        mCache = new CacheStorage();
        mFileStorage = new FileStorage(iApplicationContext);

        mSeizureCache = null;
    }

    /**
     * @brief query a list of physiological data samples from a file
     *
     * @param iFilename file storing the physio signal samples
     *
     * @return a list of physiological data samples as a JSON compact string
     *
     * @throws Exception
     */

    @Override
    public String queryRawFileContent(String iFilename) throws Exception {
        return mFileStorage.queryRawContent(iFilename);
    }

    /**
     * @brief cache a physiological data sample in the heap and periodically clear cache and save data to local
     * storage
     *
     * @param iPhysioSignal physiological data sample to be cached
     */
    public void cachePhysioSignalSample(PhysioSignalModel iPhysioSignal) throws Exception{

        CacheStorage.CacheStatus status = mCache.add(iPhysioSignal);

        if(status == CacheStorage.CacheStatus.FULL) {
            ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSignals = mCache.getChannel(iPhysioSignal.getType());
            mFileStorage.savePhysioSignals(lPhysioSignals);
            mCache.clearChannel(iPhysioSignal.getType());
        }
    }

    /**
     * @brief save a seizure event
     *
     * @throws Exception
     */
    @Override
    public void saveSeizure() throws Exception {
        if(mSeizureCache != null){
            mFileStorage.saveSeizure(mSeizureCache);
        }
    }

    /**
     * @brief clear seizure cache
     */
    @Override
    public void clearSeizure(){
        mSeizureCache = null;
    }

    /**
     * @brief force saving physio signal samples from cache to file
     *
     * @throws Exception
     */
    @Override
    public void forceSavingPhysioSignalSamples() throws Exception {
        Set<String> lChannelList = mCache.getCacheChannelIds();

        for(String lChannel : lChannelList){
            mFileStorage.savePhysioSignals(mCache.getChannel(lChannel));
        }

        mCache.clear();
    }

    /**
     * @brief remove a batch of physiological signal samples from the local storage
     *
     * @param iFilename physiological data list to be removed
     *
     * @throws Exception
     */
    @Override
    public void removePhysioSignalSamples(String iFilename) throws Exception{
        File lToBeDeletedFile = new File(mApplicationContext.getFilesDir().getPath()+ "/" + iFilename);
        lToBeDeletedFile.delete();
    }

    /**
     * @brief cache seizure basic information
     *
     * @param iUser user uuid
     * @param iTimestamp user triggered timestamp
     * @param iSensitiveEventTimestamp sensitive event timestamp
     * @param iIntensity event intensity
     */
    @Override
    public void cacheSeizureBasicInformation(String iUser, String iTimestamp, String iSensitiveEventTimestamp, String iIntensity) {
        mSeizureCache = new SeizureEventModel(iUser, iTimestamp, iSensitiveEventTimestamp, iIntensity);
    }

    /**
     * @brief cache seizure additional info
     *
     * @param iQuestionTag question tag
     * @param iResultTag result tag
     */
    @Override
    public void cacheSeizureAdditionalInformation(String iQuestionTag, String iResultTag) {
        if(mSeizureCache != null) {
            mSeizureCache.addAdditionalInformation(iQuestionTag, iResultTag);
        }
    }

    /**
     * @brief getter cache
     *
     * @return cache
     */
    public CacheStorage getCache(){
        return mCache;
    }

    public SeizureEventModel getSeizureCache() {
        return mSeizureCache;
    }
}
