/**
 * @file DataCollectorService
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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.wearablesensor.aura.data_repository.LocalDataFileRepository;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.real_time_data_processor.MetricType;
import com.wearablesensor.aura.real_time_data_processor.RealTimeDataProcessorService;
import com.wearablesensor.aura.real_time_data_processor.analyser.TimeSerieAnalyserObserver;
import com.wearablesensor.aura.real_time_data_processor.analyser.TimeSerieState;

public class DataCollectorService extends Service implements TimeSerieAnalyserObserver {
    private static final String TAG = DataCollectorService.class.getSimpleName();

    private LocalDataFileRepository mLocalDataRepository;
    private BluetoothDevicePairingService mDevicePairingService;
    private RealTimeDataProcessorService mRealTimeDataProcessorService;

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();



    public class LocalBinder extends Binder {
        DataCollectorService getService() {
            return DataCollectorService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();

        mLocalDataRepository = new LocalDataFileRepository(getApplicationContext());
        mDevicePairingService = new BluetoothDevicePairingService( getApplicationContext());
    }

        @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getAction() == null){

        }
        else if(intent.getAction().equals(DataCollectorServiceConstants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.d(TAG, "Service Start");

            Intent notificationIntent = new Intent(this, SeizureMonitoringActivity.class);
            notificationIntent.setAction(DataCollectorServiceConstants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            mRealTimeDataProcessorService = new RealTimeDataProcessorService(mDevicePairingService, mLocalDataRepository, intent.getExtras().getString("UserUUID"));
            mRealTimeDataProcessorService.addMetricAnalyserObserver(this);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.head_logo_very_small);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Aura")
                    .setTicker("Aura")
                    .setContentText("Data Collection")
                    .setSmallIcon(R.drawable.head_logo_very_small)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();

            startForeground(DataCollectorServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);

        }else if(intent.getAction().equals(DataCollectorServiceConstants.ACTION.STOPFOREGROUND_ACTION)){
            Log.d(TAG, "Stop Start");

            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    public BluetoothDevicePairingService getDevicePairingService(){
        return mDevicePairingService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy(){
        mDevicePairingService.close();
    }

    @Override
    public void onNewState(MetricType metric, TimeSerieState state) {
        if(metric!=MetricType.HEART_BEAT){
            return;
        }
        if(state == TimeSerieState.ANOMALY){
            notifyHeartBeatAnomaly();
        } else if(state == TimeSerieState.NORMAL){
            dropHeartBeatAnomalyNotification();
        }
    }

    private void dropHeartBeatAnomalyNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(DataCollectorServiceConstants.NOTIFICATION_ID.HEARTBEAT_NOTIFICATION_ID);
    }

    private void notifyHeartBeatAnomaly() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(DataCollectorServiceConstants.NOTIFICATION_ID.HEARTBEAT_NOTIFICATION_ID, heartBeatMetricIssueNotification());
    }

    private Notification heartBeatMetricIssueNotification(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.hrv_pulse_anomaly);
        String appName = getResources().getString(R.string.app_name);
        String heartBeatBadPosition = getResources().getString(R.string.heartbeat_metric_anomaly);
        return new NotificationCompat.Builder(this)
                .setContentTitle(appName)
                .setTicker(appName)
                .setContentText(heartBeatBadPosition)
                .setSmallIcon(R.drawable.hrv_pulse_anomaly)
                .setVibrate(new long[] { 1000L, 1000L})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true).build();
    }

}

