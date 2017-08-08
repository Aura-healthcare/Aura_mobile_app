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
import android.util.Log;

import com.github.pwittchen.reactivewifi.ReactiveWifi;
//import com.github.pwittchen.reactivewifi.WifiSignalLevel;
import com.github.pwittchen.reactivewifi.WifiState;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_sync.notifications.DataSyncEndNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncStartNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;
import com.wearablesensor.aura.user_session.UserPreferencesModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DataSyncService extends Observable{
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;

    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository.Session mRemoteDataSessionRepository;
    private RemoteDataRepository.TimeSeries mRemoteDataTimeSeriesRepository;
    private UserSessionService mUserSessionService;

    private Boolean mIsDataSyncEnabled;
    private Boolean mIsDataSyncInProgress;

    private Subscription mWifiStateChangeSubscription;

    /**
     * @brief constructor
     *
     * @param iLocalDataRepository local data repository
     * @param iRemoteDataSessionRepository remote data session repository
     * @param iRemoteDataTimeSeriesRepository remote data time series repository
     * @param iApplicationContext application context
     * @param iUserSessionService user session
     */
    public DataSyncService(LocalDataRepository iLocalDataRepository, RemoteDataRepository.Session iRemoteDataSessionRepository, RemoteDataRepository.TimeSeries iRemoteDataTimeSeriesRepository, Context iApplicationContext, UserSessionService iUserSessionService){
        mApplicationContext = iApplicationContext;

        mLocalDataRepository = iLocalDataRepository;
        mRemoteDataSessionRepository = iRemoteDataSessionRepository;
        mRemoteDataTimeSeriesRepository = iRemoteDataTimeSeriesRepository;
        mUserSessionService = iUserSessionService;

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
                                                                stopDataSync();
                                                            }
                                                            else if(wifiState.equals(WifiState.ENABLED)){
                                                                startDataSync();
                                                            }
                                                        }
                                                    });


        /*ReactiveWifi.observeWifiSignalLevel(mApplicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WifiSignalLevel>() {
                    @Override public void call(WifiSignalLevel signalLevel) {
                        if(signalLevel == WifiSignalLevel.NO_SIGNAL || signalLevel == WifiSignalLevel.POOR){
                            stopDataTransfert();
                        }
                        else{
                            startDataTransfert();
                        }
                    }
                });*/
    }

    /**
     * @brief close service, detach observers on Wifi state and stop data transfer
     */
    public void close(){
        stopDataSync();

        if (mWifiStateChangeSubscription != null && !mWifiStateChangeSubscription.isUnsubscribed()) {
            mWifiStateChangeSubscription.unsubscribe();
        }
    }

    /**
     * @brief setter for data sync is in progress
     *
     * @param iStatus progress status
     */
    public void setDataSyncIsInProgress(Boolean iStatus){
        mIsDataSyncInProgress = iStatus;

        this.setChanged();
        if(iStatus == true){
            this.notifyObservers( new DataSyncStartNotification() );
        }
        else {
            this.notifyObservers(new DataSyncEndNotification());
        }
    }

    /**
     * @brief start data sync
     */

    private synchronized void startDataSync() {
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
     * @brief update last sync attribute from user preferences
     *
     * @param iLastSync last sync value to apply
     * @throws Exception
     */

    private void saveLastSync(Date iLastSync) throws Exception {
        try {
            UserPreferencesModel lFormerUserPrefs = mUserSessionService.getUserPreferences();
            final UserPreferencesModel lNewUserPrefs= new UserPreferencesModel(lFormerUserPrefs.getUserId(), DateIso8601Mapper.getString(iLastSync) );
            mUserSessionService.setUserPreferences(lNewUserPrefs);
            Thread t1 = new Thread(new Runnable() {
                public void run()
                {
                    try {
                        mRemoteDataSessionRepository.saveUserPreferences(lNewUserPrefs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }});

            t1.start();

        }catch(Exception e){
            throw e;
        }
    }

    /**
     * @brief get last sync value from user preferences
     *
     * @return last sync date if exists, otherwise null
     */
    public Date getLastSync(){
        String lLastSync = mUserSessionService.getUserPreferences().getLastSync();
        return DateIso8601Mapper.getDate(lLastSync);
    }


    /**
     * @brief asynchronous task that handles the data packets push on a background thread
     */
    // TODO: we should implement a Loader and cache/remote data sync logic
    class PushDataPacketsOnRemoteAsync extends AsyncTask<Void, Integer, Boolean> {
        final private String TAG = PushDataPacketsOnRemoteAsync.class.getSimpleName();
        private Date mCurrentSync;
        private Date mFinalSync;

        /**
         * @brief get time window end
         *
         * @param iWindowStart time window start
         * @param iFinalSync current time equivalent to last window end to be updated
         * @return time window end
         */
        private Date getDateEndWindow(Date iWindowStart, Date iFinalSync){
            Date oWindowEnd;
            Calendar c = Calendar.getInstance();
            c.setTime(iWindowStart);
            c.add(Calendar.HOUR, 1);

            oWindowEnd = c.getTime();
            if(oWindowEnd.after(iFinalSync)){
                return iFinalSync;
            }
            else{
                return oWindowEnd;
            }

        }
        protected void onPreExecute() {
            Calendar c = Calendar.getInstance();
            mFinalSync = c.getTime();

            mCurrentSync = getLastSync();
            Log.d(TAG, "New data sync - interval time " + mCurrentSync + " - " + mFinalSync);

        }

        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "doInBackground");

            while(!mCurrentSync.equals(mFinalSync) && mIsDataSyncEnabled) {
                try {
                    Date lWindowStart = mCurrentSync;
                    Date lWindowEnd = getDateEndWindow(lWindowStart, mFinalSync);
                    Log.d(TAG, "data packet - time window " + lWindowStart + " - " + lWindowEnd);

                    final ArrayList<RRIntervalModel> lRrSamples = mLocalDataRepository.queryRRSamples(lWindowStart, lWindowEnd);
                    mRemoteDataTimeSeriesRepository.saveRRSample(lRrSamples);

                    final ArrayList<SeizureEventModel> lSensitiveEvents = mLocalDataRepository.querySeizures(lWindowStart, lWindowEnd);
                    mRemoteDataTimeSeriesRepository.saveSeizures(lSensitiveEvents);

                    mCurrentSync = lWindowEnd;

                    setChanged();
                    notifyObservers(new DataSyncUpdateStateNotification(mCurrentSync));

                } catch (Exception e) {
                    Log.d(TAG, "Fail to save data packet");
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        protected void onPostExecute(Boolean iSuccess) {
            Log.d(TAG, "End data sync - interval time " + mCurrentSync + " - " + mFinalSync);

            try{
                saveLastSync(mCurrentSync);
            }catch (Exception e){
                e.printStackTrace();
            }

            setDataSyncIsInProgress(false);

        }

    }

}
