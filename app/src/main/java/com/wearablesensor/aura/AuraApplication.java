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

/**
 * Created by lecoucl on 29/03/17.
 */
public class AuraApplication extends Application{
    private DevicePairingService mDevicePairingService;
    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository mRemoteDataRepository;
    private RealTimeDataProcessorService mRealTimeDataProcessorService;

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
        mRemoteDataRepository = new RemoteDataDynamoDBRepository(lApplicationContext, mAuthentificationHelper);

        mRealTimeDataProcessorService = new RealTimeDataProcessorService(mDevicePairingService, mLocalDataRepository);

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
}
