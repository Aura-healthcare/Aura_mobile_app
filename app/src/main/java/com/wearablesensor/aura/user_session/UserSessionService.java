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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;


import com.wearablesensor.aura.MainMenuActivity;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;

import java.util.Date;

public class UserSessionService {

    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext; /** application context */
    private Activity mActivity; /** user session launcher activity */

    private RemoteDataRepository.Session mRemoteDataRepository; /** remote database that stores user information */

    private UserModel mUserModel; /** user information */
    private UserPreferencesModel mUserPrefs; /** user preferences */

    private String mAuthToken; /** authentification token */
    private Boolean mIsFirstSignIn; /** first time sign-in flag */

    public final static String SHARED_PREFS_FILE = "UserSession";
    public final static String SHARED_PREFS_USER_UUID = "UUID";
    public final static String SHARED_PREFS_USER_AMAZON_ID = "AmazonId";
    public final static String SHARED_PREFS_USER_ALIAS = "Alias";
    /**
     * @brief constructor
     *
     * @param iRemoteDataRepository remote database that stores user information
     * @param iApplicationContext application context
     */
    public UserSessionService(RemoteDataRepository.Session iRemoteDataRepository,
                              Context iApplicationContext){
        mApplicationContext = iApplicationContext;

        mRemoteDataRepository = iRemoteDataRepository;

        mUserModel = new UserModel("0000-0000-0000-0000-0000", "", "");
        mUserPrefs = null;

        mAuthToken = "";
        mIsFirstSignIn = false;
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
}
