package com.wearablesensor.aura; /**
 * @file MainMenuActivity
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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainMenuActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.device_scan_button) Button mDeviceScanButton;
    @OnClick(R.id.device_scan_button)
    public void goToDeviceScan(View v){
        EventBus.getDefault().post(new NavigationNotification(NavigationConstants.NAVIGATION_DEVICE_SCANNING));
    }

    @BindView(R.id.seizure_monitoring_button) Button mSeizureMonitoringButton;
    @OnClick(R.id.seizure_monitoring_button)
    public void goToSeizureMonitoring(View v){
        EventBus.getDefault().post(new NavigationNotification(NavigationConstants.NAVIGATION_SEIZURE_MONITORING));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);
        EventBus.getDefault().register(this);

        ButterKnife.bind(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNavigationEvent(NavigationNotification iNavigationEvent){
        switch (iNavigationEvent.getNavigationFlag()) {
            case NavigationConstants.NAVIGATION_SEIZURE_MONITORING:
                goToSeizureMonitoring();
                break;
            case NavigationConstants.NAVIGATION_DEVICE_SCANNING:
                goToDeviceScanning();
                break;
        }
    }

    private void goToDeviceScanning() {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);

        this.finish();
    }

    private void goToSeizureMonitoring() {
        Intent intent = new Intent(this, SeizureMonitoringActivity.class);
        startActivity(intent);

        this.finish();
    }

    @Override
    protected void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}

