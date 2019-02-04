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
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_sync.DataSyncUpdateStateNotification;


import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public class FileStorage {

    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private KeyChain mKeyChain;
    private Crypto mCrypto;

    private GsonBuilder mGsonBuilder;
    private Gson mGson;

    public final static String CACHE_FILENAME = "DataFile_";
    public final static String SENSITIVE_EVENT_SUFFIX = "SensitiveEvent_";
    public final static String PHYSIO_SIGNAL_SUFFIX = "PhysioSignal_";
    public final static String CACHE_FILENAME_EXTENSION = "csv";

    public final static String EXPORT_AURA_DATA_DIR = "/auraExport";
    /**
     *
     * @brief constructor
     *
     * @param iApplicationContext application context
     */

    public FileStorage(Context iApplicationContext){
        mApplicationContext = iApplicationContext;

        // Creates a new Crypto object with default implementations of a key chain
        mKeyChain = new SharedPrefsBackedKeyChain(mApplicationContext, CryptoConfig.KEY_256);
        mCrypto = AndroidConceal.get().createDefaultCrypto(mKeyChain);

        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(ConcurrentLinkedQueue.class, new DataSequenceSerializer());
        mGson = mGsonBuilder.create();
    }

    /**
     * @brief save a batch of physio signals
     *
     * @param iPhysioSignalSamples list of physio signal samples to be saved
     *
     * @throws IOException
     */
    public void savePhysioSignals(final ConcurrentLinkedQueue<PhysioSignalModel> iPhysioSignalSamples) throws IOException {
        if(iPhysioSignalSamples.isEmpty()){
            return;
        }

        PhysioSignalModel lPhysioSignal = iPhysioSignalSamples.peek();
        String lFilename = getCachePhysioFilename(lPhysioSignal.getType(), lPhysioSignal.getTimestamp());

        FileOutputStream lFileOutputStream = null;
        OutputStream lOutputStream = null;

        try {
            File lDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + EXPORT_AURA_DATA_DIR);
            if(!lDir.isDirectory()){
                lDir.mkdirs();
            }

            File lFile = new File(lDir, lFilename);
            lFileOutputStream = new FileOutputStream(lFile);

            String lData = convertDataToSimpleCsv(iPhysioSignalSamples);
            lFileOutputStream.write(lData.getBytes());

            EventBus.getDefault().post(new DataSyncUpdateStateNotification());

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(lFileOutputStream != null){
                lFileOutputStream.close();
            }
        }
    }

    /**
     * @brief query content from a data file
     *
     * @param iFilename selected file name
     *
     * @return file content as a string
     *
     * @throws IOException
     * @throws KeyChainException
     * @throws CryptoInitializationException
     */
    public String queryRawContent(String iFilename) throws IOException, KeyChainException, CryptoInitializationException {

        Log.d(TAG, "File Open: " + iFilename);

        String oLine = "";
        FileInputStream lInputStream = mApplicationContext.openFileInput(iFilename);

        try{
            // if file the available for reading
            if (lInputStream != null) {
                // prepare the file for reading
                InputStreamReader lInputReader = new InputStreamReader(lInputStream);
                BufferedReader lBuffreader = new BufferedReader(lInputReader);

                String lLine = lBuffreader.readLine();
                oLine += lLine;

                while(lLine != null) {
                    lLine = lBuffreader.readLine();
                    // artefact generated by encryption/decryption
                    if( lLine != null && !lLine.equals("null")) {
                        oLine += lLine;
                    }
                }

                lBuffreader.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lInputStream.close();
        }

        return oLine;
    }

    /**
     * @brief save a seizure
     *
     * @param iSeizureEventModel seizure event to be saved
     *
     * @throws Exception
     */
    public void saveSeizure(SeizureEventModel iSeizureEventModel) throws Exception {
        String lFilename = getCacheSensitiveEventFilename();
        FileOutputStream lOutputStream = null;

        if(iSeizureEventModel == null){
            return;
        }

        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();

        // fetch previously recorded seizures
        try {
            lSeizureList = querySeizures(lFilename);
        }
        catch(Exception e){
            Log.d(TAG, "Create Seizure file");
        }

        // insert new seizure into encrypted JSON
        lSeizureList.add(iSeizureEventModel);
        try {

            File lDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + EXPORT_AURA_DATA_DIR);
            if(!lDir.isDirectory()){
                lDir.mkdirs();
            }
            File lFile = new File(lDir, lFilename);
            lOutputStream = new FileOutputStream(lFile);

            Type lType = new TypeToken<ArrayList<SeizureEventModel>>() {}.getType();
            String lData = "";
            lData = mGson.toJson(lSeizureList, lType);
            lOutputStream.write(lData.getBytes());

            EventBus.getDefault().post(new DataSyncUpdateStateNotification());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(lOutputStream!=null){
                lOutputStream.close();
            }
        }
    }

    /**
     * @brief query of list of seizures
     *
     * @param iFilename selected file name
     *
     * @return seizure list
     *
     * @throws IOException
     * @throws CryptoInitializationException
     * @throws KeyChainException
     */
    public ArrayList<SeizureEventModel> querySeizures(String iFilename) throws IOException, CryptoInitializationException, KeyChainException {
        Log.d(TAG, "File Open: " + iFilename);

        String lSeizureData = queryRawContent(iFilename);

        Type lType = new TypeToken<ArrayList<SeizureEventModel>>() {}.getType();
        ArrayList<SeizureEventModel> lSeizureList = mGson.fromJson(lSeizureData, lType);

        Log.d(TAG, "Samples count - " + lSeizureList.size());
        return lSeizureList;
    }

    public static String getCachePhysioFilename(String iDatatype, String iTimestamp){
        return CACHE_FILENAME + PHYSIO_SIGNAL_SUFFIX + iDatatype + "_" + iTimestamp + "." + CACHE_FILENAME_EXTENSION;
    }

    public static String getCacheSensitiveEventFilename(){
        return CACHE_FILENAME + SENSITIVE_EVENT_SUFFIX + "." + CACHE_FILENAME_EXTENSION;
    }

    public static String convertDataToSimpleCsv(ConcurrentLinkedQueue<PhysioSignalModel> iData)
    {
        if(iData == null || iData.isEmpty()){
            return "";
        }

        String oCsv = iData.peek().toCsvHeader();
        for (PhysioSignalModel iDataSample : iData){
            oCsv += iDataSample.toCsvString();
        }

        return oCsv;
    }

}
