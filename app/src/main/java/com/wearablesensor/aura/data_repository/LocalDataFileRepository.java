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

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.KeyChain;
import com.wearablesensor.aura.data_repository.models.ModelSerializer;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;


public class LocalDataFileRepository implements LocalDataRepository {
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private ArrayList<PhysioSignalModel> mPhysioSignalCache; /* temporary storage in an array to avoid call overhead to local data repository */

    private KeyChain mKeyChain;
    private Crypto mCrypto;

    public final static String CACHE_FILENAME = "DataFile_";
    public final static String SENSITIVE_EVENT_SUFFIX = "SensitiveEvent_";
    public final static String PHYSIO_SIGNAL_SUFFIX = "PhysioSignal_";
    /**
     * @brief constructor
     *
     * @param iApplicationContext application context
     */

    public LocalDataFileRepository(Context iApplicationContext){
        Log.d(TAG, "Local data file repository init");

        mApplicationContext = iApplicationContext;
        mPhysioSignalCache = new ArrayList<PhysioSignalModel>();

        // Creates a new Crypto object with default implementations of a key chain
        mKeyChain = new SharedPrefsBackedKeyChain(mApplicationContext, CryptoConfig.KEY_256);
        mCrypto = AndroidConceal.get().createDefaultCrypto(mKeyChain);
    }

    /**
     * @brief query a list of physiological data samples from a cache file
     *
     * @param iFilename cache file storing the physio signal samples
     *
     * @return a list of physiological data samples
     *
     * @throws Exception
     */

    @Override
    public ArrayList<PhysioSignalModel> queryPhysioSignalSamples(String iFilename) throws Exception {

        Log.d(TAG, "File Open: " + iFilename);

        FileInputStream lInputStream = mApplicationContext.openFileInput(iFilename);
        InputStream lDecryptedInputStream = mCrypto.getCipherInputStream(
                lInputStream, Entity.create("entity_id"));
        ArrayList<PhysioSignalModel> lPhysioSignalSamples = new ArrayList<PhysioSignalModel>();

        try{
            // if file the available for reading
            if (lInputStream != null) {


                // prepare the file for reading
                InputStreamReader lInputReader = new InputStreamReader(lDecryptedInputStream);
                BufferedReader lBuffreader = new BufferedReader(lInputReader);

                String lLine = lBuffreader.readLine();

                while(lLine != null) {
                    PhysioSignalModel lPhysioSignalSample = ModelSerializer.deserialize(lLine);
                    if(lPhysioSignalSample != null){
                        lPhysioSignalSamples.add(lPhysioSignalSample);
                    }
                    lLine = lBuffreader.readLine();
                }

                lBuffreader.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lInputStream.close();
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
        if(iPhysioSignalSamples.isEmpty()){
            return;
        }

        String lFilename = getCachePhysioFilename(iPhysioSignalSamples.get(0).getTimestamp());
        FileOutputStream lFileOutputStream = null;
        OutputStream lOutputStream = null;

        Log.d(TAG, "Start Recording");
        try {
            lFileOutputStream = mApplicationContext.openFileOutput(lFilename, Context.MODE_PRIVATE);
            lOutputStream = new BufferedOutputStream(lFileOutputStream);

            // Creates an output stream which encrypts the data as
            // it is written to it and writes it out to the file.
            OutputStream lCryptedStream = mCrypto.getCipherOutputStream(
                    lOutputStream,
                    Entity.create("entity_id"));

            String lData = "";
            for(int i = 0; i < iPhysioSignalSamples.size(); i++){
                lData += ModelSerializer.serialize(iPhysioSignalSamples.get(i)) + "\n";
            }

            lCryptedStream.write(lData.getBytes());
            lCryptedStream.close();

            EventBus.getDefault().post(new DataSyncUpdateStateNotification());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(lFileOutputStream != null){
                lFileOutputStream.close();
            }

            if(lOutputStream != null){
                lOutputStream.close();
            }

        }
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
     * @brief query a list of  sensitive events
     *
     * @param iFilename cache file storing the physio signal samples
     *
     * @return a list of physiological data samples
     *
     * @throws Exception
     */

    @Override
    public ArrayList<SeizureEventModel> querySeizures(String iFilename) throws Exception {

        Log.d(TAG, "File Open: " + iFilename);

        FileInputStream lInputStream = mApplicationContext.openFileInput(iFilename);
        InputStream lDecryptedInputStream = mCrypto.getCipherInputStream(
                lInputStream, Entity.create("entity_id"));
        ArrayList<SeizureEventModel> lSeizureEventSamples = new ArrayList<SeizureEventModel>();

        try{
            // if file the available for reading
            if (lDecryptedInputStream != null) {
                // prepare the file for reading
                InputStreamReader lInputReader = new InputStreamReader(lDecryptedInputStream);
                BufferedReader lBuffreader = new BufferedReader(lInputReader);

                String lLine = lBuffreader.readLine();

                // read every line of the file into the line-variable, on line at the time
                // TODO: implement a builder

                while(lLine != null) {
                    String[] lArgs = lLine.split(" ");
                    if(lArgs[1].equals(SeizureEventModel.SENSITIVE_EVENT_TYPE)){
                        SeizureEventModel lSeizureEvent = new SeizureEventModel(lArgs[0], lArgs[2], lArgs[3], lArgs[4], lArgs[5]);
                        lSeizureEventSamples.add( lSeizureEvent );
                    }

                    lLine = lBuffreader.readLine();
                }

                lBuffreader.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lInputStream.close();
        }

        Log.d(TAG, "Samples count - " + lSeizureEventSamples.size());
        return lSeizureEventSamples;
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

        String lFilename = getCacheSensitiveEventFilename();
        FileOutputStream lOutputStream = null;

        ArrayList<SeizureEventModel> lSeizureList = new ArrayList<>();
        try {
            lSeizureList = querySeizures(lFilename);
        }
        catch(FileNotFoundException e){
            Log.d(TAG, "Create Seizure file");
        }
        catch (Exception e){
            throw new Exception();
        }

        if(iSeizureEventModel == null){
            return;
        }

        lSeizureList.add(iSeizureEventModel);

        Log.d(TAG, "Start Recording");
        try {
            lOutputStream = mApplicationContext.openFileOutput(lFilename, Context.MODE_PRIVATE);
            // Creates an output stream which encrypts the data as
            // it is written to it and writes it out to the file.
            OutputStream lCryptedStream = mCrypto.getCipherOutputStream(
                    lOutputStream,
                    Entity.create("entity_id"));

            String lData = "";
            for(SeizureEventModel lSeizure : lSeizureList){
                lData += lSeizureList.toString() + "\n";
            }

            lCryptedStream.write(lData.getBytes());
            lCryptedStream.close();

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
     * @brief clear cache and store data from heap to local data storage
     *
     */
    public void clearCache() throws Exception{
        savePhysioSignalSamples(mPhysioSignalCache);
        mPhysioSignalCache.clear();
    }

    public static String getCachePhysioFilename(String iTimestamp){
        return CACHE_FILENAME + PHYSIO_SIGNAL_SUFFIX +iTimestamp+".dat";
    }

    public static String getCacheSensitiveEventFilename(){
        return CACHE_FILENAME + SENSITIVE_EVENT_SUFFIX + ".dat";
    }
}
