package com.wearablesensor.aura;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.FileStorage;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by lecoucl on 29/03/18.
 */

/**
 * @brief this class tests the FileStorage component
 * @details it rely on an instrumented test in order to be able to use Conceal file encryption system
 * during the test
 */
@RunWith(AndroidJUnit4.class)
public class FileStorageTest {

    private FileStorage mFileStorage;

    private Context mApplicationContext;
    private Context mTestApplicationContext;

    private DataFileHelper mDataFileHelper;

    private boolean compareStringToJsonFile(Context iTestApplicationContext, String iDataSequence, String iRefFilePath){
        String lRefFileString = "";
        InputStream lRefFileInputStream = null;

        try {
            lRefFileInputStream = iTestApplicationContext.getAssets().open(iRefFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            lRefFileString = IOUtils.toString(lRefFileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        iDataSequence = iDataSequence.trim();
        lRefFileString = lRefFileString.trim();

        return (iDataSequence.compareTo(lRefFileString) == 0);
    }
    // create a rule for an exception grabber that you can use across
    // the methods in this test class
    @Rule
    public ExpectedException sExceptionGrabber = ExpectedException.none();

    @Before
    public void setUp(){
        mApplicationContext = InstrumentationRegistry.getTargetContext();
        mTestApplicationContext = InstrumentationRegistry.getContext();

        mFileStorage = new FileStorage(mApplicationContext);
        mDataFileHelper = new DataFileHelper();
    }

    @Test
    public void saveSeizure_NullEntry() throws Exception{
        mFileStorage.saveSeizure(null);
        assertThat("null seizure event recording failed", !mDataFileHelper.isFileExistAt(FileStorage.getCacheSensitiveEventFilename()));
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void saveSeizure_SingleEntry() throws Exception{
        final String lUserUuid = UUID.randomUUID().toString();

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */ 118, 3, 1,1,10));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 3, 1,1,20));

        SeizureEventModel lSeizureEventModel = new SeizureEventModel(lUserUuid, lTimestamp1, lTimestamp2, "Big");
        mFileStorage.saveSeizure(lSeizureEventModel);
        assertThat("Single seizure event recording failed", mDataFileHelper.isFileExistAt(FileStorage.getCacheSensitiveEventFilename()));
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void saveSeizure_MultiplesEntries() throws Exception{
        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();

        final String lUserUuid = UUID.randomUUID().toString();

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,5));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,10));
        final String lTimestamp3 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,15));
        final String lTimestamp4 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,20));
        final String lTimestamp5 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,25));
        final String lTimestamp6 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,30));
        final String lTimestamp7 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,35));
        final String lTimestamp8 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 4, 1,1,40));

        mFileStorage.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp1, lTimestamp2, "Big"));
        mFileStorage.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp3, lTimestamp4, "Small"));
        mFileStorage.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp5, lTimestamp6, "Medium"));
        mFileStorage.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp7, lTimestamp8, "Medium"));

        lSeizureList = mFileStorage.querySeizures(FileStorage.getCacheSensitiveEventFilename());
        assertThat("Multiples seizures event query failed", (lSeizureList.size() == 4));
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void savePhysioSignals_MultipleEntries() throws Exception{
        final int NUMBER_OF_LOW_CACHE_VALUES = 100;
        final int NUMBER_OF_HIGH_CACHE_VALUES = 500;

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSignals = new ConcurrentLinkedQueue();
        for(int i = 0;i < NUMBER_OF_LOW_CACHE_VALUES; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 4, 1,1,5, 0));
            lPhysioSignals.add(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }
        mFileStorage.savePhysioSignals(lPhysioSignals);

        lPhysioSignals = new ConcurrentLinkedQueue();
        for(int i = 0;i < NUMBER_OF_HIGH_CACHE_VALUES; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 5, 1,1,5, 0));
            lPhysioSignals.add(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, new float[]{i, i+0.1f, i+0.2f}, "2G"));
        }
        mFileStorage.savePhysioSignals(lPhysioSignals);

        lPhysioSignals = new ConcurrentLinkedQueue();
        for(int i = 0;i < NUMBER_OF_LOW_CACHE_VALUES; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 6, 1,1,5, 0));
            lPhysioSignals.add(new SkinTemperatureModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, 25.f));
        }
        mFileStorage.savePhysioSignals(lPhysioSignals);

        assertThat("Cache events failed - file recorded", mDataFileHelper.getDataFiles().length == 3);
        mDataFileHelper.cleanPrivateFiles();
    }

    @Test
    public void queryRawContent_InvalidFileName() throws Exception{
        final String INVALID_DATA_FILE_NAME = "InvalidDataFileName.dat";

        // test invalid name file
        sExceptionGrabber.expect(Exception.class);
        mFileStorage.queryRawContent(INVALID_DATA_FILE_NAME);
    }

    @Test
    public void queryPhysioSignalSamples_MultiplesEntries() throws Exception{
        ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSignalList = new ConcurrentLinkedQueue<>();

        final String lPhysioUuid1 = UUID.randomUUID().toString();
        final String lPhysioUuid2 = UUID.randomUUID().toString();
        final String lPhysioUuid3 = UUID.randomUUID().toString();

        final String lDeviceAdressUuid = "df398d2d-f29e-4dd7-b1f9-09cc34eb1e83";
        final String lUserUuid = "c561e550-c174-483f-ac38-76fd09c72afd";

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,10));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,20));
        final String lTimestamp3 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,30));

        lPhysioSignalList.add(new MotionGyroscopeModel(lPhysioUuid1, lDeviceAdressUuid, lUserUuid, lTimestamp1, new float[]{-90.f, 30.f, 255.f}));
        lPhysioSignalList.add(new MotionGyroscopeModel(lPhysioUuid2, lDeviceAdressUuid, lUserUuid, lTimestamp2, new float[]{-10.f, 10.f, 45.f}));
        lPhysioSignalList.add(new MotionGyroscopeModel(lPhysioUuid3, lDeviceAdressUuid, lUserUuid, lTimestamp3, new float[]{10.f, -10.f, 45.f}));

        mFileStorage.savePhysioSignals(lPhysioSignalList);

        String lPhysioSignalsContent = mFileStorage.queryRawContent(FileStorage.getCachePhysioFilename(lTimestamp1));

        boolean lStatus = compareStringToJsonFile(mTestApplicationContext, lPhysioSignalsContent, "savePhysioSignals.json");
        assertThat("file content does not match with references json file", lStatus);
        mDataFileHelper.cleanPrivateFiles();
    }


}
