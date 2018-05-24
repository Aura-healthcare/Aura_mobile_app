package com.wearablesensor.aura.data_repository;

import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;


public class CacheStorageTest {

    @Test
    public void addPhysioSample_MultipleSamples(){
        CacheStorage lCacheStorage = new CacheStorage();

        String lDeviceAdressUuid = UUID.randomUUID().toString();
        String lUserUuid = UUID.randomUUID().toString();

        String lPhysioUuid;
        String lTimestamp;
        for(int i=0; i < 3; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{0.1f, 0.2f, 0.3f}, "2G"));
        }

        for(int i= 0; i < 3; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, 700));
        }

        assertThat("Motion data not properly recorded", lCacheStorage.getChannel(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL).size() == 3);
        assertThat("RR interval data not properly recorded", lCacheStorage.getChannel(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL).size() == 3);
        assertThat("Wrong channel number", lCacheStorage.getCacheChannelIds().size() == 2);
    }

    @Test
    public void addPhysioSample_CacheStatus(){
        CacheStorage lCacheStorage = new CacheStorage();

        String lDeviceAdressUuid = UUID.randomUUID().toString();
        String lUserUuid = UUID.randomUUID().toString();

        String lPhysioUuid;
        String lTimestamp;
        CacheStorage.CacheStatus lCacheStatus;
        for(int i= 1; i < 1000; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStatus = lCacheStorage.add(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{0.1f, 0.2f, 0.3f}, "2G"));
            if(i < CacheStorage.CACHE_CHANNEL_HIGH_DATA_LIMIT){
                assertThat("Cache status incorrect", lCacheStatus == CacheStorage.CacheStatus.FREE_SPACE);
            }
            else{
                assertThat("Cache status incorrect", lCacheStatus == CacheStorage.CacheStatus.FULL);
            }
        }

        for(int i=1; i < 1000; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStatus = lCacheStorage.add(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, 37.5f));
            if(i < CacheStorage.CACHE_CHANNEL_LOW_DATA_LIMIT){
                assertThat("Cache status incorrect", lCacheStatus == CacheStorage.CacheStatus.FREE_SPACE);
            }
            else{
                assertThat("Cache status incorrect", lCacheStatus == CacheStorage.CacheStatus.FULL);
            }
        }
    }

    @Test
    public void clearCacheChannel(){
        CacheStorage lCacheStorage = new CacheStorage();

        String lDeviceAdressUuid = UUID.randomUUID().toString();
        String lUserUuid = UUID.randomUUID().toString();

        String lPhysioUuid;
        String lTimestamp;

        for(int i=0; i < CacheStorage.CACHE_CHANNEL_LOW_DATA_LIMIT; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, 37.5f));
        }

        for(int i=0; i < CacheStorage.CACHE_CHANNEL_HIGH_DATA_LIMIT; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new MotionGyroscopeModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{180.0f, -90.0f, 0.0f}));
        }

        assertThat("Fail to record data", lCacheStorage.getCacheChannelIds().size() == 2);

        lCacheStorage.clearChannel(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL);
        assertThat("Channel removal fail", lCacheStorage.getCacheChannelIds().size() == 1 && lCacheStorage.getCacheChannelIds().toArray()[0] == SkinTemperatureModel.SKIN_TEMPERATURE_TYPE);

        lCacheStorage.clearChannel(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE);
        assertThat("Channel removal fail", lCacheStorage.getCacheChannelIds().size() == 0);
    }

    @Test
    public void clearCache(){
        CacheStorage lCacheStorage = new CacheStorage();

        String lDeviceAdressUuid = UUID.randomUUID().toString();
        String lUserUuid = UUID.randomUUID().toString();

        String lPhysioUuid;
        String lTimestamp;

        for(int i=0; i < 10; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, 37.5f));
        }

        for(int i=0; i < 10; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new MotionGyroscopeModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{180.0f, -90.0f, 0.0f}));
        }

        for(int i=0; i < 10; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{0.1f, 0.2f, 0.3f}, "2G"));
        }

        for(int i=0; i < 10; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date());
            lCacheStorage.add(new ElectroDermalActivityModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, 120000, 15));
        }

        assertThat("Fail to record data", lCacheStorage.getCacheChannelIds().size() == 4);

        lCacheStorage.clear();

        assertThat("Fail to clear cache", lCacheStorage.getCacheChannelIds().size() == 0);
    }


}