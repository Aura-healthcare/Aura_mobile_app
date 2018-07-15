/**
 * @file DataSyncService
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
 * DataSyncService is the service that handles transfer data logic from local data repository to
 * remote data repository and keep user session settings updated accordingly
 */

package com.wearablesensor.aura.data_sync;

import android.content.Context;
import android.net.wifi.WifiManager;

import android.util.Log;

import com.wearablesensor.aura.data_repository.FileStorage;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_sync.notifications.DataAckNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncEndNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncStartNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;
import com.wearablesensor.aura.data_sync.notifications.SocketOnCloseNotification;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class DataSyncService{
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository.TimeSeries mRemoteDataTimeSeriesRepository;
    private AtomicBoolean mIsDataSyncEnabled;
    private AtomicBoolean mIsDataSyncInProgress;

    private ScheduledExecutorService mScheduler;
    private CountDownLatch mSocketWorkDoneBarrier;

    /**
     * @brief constructor
     *
     * @param iLocalDataRepository local data repository
     * @param iRemoteDataTimeSeriesRepository remote data time series repository
     * @param iApplicationContext application context
     */
    public DataSyncService(LocalDataRepository iLocalDataRepository, RemoteDataRepository.TimeSeries iRemoteDataTimeSeriesRepository, Context iApplicationContext){
        mApplicationContext = iApplicationContext;

        mLocalDataRepository = iLocalDataRepository;
        mRemoteDataTimeSeriesRepository = iRemoteDataTimeSeriesRepository;

        mIsDataSyncEnabled = new AtomicBoolean(false);
        mIsDataSyncInProgress = new AtomicBoolean(false);
        setDataSyncIsInProgress(false);
        mSocketWorkDoneBarrier = null;
    }

    /**
     * @brief initialize data sync service by enabling observer on Wifi state
     */

    public void initialize(){
        EventBus.getDefault().register(this);

        startDataSync();
    }

    /**
     * @brief close service, detach observers on Wifi state and stop data transfer
     */
    public void close(){
        stopDataSync();
        EventBus.getDefault().unregister(this);
    }


    /**
     * @brief setter for data sync is in progress
     *
     * @param iStatus progress status
     */
    public void setDataSyncIsInProgress(Boolean iStatus){
        mIsDataSyncInProgress.set(iStatus);

        if(iStatus == true){
            EventBus.getDefault().post(new DataSyncStartNotification());
        }
        else {
            EventBus.getDefault().post(new DataSyncEndNotification());
        }
    }

    /**
     * @brief getter for data syn is in progress
     *
     * @return true if data sync is in progress, false otherwise
     */

    public boolean isDataSyncInProgress() {
        return mIsDataSyncInProgress.get();
    }
    /**
     * @brief start data sync
     */

    public synchronized void startDataSync() {
        Log.d(TAG, "Start Data - " + mIsDataSyncEnabled.get() + " - " + mIsDataSyncInProgress.get() );

        if(mIsDataSyncEnabled.get()) {
            return;
        }

        mIsDataSyncEnabled.set(true);
        // sync data with server continuously
        mScheduler = Executors.newSingleThreadScheduledExecutor();
        mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Run Scheduled Thread");

                if(!isWifiEnabled()){
                    Log.d(TAG, "Wifi Not Enabled");
                    return;
                }

                Log.d(TAG, "Wifi ok");

                if(isDataSyncInProgress()){
                    return;
                }

                setDataSyncIsInProgress(true);
                Log.d(TAG, "Data transfer is in progress");

                try {
                    mRemoteDataTimeSeriesRepository.connectToServer();
                } catch (Exception e) {
                    setDataSyncIsInProgress(false);
                    Log.d(TAG, "Fail to connect to Server");
                    return;
                }

                Log.d(TAG, "Connection to server");

                sendAll();

                try {
                    mRemoteDataTimeSeriesRepository.closeServer();
                } catch (InterruptedException e) {
                    setDataSyncIsInProgress(false);
                    Log.d(TAG, "Fail to close server");
                }

                setDataSyncIsInProgress(false);
            }
        }, 0, 1, TimeUnit.MINUTES);

    }

    private boolean isWifiEnabled() {
        WifiManager wifi = (WifiManager) mApplicationContext.getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    /**
     * @brief stop data sync
     */

    public synchronized void stopDataSync(){
        Log.d(TAG, "stop data transfer");
        // data transfert is stopped only if not stopped already
        if(!mIsDataSyncEnabled.get()){
            return;
        }

        // force ending data transfer
        mIsDataSyncEnabled.set(false);

        if(mSocketWorkDoneBarrier != null){
            mSocketWorkDoneBarrier.countDown();
        }

        mScheduler.shutdown();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataAckNotification(DataAckNotification iAckNotification) {
        String lPacket = iAckNotification.getFileName();
        Log.d(TAG, "Delete File " + lPacket);
        try {
            mLocalDataRepository.removePhysioSignalSamples(lPacket);
            mPackets.remove(lPacket);
            EventBus.getDefault().post(new DataSyncUpdateStateNotification());

            // trigger a signal to close the data transfer
            if(mPackets.size() == 0){
                mSocketWorkDoneBarrier.countDown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSocketOnCloseNotification(SocketOnCloseNotification iCloseNotification) {
        if(mSocketWorkDoneBarrier != null) {
            mSocketWorkDoneBarrier.countDown();
        }
    }

    public ConcurrentLinkedQueue<String> mPackets;

    public ConcurrentLinkedQueue getPackets(){
        String lPath = mApplicationContext.getFilesDir().getPath();
        File lDirectory = new File(lPath);

        File[] lFiles = lDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String lDataFilePrefix = FileStorage.CACHE_FILENAME + FileStorage.PHYSIO_SIGNAL_SUFFIX;
                if(pathname.toString().contains(lDataFilePrefix)){
                    return true;
                }

                return false;
            }
        });

        if(lFiles == null || lFiles.length == 0){
            return null;
        }

        ConcurrentLinkedQueue<String> oFileNames = new ConcurrentLinkedQueue<String>();
        for (int i = 0; i < lFiles.length; i++)
        {
            oFileNames.add(lFiles[i].getName());
        }

        return oFileNames;
    }

    public void sendAll() {
        Log.d(TAG, "Start data transfer");

        mPackets = getPackets();

        mSocketWorkDoneBarrier = new CountDownLatch(1);

        if(mPackets == null || mPackets.size() == 0){
            return;
        }

        int i = 0;
        int lInitialPacketSize = mPackets.size();
        Log.d(TAG, "Data transfer - " + lInitialPacketSize);
        while (i <= lInitialPacketSize && mIsDataSyncEnabled.get()) {
            i++;

            String lPacket = mPackets.poll();

            if(lPacket == null){
                return;
            }

            try {
                String lData = mLocalDataRepository.queryPhysioSignalSamples(lPacket);
                mRemoteDataTimeSeriesRepository.save(lData);
            }
            catch (FileNotFoundException e){
                Log.d(TAG, "File does not exist anymore");
                e.printStackTrace();
            }
            catch (WebsocketNotConnectedException e){
                Log.d(TAG, "Fail to save data packet - socket not connected");
                e.printStackTrace();
                return;
            }
            catch (Exception e) {
                Log.d(TAG, "Fail to save data packet");
                e.printStackTrace();
            }

            mPackets.add(lPacket);
        }

        Log.d(TAG, "Packets has been sent");

        if(mPackets == null || mPackets.size() == 0 || !mIsDataSyncEnabled.get()){
            return;
        }

        Log.d(TAG, "Start Acknowledgment receiving");

        // wait for packet acknowledgment
        try {
            mSocketWorkDoneBarrier.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Log.d(TAG, "End of data transfer");
    }
}
