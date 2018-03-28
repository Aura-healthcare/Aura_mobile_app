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

package com.wearablesensor.aura.real_time_data_processor;

import android.util.Log;

import com.wearablesensor.aura.DataCollectorService;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;
import com.wearablesensor.aura.real_time_data_processor.analyser.TimeSerieAnalyser;
import com.wearablesensor.aura.real_time_data_processor.analyser.TimeSerieAnalyserObserver;
import com.wearablesensor.aura.real_time_data_processor.analyser.TimeSerieState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class RealTimeDataProcessorService implements TimeSerieAnalyserObserver {

    private final String TAG = this.getClass().getSimpleName();

    private DevicePairingService mDevicePairingService;
    private String mUserUUID;

    private LocalDataRepository mLocalDataRepository;

    private TimeSerieAnalyser<Integer> rrIntervalAnalyser = TimeSerieAnalyser.<Integer>builder()
            .observationWindow(10)
            .metricType(MetricType.HEART_BEAT)
            .maxValue(1500) // 40 bpm
            .minValue(300) // 200 bpm
            .build();

    public RealTimeDataProcessorService(DevicePairingService iBluetoothDevicePairingService,
                                        LocalDataRepository iLocalDataRepository,
                                        String iUserUUID){
        mDevicePairingService = iBluetoothDevicePairingService;
        mLocalDataRepository = iLocalDataRepository;

        mUserUUID = iUserUUID;

        rrIntervalAnalyser.addObserver(this);
        EventBus.getDefault().register(this);
    }

    private void putSampleInCache(PhysioSignalModel iPhysioSignal){
        //TODO: Insert TimeSerieAnalyser here
        try{
            mLocalDataRepository.cachePhysioSignalSample(iPhysioSignal);
        }
        catch (Exception e){
            Log.d(TAG, "Fail to cache RrSample");
        }
    }


    /**
     * @brief method executed by observer class when receiving a device pairing notification event
     *
     * @param iDevicePairingNotification notification to be processed by observer class
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePairingEvent(DevicePairingNotification iDevicePairingNotification){
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();

        if(lStatus == DevicePairingStatus.RECEIVED_DATA){
            DevicePairingReceivedDataNotification lDevicePairingNotification = (DevicePairingReceivedDataNotification) iDevicePairingNotification;

            Log.d(TAG, "User Session info "+ mUserUUID);
            // no user registered yet
            if(mUserUUID == null || mUserUUID.isEmpty()){
                return;
            }

            PhysioSignalModel lPhysioSignal = lDevicePairingNotification.getPhysioSignal();
            if(lPhysioSignal instanceof RRIntervalModel){
                RRIntervalModel sig = (RRIntervalModel) lPhysioSignal;
                rrIntervalAnalyser.append(sig.getRrInterval());
            }
            lPhysioSignal.setUser(mUserUUID);
            putSampleInCache(lPhysioSignal);
        }
    }

    @Override
    public void finalize(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNewState(MetricType metric, TimeSerieState state) {
        EventBus.getDefault().post(TimeSerieEvent.builder().state(state).type(metric).build());
    }

    public void addMetricAnalyserObserver(DataCollectorService dataCollectorService) {
        rrIntervalAnalyser.addObserver(dataCollectorService);
    }
}
