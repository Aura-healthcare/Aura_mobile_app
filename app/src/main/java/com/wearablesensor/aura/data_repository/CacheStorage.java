/**
 * @file CacheStorage
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
 * ).CacheStorage provide a multi channel cache mecanism
 *
 */

package com.wearablesensor.aura.data_repository;


import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class CacheStorage  {

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<PhysioSignalModel> > mCache; // cache storage

    enum CacheStatus{
        FREE_SPACE,
        FULL
    } // cache status

    public final static int CACHE_CHANNEL_HIGH_DATA_LIMIT = 5000;
    public final static int CACHE_CHANNEL_LOW_DATA_LIMIT = 1000;

    /**
     * @brief constructor
     */
    public CacheStorage(){
        mCache = new ConcurrentHashMap<>();
    }

    /**
     * @brief add new entry in cache
     * @param iPhysioSample data sample
     *
     * @return cache channel status (free space or full)
     */
    public CacheStatus add(PhysioSignalModel iPhysioSample){
        String lPhysioSampleType = iPhysioSample.getType();

        // create one cache channel by physio signal type
        if(!mCache.containsKey(lPhysioSampleType)){
            mCache.put(lPhysioSampleType, new ConcurrentLinkedQueue<PhysioSignalModel>());
        }

        // fill the proper channel
        mCache.get(lPhysioSampleType).add(iPhysioSample);
        return getCacheChannelStatus(lPhysioSampleType);
    }

    /**
     * @brief get data from a specific cache channel
     *
     * @param iChannel selected channel
     * @return data list contains in this channel
     */
    public ConcurrentLinkedQueue<PhysioSignalModel> getChannel(String iChannel){
        return mCache.get(iChannel);
    }

    /**
     * @brief clear data from a specific channel
     *
     * @param iChannel selected channel
     */
    public void clearChannel(String iChannel){
        mCache.remove(iChannel);
    }

    /**
     * @brief clear data from cache
     */
    public void clear(){
        mCache.clear();
    }

    /**
     * @brief get cache channel status
     *
     * @param iChannel selected channel
     *
     * @return cache channel status
     */
    public CacheStatus getCacheChannelStatus(String iChannel){
        if( isChannelHighStream(iChannel) ){
            if (mCache.get(iChannel).size() >= CACHE_CHANNEL_HIGH_DATA_LIMIT) {
                return CacheStatus.FULL;
            }
        }
        else {
            if (mCache.get(iChannel).size() >= CACHE_CHANNEL_LOW_DATA_LIMIT) {
                return CacheStatus.FULL;
            }
        }

        return CacheStatus.FREE_SPACE;
    }

    /**
     * @brief specific cache data limit strategy for channel with high streaming input
     *
     * @param iChannel selected channel
     *
     * @return true if the channel is a high stream input channel
     */
    private boolean isChannelHighStream(String iChannel){
        return (iChannel.equals(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL) || iChannel.equals(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL));
    }

    /**
     * @brief get a set of created channels in cache
     *
     * @return set of channels
     */
    public Set<String> getCacheChannelIds(){
        return mCache.keySet();
    }

    /**
     * @brief print cache description
     *
     * @return cache description
     */
    public String toString(){
        String lString = "";

        for(String key : mCache.keySet()){
            lString += "channel - " + key + " entries - "+ mCache.get(key).size() + "\n";
        }

        return lString;
    }


}
