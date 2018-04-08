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
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.github.pwittchen.reactivewifi.ReactiveWifi;
import com.github.pwittchen.reactivewifi.WifiSignalLevel;
import com.github.pwittchen.reactivewifi.WifiState;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataFileRepository;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_sync.notifications.DataSyncEndNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncNoSignalNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncStartNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;
import com.wearablesensor.aura.user_session.UserPreferencesModel;
import com.wearablesensor.aura.user_session.UserSessionService;
import com.wearablesensor.aura.utils.Timer;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DataSyncService{
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository.TimeSeries mRemoteDataTimeSeriesRepository;

    private Boolean mIsWifiEnabled;
    private Boolean mIsDataSyncEnabled;
    private Boolean mIsDataSyncInProgress;

    private Subscription mWifiStateChangeSubscription;
    private Subscription mWifiSignalLevelChangeSubscription;

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

        mIsWifiEnabled = false;
        mIsDataSyncEnabled = false;
        setDataSyncIsInProgress(false);

    }

    /**
     * @brief initialize data sync service by enabling observer on Wifi state
     */

    public void initialize(){
        mWifiStateChangeSubscription = ReactiveWifi.observeWifiStateChange(mApplicationContext)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Action1<WifiState>() {
                                                        @Override public void call(WifiState wifiState) {
                                                            if(wifiState.equals(WifiState.DISABLED)){
                                                                Log.d(TAG, "Wifi Disable");
                                                                mIsWifiEnabled = false;

                                                                EventBus.getDefault().post(new DataSyncNoSignalNotification());

                                                                stopDataSync();
                                                            }
                                                            else if(wifiState.equals(WifiState.ENABLED)){
                                                                mIsWifiEnabled = true;
                                                                Log.d(TAG, "Wifi Enable");
                                                            }
                                                        }
                                                    });


        mWifiSignalLevelChangeSubscription = ReactiveWifi.observeWifiSignalLevel(mApplicationContext)
                                                         .subscribeOn(Schedulers.io())
                                                         .observeOn(AndroidSchedulers.mainThread())
                                                         .subscribe(new Action1<WifiSignalLevel>() {
                                                            @Override public void call(WifiSignalLevel signalLevel) {

                                                                if(!mIsWifiEnabled){
                                                                    return;
                                                                }

                                                                 Log.d(TAG, "Wifi Signal Level - " + signalLevel);
                                                                 startDataSync();
                                                            }
                                                        });
    }

    /**
     * @brief close service, detach observers on Wifi state and stop data transfer
     */
    public void close(){
        stopDataSync();

        if (mWifiStateChangeSubscription != null && !mWifiStateChangeSubscription.isUnsubscribed()) {
            mWifiStateChangeSubscription.unsubscribe();
        }

        if (mWifiSignalLevelChangeSubscription != null && !mWifiSignalLevelChangeSubscription.isUnsubscribed()) {
            mWifiSignalLevelChangeSubscription.unsubscribe();
        }
    }

    /**
     * @brief setter for data sync is in progress
     *
     * @param iStatus progress status
     */
    public void setDataSyncIsInProgress(Boolean iStatus){
        mIsDataSyncInProgress = iStatus;

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
        return mIsDataSyncInProgress;
    }
    /**
     * @brief start data sync
     */

    public synchronized void startDataSync() {
        Log.d(TAG, "start data sync");
        // data transfert is started only if not started/transfering already
        if(mIsDataSyncEnabled || mIsDataSyncInProgress){
            return;
        }

        mIsDataSyncEnabled = true;
        setDataSyncIsInProgress(true);


        Log.d(TAG, "clear cache");
        try {
            mLocalDataRepository.clearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "push data packets - " + mIsDataSyncEnabled + " " + mIsDataSyncInProgress);
        new PushDataPacketsOnRemoteAsync().execute();
    }

    /**
     * @brief stop data sync
     */

    private synchronized void stopDataSync(){
        Log.d(TAG, "stop data transfer");
        // data transfert is stopped only if not stopped already
        if(!mIsDataSyncEnabled){
            return;
        }

        mIsDataSyncEnabled = false;
    }

    /**
     * @brief asynchronous task that handles the data packets push on a background thread
     */
    // TODO: we should implement a Loader and cache/remote data sync logic
    class PushDataPacketsOnRemoteAsync extends AsyncTask<Void, Integer, Boolean> {
        final private String TAG = PushDataPacketsOnRemoteAsync.class.getSimpleName();

        private PowerManager.WakeLock mWakeLock;
        private String[] mPackets;


        public String[] getPackets(){
            String lPath = mApplicationContext.getFilesDir().getPath();
            File lDirectory = new File(lPath);
            File[] lFiles = lDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.toString().contains(LocalDataFileRepository.CACHE_FILENAME)){
                        return true;
                    }

                    return false;
                }
            });

            if(lFiles == null || lFiles.length == 0){
                return null;
            }
            String[] oFileNames = new String[lFiles.length];
            for (int i = 0; i < lFiles.length; i++)
            {
                oFileNames[i] = lFiles[i].getName();
            }

            Log.d(TAG, "File number " + oFileNames.length);
            return oFileNames;
        }

        protected void onPreExecute() {
            PowerManager powerManager = (PowerManager) mApplicationContext.getSystemService(mApplicationContext.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "DataSyncWakelockTag");
            mWakeLock.acquire();

            mPackets = getPackets();
        }

        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "doInBackground");

            while(mPackets != null && mPackets.length > 0 && mIsDataSyncEnabled) {

                String lPacket = mPackets[0];
                try {
                    Timer.init("start transfer");
                    Timer.logResult("start data package transfer "+ lPacket);

                    if(lPacket.contains(LocalDataFileRepository.PHYSIO_SIGNAL_SUFFIX)) {
                        final ArrayList<PhysioSignalModel> lPhysioSignalSamples = mLocalDataRepository.queryPhysioSignalSamples(lPacket);
                        Timer.logResult("query data  " + lPacket);
                        mRemoteDataTimeSeriesRepository.savePhysioSignalSamples(lPhysioSignalSamples);
                        Timer.logResult("save data " + lPacket);
                    }
                    else if(lPacket.contains(LocalDataFileRepository.SENSITIVE_EVENT_SUFFIX)) {
                        final ArrayList<SeizureEventModel> lSensitiveEvents = mLocalDataRepository.querySeizures(lPacket);
                        Timer.logResult("query data  " + lPacket);
                        mRemoteDataTimeSeriesRepository.saveSeizures(lSensitiveEvents);
                        Timer.logResult("save data " + lPacket);
                    }

                    mLocalDataRepository.removePhysioSignalSamples(lPacket);

                    mPackets = getPackets();
                    EventBus.getDefault().post(new DataSyncUpdateStateNotification());

                } catch (Exception e) {
                    Log.d(TAG, "Fail to save data packet");
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        protected void onPostExecute(Boolean iSuccess) {
            Timer.logResult("end transfer");

            setDataSyncIsInProgress(false);
            mWakeLock.release();
        }

    }

}
