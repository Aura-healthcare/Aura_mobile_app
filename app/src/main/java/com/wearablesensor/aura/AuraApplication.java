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

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.wearablesensor.aura.authentification.AmazonCognitoAuthentificationHelper;
import com.wearablesensor.aura.data_repository.LocalDataCouchbaseRepository;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataDynamoDBRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.real_time_data_processor.RealTimeDataProcessorService;
import com.wearablesensor.aura.user_session.UserSessionService;

public class AuraApplication extends Application{
    private DevicePairingService mDevicePairingService;
    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository mRemoteDataRepository;
    private RealTimeDataProcessorService mRealTimeDataProcessorService;
    private UserSessionService mUserSessionService;

    private AmazonCognitoAuthentificationHelper mAuthentificationHelper;

    @Override
    public void onCreate() {
        Log.d("AuraApplication", "Init");
        super.onCreate();

        Context lApplicationContext = getApplicationContext();
        boolean lIsBluetoothLeFeatureSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        BluetoothManager lBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mAuthentificationHelper = new AmazonCognitoAuthentificationHelper();
        mAuthentificationHelper.init(lApplicationContext);

        mDevicePairingService = new BluetoothDevicePairingService(lIsBluetoothLeFeatureSupported, lBluetoothManager, lApplicationContext);
        mLocalDataRepository = new LocalDataCouchbaseRepository(lApplicationContext);
        mRemoteDataRepository = new RemoteDataDynamoDBRepository(lApplicationContext);

        mUserSessionService = new UserSessionService(mRemoteDataRepository, lApplicationContext);

        mRealTimeDataProcessorService = new RealTimeDataProcessorService(mDevicePairingService, mLocalDataRepository, mUserSessionService);
        mRealTimeDataProcessorService.init();
    }

    public DevicePairingService getDevicePairingService() {
        return mDevicePairingService;
    }

    public LocalDataRepository getLocalDataRepository() {
        return mLocalDataRepository;
    }

    public RemoteDataRepository getRemoteDataRepository() {
        return mRemoteDataRepository;
    }

    public AmazonCognitoAuthentificationHelper getAuthentificationHelper() {return mAuthentificationHelper;}

    public UserSessionService getUserSessionService(){return mUserSessionService;}
}
