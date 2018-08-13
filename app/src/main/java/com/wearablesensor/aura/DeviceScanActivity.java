/**
 * @file DeviceScanActivity
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
 */
package com.wearablesensor.aura;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.wearablesensor.aura.device_pairing.DevicePairingService;

import com.wearablesensor.aura.device_scan_details.DeviceScanDetailsFragment;
import com.wearablesensor.aura.device_scan_details.DeviceScanDetailsPresenter;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

public class DeviceScanActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private DataCollectorService mDataCollectorService;
    private DevicePairingService mDevicePairingService;

    private DeviceScanDetailsFragment mDeviceScanDetailsFragment;
    private DeviceScanDetailsPresenter mDeviceScanDetailsPresenter;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataCollectorService = ((DataCollectorService.LocalBinder)service).getService();
            mDevicePairingService = mDataCollectorService.getDevicePairingService();
            mDeviceScanDetailsPresenter.setDevicePairingService(mDevicePairingService);
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataCollectorService = null;
            mDevicePairingService = null;
            mDeviceScanDetailsPresenter.setDevicePairingService(null);
        }
    };

    private Boolean mIsDataCollectorBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_scan);

        loadUser();
        startDataCollector();

        mDeviceScanDetailsFragment = DeviceScanDetailsFragment.newInstance();
        mDeviceScanDetailsPresenter = new DeviceScanDetailsPresenter(mDevicePairingService, this,  mDeviceScanDetailsFragment);
        ButterKnife.bind(this);

        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();
        lTransaction.add(R.id.device_pairing_container, mDeviceScanDetailsFragment, DeviceScanDetailsFragment.class.getSimpleName());
        lTransaction.addToBackStack(null);
        lTransaction.commit();
    }

    /**
     * @brief bind to dataCollector
     */
    private void doBindService() {

        bindService(new Intent(getApplicationContext(), DataCollectorService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        mIsDataCollectorBound = true;
    }

    /**
     * @brief unbind the dataCollector
     */
    private void doUnbindService() {
        if (mIsDataCollectorBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsDataCollectorBound = false;
        }
    }

    /**
     * @brief initialize data collector
     */
    private void startDataCollector(){
        // no running Aura Data Collector service
        if(!isMyServiceRunning(DataCollectorService.class)){
                Intent startIntent = new Intent(DeviceScanActivity.this, DataCollectorService.class);
                startIntent.putExtra("UserUUID", ((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());
                startIntent.setAction(DataCollectorServiceConstants.ACTION.STARTFOREGROUND_ACTION);
                startService(startIntent);

                doBindService();
        }
        // running Aura Data Collector service but not binded to Activity
        else if(isMyServiceRunning(DataCollectorService.class) && mDataCollectorService == null){
            doBindService();
        }
    }

    /**
     * @brief check if the service is running
     *
     * @param iServiceClass service to monitor
     * @return true if service is already running
     */
    private boolean isMyServiceRunning(Class<?> iServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (iServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @brief load user information
     */
    private void loadUser() {
        SharedPreferences lSharedPref = getSharedPreferences(UserSessionService.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String lUserUUID = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_UUID, null);
        String lUserAmazonId = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_AMAZON_ID, "");
        String lUserAlias = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_ALIAS,"");

        if(lUserUUID != null) {
            ((AuraApplication) getApplication()).getUserSessionService().setUser(new UserModel(lUserUUID, lUserAmazonId, lUserAlias));
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNavigationEvent(NavigationNotification iNavigationEvent){
        switch (iNavigationEvent.getNavigationFlag()) {
            case NavigationConstants.NAVIGATION_SEIZURE_MONITORING:
                goToSeizureMonitoring();
                break;
        }
    }

    private void goToSeizureMonitoring() {
        Intent intent = new Intent(this, SeizureMonitoringActivity.class);
        startActivity(intent);

        this.finish();
    }

    @Override
    protected void onDestroy(){
        doUnbindService();
        super.onDestroy();
    }
}

