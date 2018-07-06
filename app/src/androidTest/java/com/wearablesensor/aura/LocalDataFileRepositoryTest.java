package com.wearablesensor.aura;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.wearablesensor.aura.data_repository.CacheStorage;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.FileStorage;
import com.wearablesensor.aura.data_repository.LocalDataFileRepository;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by lecoucl on 29/03/18.
 */

/**
 * @brief this class tests the LocalDataFile component
 * @details it rely on an instrumented test in order to be able to use Conceal file encryption system
 * during the test
 */
@RunWith(AndroidJUnit4.class)
public class LocalDataFileRepositoryTest{

    private LocalDataFileRepository mLocalDataFileRepository;

    private DataFileHelper mDataFileHelper;

    @Before
    public void setUp(){
        mLocalDataFileRepository = new LocalDataFileRepository( InstrumentationRegistry.getTargetContext());
        mDataFileHelper = new DataFileHelper();
    }


    @Test
    public void cachePhysioSignal_MultipleEntriesAndFileStorage() throws Exception{
        final int REMAINING_SAMPLES_IN_CACHE_LOW = 30;
        final int REMAINING_SAMPLES_IN_CACHE_HIGH = 300;

        final int NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES_LOW = 2 * CacheStorage.CACHE_CHANNEL_LOW_DATA_LIMIT + REMAINING_SAMPLES_IN_CACHE_LOW;
        final int NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES_HIGH = 2 * CacheStorage.CACHE_CHANNEL_HIGH_DATA_LIMIT + REMAINING_SAMPLES_IN_CACHE_HIGH;

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        for(int i = 0;i < NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES_LOW; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 4, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        for(int i = 0;i < NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES_HIGH; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 5, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, new float[]{i, i+0.1f, i+0.2f}, "2G"));
        }

        for(int i = 0;i < NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES_LOW; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 6, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, 25.f));
        }

        assertThat("Cache events failed - file recorded", mDataFileHelper.getDataFiles().length == 6);

        int lRRintervalInCache = mLocalDataFileRepository.getCache().getChannel(RRIntervalModel.RR_INTERVAL_TYPE).size();
        int lSkinTemperatureInCache = mLocalDataFileRepository.getCache().getChannel(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE).size();
        int lMotionAccelerometerInCache = mLocalDataFileRepository.getCache().getChannel(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL).size();

        assertThat("Remaining cache is invalid", lRRintervalInCache == REMAINING_SAMPLES_IN_CACHE_LOW &&
                                                                lSkinTemperatureInCache == REMAINING_SAMPLES_IN_CACHE_LOW &&
                                                                lMotionAccelerometerInCache == REMAINING_SAMPLES_IN_CACHE_HIGH);
        mLocalDataFileRepository.getCache().clear();
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void forceSavingPhysioSignalSamples_MultiplesEntries() throws Exception{
        int SAMPLES_IN_CACHE = 50;

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        for(int i = 0;i < SAMPLES_IN_CACHE; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 4, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        for(int i = 0;i < SAMPLES_IN_CACHE; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 5, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new MotionGyroscopeModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, new float[]{100.f, 80.f, 60.f}));
        }

        for(int i = 0;i < SAMPLES_IN_CACHE; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 6, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, 25.f));
        }

        mLocalDataFileRepository.forceSavingPhysioSignalSamples();

        assertThat("data not saved in file storage", mDataFileHelper.getDataFiles().length == 3);
        assertThat("cache is not clear", mLocalDataFileRepository.getCache().getCacheChannelIds().size() == 0);
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void removePhysioSignal() throws Exception{
        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        String lTimestamp1 = "", lTimestamp2 = "";

        for(int i = 0;i < CacheStorage.CACHE_CHANNEL_LOW_DATA_LIMIT; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 1, 1,1,5, 0));
            if(i == 0){
                lTimestamp1 = lTimestamp;
            }
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        for(int i = 0;i < CacheStorage.CACHE_CHANNEL_LOW_DATA_LIMIT; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 2, 1,1,5, 0));
            if(i == 0){
                lTimestamp2 = lTimestamp;
            }
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        mLocalDataFileRepository.removePhysioSignalSamples(FileStorage.getCachePhysioFilename(RRIntervalModel.RR_INTERVAL_TYPE, lTimestamp1));
        mLocalDataFileRepository.removePhysioSignalSamples(FileStorage.getCachePhysioFilename(RRIntervalModel.RR_INTERVAL_TYPE, lTimestamp2));
        assertThat("RemovePhysioSignals failed - files not deleted", mDataFileHelper.getDataFiles().length == 0);
        mDataFileHelper.cleanPrivateFiles();
    }
}
