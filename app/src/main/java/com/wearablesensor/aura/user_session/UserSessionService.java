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
 * UserSessionService is a service class that handles user session lifecycle.
 * It provides the following functionnality:
 * - initialize and exit user session
 * - connect to remote database with Authentification token
 * - load user and user preferences at session initialisation
 * - store and provide acces to user preferences
 */


package com.wearablesensor.aura.user_session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.wearablesensor.aura.AuraApplication;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.SessionSignInActivity;
import com.wearablesensor.aura.SignInActivity;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_sync.DataSyncPresenter;

import java.util.Date;

public class UserSessionService {

    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext; /** application context */
    private Activity mActivity; /** user session launcher activity */

    private RemoteDataRepository mRemoteDataRepository; /** remote database that stores user information */

    private UserModel mUserModel; /** user information */
    private UserPreferencesModel mUserPrefs; /** user preferences */

    private String mAuthToken; /** authentification token */
    private Boolean mIsFirstSignIn; /** first time sign-in flag */

    /**
     * @brief constructor
     *
     * @param iRemoteDataRepository remote database that stores user information
     * @param iApplicationContext application context
     */
    public UserSessionService(RemoteDataRepository iRemoteDataRepository,
                              Context iApplicationContext){
        mApplicationContext = iApplicationContext;

        mRemoteDataRepository = iRemoteDataRepository;

        mUserModel = null;
        mUserPrefs = null;

        mAuthToken = "";
        mIsFirstSignIn = false;
    }

    /**
     * @brief attempt to start a user session
     *
     * @param iAmazonId Amazon unique user name
     * @param iActivity user session launcher activity
     */
    public void initSession(String iAmazonId, Activity iActivity){
        try {
            mRemoteDataRepository.connect(mAuthToken);
        }catch (Exception e){
            closeSession(iActivity, mApplicationContext.getResources().getString(R.string.fail_extra_message_no_internet));
            return;
        }

        setCurrentActivity(iActivity);

        if(mIsFirstSignIn){
            registerUser(iAmazonId);
        }
        else {
            new LoadUserAsync().execute(iAmazonId);
        }
    }

    /**
     * @brief setter
     *
     * @param iActivity user session launcher activity
     */
    private void setCurrentActivity(Activity iActivity) {
        mActivity = iActivity;
    }

    /**
     * @brief getter used to allow asynchronous tasks to get acces to current activity
     *
     * @return user session launcher activity
     */
    private Activity getCurrentActivity(){
        return mActivity;
    }

    public void startSession(Activity iActivity){
        Intent intent = new Intent(mApplicationContext, SeizureMonitoringActivity.class);
        iActivity.startActivity(intent);
        iActivity.finish();
    }

    public void closeSession(Activity iActivity, String iSessionSignInFailExtraMessage){
        mUserModel = null;
        mUserPrefs = null;

        Intent intent = new Intent(mApplicationContext, SignInActivity.class);
        intent.putExtra("sessionFailSignInExtraMessage", iSessionSignInFailExtraMessage);
        iActivity.startActivity(intent);
        iActivity.finish();
    }

    /**
     * @brief setter
     *
     * @param iAuthToken authentification token
     */
    public void setAuthToken(String iAuthToken) {
        this.mAuthToken = iAuthToken;
    }

    /**
     * @brief setter
     *
     * @param lIsFirstSignIn first time sign-in flag
     */
    public void setIsFirstSignIn(Boolean lIsFirstSignIn) {
        this.mIsFirstSignIn = lIsFirstSignIn;
    }

    /**
     * @brief setter
     *
     * @param iUserPrefs user preferences
     */
    public void setUserPreferences(UserPreferencesModel iUserPrefs){
        mUserPrefs = iUserPrefs;
    }

    /**
     * @brief getter
     *
     * @return user preferences
     */
    public UserPreferencesModel getUserPreferences() {
        return mUserPrefs;
    }

    /**
     * @brief setter
     *
     * @param iUserModel user information
     */
    public void setUser(UserModel iUserModel){
        mUserModel = iUserModel;
    }

    /**
     * @brief getter
     *
     * @return user information
     */
    public UserModel getUser(){
        return mUserModel;
    }

    /**
     * @brief load user and user preferences from remote database asynchronous task
     *
     */
    class LoadUserAsync extends AsyncTask<String, Integer, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... iAmazonId) {
            try {
                mUserModel = mRemoteDataRepository.queryUser(iAmazonId[0]);
                mUserPrefs = mRemoteDataRepository.queryUserPreferences(mUserModel.getUuid());
                Log.d(TAG, "Success LoadUser" + mUserModel.getUuid() + " " + mUserModel.getAmazonId());

                return true;
            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "Fail LoadUser");
                return false;
            }
        }

        protected void onPostExecute(Boolean iLoadingSucceed) {
            if(iLoadingSucceed){
              startSession(getCurrentActivity());
            }
            else{
               closeSession(getCurrentActivity(), mApplicationContext.getResources().getString(R.string.fail_extra_message_no_internet));
            }
        }
    }

    /**
     * @brief create and register a new user in remote database
     *
     * @param iAmazonId Amazon unique username
     */
    public void registerUser(String iAmazonId){
        UserModel lUser = new UserModel(iAmazonId);
        try {
            new RegisterUserAsync().execute(lUser);
        }catch (Exception e){
            Log.d(TAG, "");
        }
    }


    /**
     * @brief new user registering in remote database asynchronous task
     *
     */
    class RegisterUserAsync extends AsyncTask<UserModel, Integer, Boolean>
    {
        private String mRegisteredAmazonId;

        @Override
        protected Boolean doInBackground(UserModel... lUser) {
            try {
                mRemoteDataRepository.saveUser(lUser[0]);


                UserPreferencesModel lDefaultUserPreferencesModel = new UserPreferencesModel(lUser[0].getUuid(), DateIso8601Mapper.getString(new Date()));
                mRemoteDataRepository.saveUserPreferences(lDefaultUserPreferencesModel);

                mRegisteredAmazonId = lUser[0].getAmazonId();
                Log.d(TAG, "Success RegisterUser" + lUser[0].getAmazonId() +" " +lUser[0].getUuid());
                return true;

            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "Fail RegisterUser");
                return false;
            }
        }

        protected void onPostExecute(Boolean iLoadingSucceed) {
            if(iLoadingSucceed){
                new LoadUserAsync().execute(mRegisteredAmazonId);
            }
            else{
                closeSession(getCurrentActivity(), mApplicationContext.getResources().getString(R.string.fail_extra_message_no_internet));
            }
        }

    }
}
