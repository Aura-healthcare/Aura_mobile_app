package com.wearablesensor.aura.data_repository;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;


public class DataSequenceSerializerTest {

    private DataSequenceSerializer mDataSequenceSerializer;

    private GsonBuilder mGsonBuilder;
    private Gson mGson;

    private boolean compareStringToJsonFile(String iDataSequence, String iRefFilePath){
        String lRefFileString = "";
        InputStream lRefFileInputStream = getClass().getResourceAsStream(iRefFilePath);
        try {
            lRefFileString = IOUtils.toString(lRefFileInputStream);
        } catch (IOException e) {

        }

        iDataSequence = iDataSequence.trim();
        lRefFileString = lRefFileString.trim();

        return (iDataSequence.compareTo(lRefFileString) == 0);
    }

    @Before
    public void SetUp(){
        mDataSequenceSerializer = new DataSequenceSerializer();

        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(ConcurrentLinkedQueue.class, new DataSequenceSerializer());
        mGson = mGsonBuilder.create();
    }

    @Test
    public void serialize_emptyDataSequence(){
        ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSamples = new ConcurrentLinkedQueue<>();
        String lDataSequence = mGson.toJson(lPhysioSamples);
        assertThat("Empty data sequence jsonify fail", lDataSequence.equals("{}"));
    }

    @Test
    public void serialize_SmallDataSequence(){
        ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSamples = new ConcurrentLinkedQueue<>();

        String lDeviceAdressUuid = "89d16533-e19d-4af8-a9ad-d759dbe0d5a1";
        String lUserUuid = "d6273433-9f75-4d81-81c3-ee47c6334178";

        String lPhysioUuid;
        String lTimestamp;
        for(int i = 0; i < 5; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date(i, 1, 1, 1, 1, 1));
            lPhysioSamples.add(new RRIntervalModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, i * 100));
        }

        String lDataSequence = mGson.toJson(lPhysioSamples);
        boolean lIsSame = compareStringToJsonFile(lDataSequence, "/smallDataSequence.json");
        assertThat("fail to serialize data", lIsSame);
    }

    @Test
    public void serialize_LargeDataSequence(){
        ConcurrentLinkedQueue<PhysioSignalModel> lPhysioSamples = new ConcurrentLinkedQueue<>();

        String lDeviceAdressUuid = "2de6210e-29cc-4187-bc02-dd54d45a1dde";
        String lUserUuid = "fae4d0af-62de-41a2-9ad7-7a41021f961d";

        String lPhysioUuid;
        String lTimestamp;
        for(int i = 0; i < 500; i++){
            lPhysioUuid = UUID.randomUUID().toString();
            lTimestamp = DateIso8601Mapper.getString(new Date(i, 1, 1, 1, 1, 1));
            lPhysioSamples.add(new MotionAccelerometerModel(lPhysioUuid, lDeviceAdressUuid, lUserUuid, lTimestamp, new float[]{0.5f, 0.5f, 0.5f}, "2G"));
        }

        String lDataSequence = mGson.toJson(lPhysioSamples);
        boolean lIsSame = compareStringToJsonFile(lDataSequence, "/largeDataSequence.json");
        assertThat("fail to serialize data", lIsSame);
    }

}