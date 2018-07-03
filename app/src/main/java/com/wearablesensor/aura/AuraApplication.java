/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura;

import com.crashlytics.android.Crashlytics;

import android.content.Context;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.soloader.SoLoader;
import com.wearablesensor.aura.authentification.AmazonCognitoAuthentificationHelper;
import com.wearablesensor.aura.data_repository.LocalDataFileRepository;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataDynamoDBRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataWebSocketRepository;
import com.wearablesensor.aura.data_sync.DataSyncService;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.net.URISyntaxException;

import io.fabric.sdk.android.Fabric;


public class AuraApplication extends MultiDexApplication {

    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository.Session mRemoteDataSessionRepository;

    private UserSessionService mUserSessionService;

    private AmazonCognitoAuthentificationHelper mAuthentificationHelper;

    @Override
    public void onCreate() {
        Log.d("AuraApplication", "Init");
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        SoLoader.init(this, false);

        Context lApplicationContext = getApplicationContext();
        mAuthentificationHelper = new AmazonCognitoAuthentificationHelper();
        mAuthentificationHelper.init(lApplicationContext);

        mLocalDataRepository = new LocalDataFileRepository(lApplicationContext);
        mRemoteDataSessionRepository = new RemoteDataDynamoDBRepository(lApplicationContext);

        mUserSessionService = new UserSessionService(mRemoteDataSessionRepository, lApplicationContext);
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        Log.d("AURA APP", "LOW MEMORY ");
        Crashlytics.getInstance().logException(new Exception("LowMemory"));
    }

    @Override
    public void onTrimMemory(int level){
        super.onTrimMemory(level);
        Log.d("AURA APP", "TRIM MEMORY " + level);
        Crashlytics.getInstance().logException(new Exception("TrimMemory - "+level));
    }

    public LocalDataRepository getLocalDataRepository() {
        return mLocalDataRepository;
    }

    public RemoteDataRepository.Session getRemoteDataSessionRepository() {
        return mRemoteDataSessionRepository;
    }

    public AmazonCognitoAuthentificationHelper getAuthentificationHelper() {return mAuthentificationHelper;}

    public UserSessionService getUserSessionService(){return mUserSessionService;}
}
