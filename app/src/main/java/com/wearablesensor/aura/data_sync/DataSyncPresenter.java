
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
 * DataSyncPresenter is the presentation component that handles action related to data sync with Cloud
 * It implements the DataSyncContract.Presenter interface
 *
 */

package com.wearablesensor.aura.data_sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.user_session.UserPreferencesModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataSyncPresenter implements DataSyncContract.Presenter {

    private final LocalDataRepository mLocalDataRepository;
    private final RemoteDataRepository mRemoteDataRepository;

    private final UserSessionService mUserSessionService; /** user session service is required to keep user preferences updated on data push */

    private final DataSyncContract.View mView;

    private final Context mApplicationContext;

    /**
     *
     * @param iLocalDataRepository local data repository that stored to-be-pushed data
     * @param iRemoteDataRepository remote data repository that need to be synced
     * @param iView UI component that displays data push state to user
     * @param iApplicationContext application context
     * @param iUserSessionService user session information
     */
    public DataSyncPresenter(LocalDataRepository iLocalDataRepository,
                             RemoteDataRepository iRemoteDataRepository,
                             DataSyncContract.View iView,
                             Context iApplicationContext,
                             UserSessionService iUserSessionService) {
        mLocalDataRepository = iLocalDataRepository;
        mRemoteDataRepository = iRemoteDataRepository;
        mView = iView;
        mApplicationContext = iApplicationContext;
        mUserSessionService = iUserSessionService;
        Log.d("DataSyncPresenter","constructor" );

        mView.setPresenter(this);
    }

    /**
     * @brief presenter initialisation method executed at the creation of the view fragment
     */
    @Override
    public void start() {
        Date lLastSync = getLastSync();
        mView.refreshLastSync(lLastSync);
    }

    /**
     * @brief callback triggered when user push data on Cloud
     */
    @Override
    public void pushData() {
        new PushDataOnRemoteAsync().execute();
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
            UserPreferencesModel lNewUserPrefs= new UserPreferencesModel(lFormerUserPrefs.getUserId(), DateIso8601Mapper.getString(iLastSync) );
            mUserSessionService.setUserPreferences(lNewUserPrefs);
            mRemoteDataRepository.saveUserPreferences(lNewUserPrefs);
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * @brief get last sync value from user preferences
     *
     * @return last sync date if exists, otherwise null
     */
    private Date getLastSync(){
        String lLastSync = mUserSessionService.getUserPreferences().getLastSync();
        return DateIso8601Mapper.getDate(lLastSync);
    }


    /**
     * @brief asynchronous task that handles the data push on a background thread
     */
    // TODO: we should implement a Loader and cache/remote data sync logic
    class PushDataOnRemoteAsync extends AsyncTask<Void, Integer, Boolean> {
        final private String TAG = PushDataOnRemoteAsync.class.getSimpleName();
        private Date mCurrentSync;
        private String mFailMessage;

        protected void onPreExecute() {
            Log.d(TAG, "OnPreExecute");
            mCurrentSync = null;
            mFailMessage = "";
            mView.startPushDataOnCloud();
        }

        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "doInBackground");

            try {
                publishProgress(1);
                mLocalDataRepository.clearCache();
                Calendar c = Calendar.getInstance();
                mCurrentSync = c.getTime();

                Date lLastSync = getLastSync();
                publishProgress(10);
                final ArrayList<RRIntervalModel> lRrSamples = mLocalDataRepository.queryRRSamples(lLastSync, mCurrentSync);
                publishProgress(20);
                final ArrayList<SeizureEventModel> lSensitiveEvents = mLocalDataRepository.querySeizures(lLastSync, mCurrentSync);
                publishProgress(30);

                mRemoteDataRepository.saveRRSample(lRrSamples);
                publishProgress(60);
                mRemoteDataRepository.saveSeizures(lSensitiveEvents);
                publishProgress(90);

                saveLastSync(mCurrentSync);
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Fail to save RR data");
                mFailMessage = e.toString();
                return false;
            }
        }


        protected void onProgressUpdate(Integer...a){
            Log.d(TAG , "onProgressUpdate");
            mView.refreshProgressPushDataOnCloud(a[0]);
        }


        protected void onPostExecute(Boolean iSuccess) {
            Log.d(TAG , "onPostExecute");
            if(iSuccess) {
                mView.refreshLastSync(mCurrentSync);
            }
            else{
                mView.displayFailMessageOnPushData(mApplicationContext, mFailMessage);
            }

            mView.endPushDataOnCloud();
        }

    }
}