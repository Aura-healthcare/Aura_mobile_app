package com.wearablesensor.aura;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
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

    private boolean isFileExistAt(String iFileName){
        FileInputStream lFileStream = null;
        try {
            lFileStream =  InstrumentationRegistry.getTargetContext().openFileInput(iFileName);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    private File[] getDataFiles(){
        File lrootFolder = InstrumentationRegistry.getTargetContext().getFilesDir();
        File[] lDataFiles = lrootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(".+dat$", pathname.getName());
            }
        });

        return lDataFiles;
    }
    private void cleanPrivateFiles(){
        File lrootFolder = InstrumentationRegistry.getTargetContext().getFilesDir();
        File[] lDataFiles = getDataFiles();

        // clean all data files
        for(File lFile : lDataFiles){
            InstrumentationRegistry.getTargetContext().deleteFile(lFile.getName());
        }
    }

    // create a rule for an exception grabber that you can use across
    // the methods in this test class
    @Rule
    public ExpectedException sExceptionGrabber = ExpectedException.none();

    @Before
    public void setUp(){
        mLocalDataFileRepository = new LocalDataFileRepository( InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void savePhysioSignalSamples_NullEntries() throws Exception{
        ArrayList<PhysioSignalModel> lPhysioSignalList = new ArrayList<>();

        mLocalDataFileRepository.savePhysioSignalSamples(lPhysioSignalList);
        // no data files has been written

        assertThat("empty physio signals list recording failed", getDataFiles().length == 0 );
        cleanPrivateFiles();
    }

    @Test
    public void savePhysioSignalSamples_MultipleEntries() throws Exception{
        ArrayList<PhysioSignalModel> lPhysioSignalList = new ArrayList<>();

        final String lPhysioUuid1 = UUID.randomUUID().toString();
        final String lPhysioUuid2 = UUID.randomUUID().toString();
        final String lPhysioUuid3 = UUID.randomUUID().toString();

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 1, 1,1,10));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 1, 1,1,20));
        final String lTimestamp3 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 1, 1,1,30));

        lPhysioSignalList.add(new RRIntervalModel(lPhysioUuid1, lDeviceAdressUuid, lUserUuid, lTimestamp1, 400));
        lPhysioSignalList.add(new MotionAccelerometerModel(lPhysioUuid2, lDeviceAdressUuid, lUserUuid, lTimestamp2, new float[]{0.5f, 1.0f, -0.5f}, "2G"));
        lPhysioSignalList.add(new SkinTemperatureModel(lPhysioUuid3, lDeviceAdressUuid, lUserUuid, lTimestamp3, 28.0f));

        mLocalDataFileRepository.savePhysioSignalSamples(lPhysioSignalList);

        // encrypted file is recorded with the name including timestamp of the first saved sample
        assertThat("Multiple physio signals recording failed", isFileExistAt(LocalDataFileRepository.getCachePhysioFilename(lTimestamp1)));
        cleanPrivateFiles();
    }

    @Test
    public void queryPhysioSignalSamples_InvalidFileName() throws Exception{
        ArrayList<PhysioSignalModel> lPhysioSignalList = new ArrayList<>();

        final String INVALID_DATA_FILE_NAME = "InvalidDataFileName.dat";

        // test invalid name file
        sExceptionGrabber.expect(Exception.class);
        lPhysioSignalList = mLocalDataFileRepository.queryPhysioSignalSamples(INVALID_DATA_FILE_NAME);
    }

    @Test
    public void queryPhysioSignalSamples_InvalidFileContent() throws Exception{
        ArrayList<PhysioSignalModel> lPhysioSignalList = new ArrayList<>();

        final String INVALID_DATA_FILE_NAME = "InvalieDataFileName.dat";

        // invalid data file content test
        FileOutputStream lOutputStream = InstrumentationRegistry.getTargetContext().openFileOutput(INVALID_DATA_FILE_NAME, Context.MODE_PRIVATE);
        lOutputStream.write("Invalid Invalid Invalid Invalid".getBytes());
        assertThat("query invalid file content failed", lPhysioSignalList.size() == 0 );
        cleanPrivateFiles();
    }

    @Test
    public void queryPhysioSignalSamples_MultiplesEntries() throws Exception{
        ArrayList<PhysioSignalModel> lPhysioSignalList = new ArrayList<>();
        ArrayList<PhysioSignalModel> lPhysioSignalOutList = new ArrayList<>();

        final String lPhysioUuid1 = UUID.randomUUID().toString();
        final String lPhysioUuid2 = UUID.randomUUID().toString();
        final String lPhysioUuid3 = UUID.randomUUID().toString();

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,10));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,20));
        final String lTimestamp3 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 2, 1,1,30));

        lPhysioSignalList.add(new MotionGyroscopeModel(lPhysioUuid1, lDeviceAdressUuid, lUserUuid, lTimestamp1, new float[]{-90.f, 30.f, 255.f}));
        lPhysioSignalList.add(new ElectroDermalActivityModel(lPhysioUuid2, lDeviceAdressUuid, lUserUuid, lTimestamp2, 3000, 20.2));
        lPhysioSignalList.add(new RRIntervalModel(lPhysioUuid3, lDeviceAdressUuid, lUserUuid, lTimestamp3, 400));

        mLocalDataFileRepository.savePhysioSignalSamples(lPhysioSignalList);

        lPhysioSignalOutList = mLocalDataFileRepository.queryPhysioSignalSamples(LocalDataFileRepository.getCachePhysioFilename(lTimestamp1));
        assertThat("query multiples entries failed - wrong elements number", lPhysioSignalOutList.size() == 3 );
        assertThat("query multiples entries failed - wrong elements type", (lPhysioSignalOutList.get(0).getType() == lPhysioSignalList.get(0).getType()) &&
                                                                                          (lPhysioSignalOutList.get(1).getType() == lPhysioSignalList.get(1).getType()) &&
                                                                                          (lPhysioSignalOutList.get(2).getType() == lPhysioSignalList.get(2).getType()));
        cleanPrivateFiles();
    }

    @Test
    public void saveSeizure_NullEntry() throws Exception{
        mLocalDataFileRepository.saveSeizure(null);
        assertThat("null seizure event recording failed", !isFileExistAt(LocalDataFileRepository.getCacheSensitiveEventFilename()));
        cleanPrivateFiles();
    }

    @Test
    public void saveSeizure_SingleEntry() throws Exception{
        final String lUserUuid = UUID.randomUUID().toString();

        final String lTimestamp1 = DateIso8601Mapper.getString(new Date(/*1900 + */ 118, 3, 1,1,10));
        final String lTimestamp2 = DateIso8601Mapper.getString(new Date(/*1900 + */118, 3, 1,1,20));

        SeizureEventModel lSeizureEventModel = new SeizureEventModel(lUserUuid, lTimestamp1, lTimestamp2, "Big");
        mLocalDataFileRepository.saveSeizure(lSeizureEventModel);
        assertThat("Single seizure event recording failed", isFileExistAt(LocalDataFileRepository.getCacheSensitiveEventFilename()));
        cleanPrivateFiles();
    }

    @Test
    public void querySeizure_MultiplesEntries() throws Exception{
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

        mLocalDataFileRepository.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp1, lTimestamp2, "Big"));
        mLocalDataFileRepository.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp3, lTimestamp4, "Small"));
        mLocalDataFileRepository.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp5, lTimestamp6, "Medium"));
        mLocalDataFileRepository.saveSeizure(new SeizureEventModel(lUserUuid, lTimestamp7, lTimestamp8, "Medium"));

        lSeizureList = mLocalDataFileRepository.querySeizures(LocalDataFileRepository.getCacheSensitiveEventFilename());
        assertThat("Multiples seizures event query failed", (lSeizureList.size() == 4));
        cleanPrivateFiles();
    }

    @Test
    public void cachePhysioSignal_MultipleEntries() throws Exception{
        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();

        final int NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES = 230;

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        for(int i = 0;i < NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 4, 1,1,5, 0));
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        assertThat("Cache events failed - file recorded", getDataFiles().length == 2);
        cleanPrivateFiles();
    }

    @Test
    public void removePhysioSignal() throws Exception{
        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();

        final int NUMBER_OF_CACHE_VALUES_MULTIPLES_FILES = 230;

        final String lDeviceAdressUuid = UUID.randomUUID().toString();
        final String lUserUuid = UUID.randomUUID().toString();

        String lTimestamp1 = "", lTimestamp2 = "";

        for(int i = 0;i < 100; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 1, 1,1,5, 0));
            if(i == 0){
                lTimestamp1 = lTimestamp;
            }
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        for(int i = 0;i < 100; i++){
            String lPhysioUuid = UUID.randomUUID().toString();
            String lTimestamp = DateIso8601Mapper.getString(new Date(/*1900 + */i, 2, 1,1,5, 0));
            if(i == 0){
                lTimestamp2 = lTimestamp;
            }
            mLocalDataFileRepository.cachePhysioSignalSample(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid,lTimestamp, i));
        }

        mLocalDataFileRepository.removePhysioSignalSamples(LocalDataFileRepository.getCachePhysioFilename(lTimestamp1));
        mLocalDataFileRepository.removePhysioSignalSamples(LocalDataFileRepository.getCachePhysioFilename(lTimestamp2));
        assertThat("RemovePhysioSignals failed - files not deleted", getDataFiles().length == 0);
        cleanPrivateFiles();
    }
}
