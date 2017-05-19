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

import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;
import com.wearablesensor.aura.user_session.UserSessionService;


public class RealTimeDataProcessorService extends DevicePairingServiceObserver{

    private final String TAG = this.getClass().getSimpleName();

    private DevicePairingService mDevicePairingService;
    private UserSessionService mUserSessionService;

    private LocalDataRepository mLocalDataRepository;

    public RealTimeDataProcessorService(DevicePairingService iBluetoothDevicePairingService,
                                        LocalDataRepository iLocalDataRepository,
                                        UserSessionService iUserSessionService){
        mDevicePairingService = iBluetoothDevicePairingService;
        mLocalDataRepository = iLocalDataRepository;

        mUserSessionService = iUserSessionService;
    }

    public void init(){
        mDevicePairingService.addObserver(this);
    }

    @Override
    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();

        if(lStatus == DevicePairingStatus.RECEIVED_DATA){
            DevicePairingReceivedDataNotification lDevicePairingNotification = (DevicePairingReceivedDataNotification) iDevicePairingNotification;

            Log.d(TAG, "User Session info "+ mUserSessionService + " " + mUserSessionService.getUser().getUuid());
            // no user registered yet
            if(mUserSessionService == null || mUserSessionService.getUser() == null){
                return;
            }

            RRIntervalModel lRrIntervalModel = lDevicePairingNotification.getSampleRrInterval();
            lRrIntervalModel.setUser(mUserSessionService.getUser().getUuid());
            putSampleInCache(lDevicePairingNotification.getSampleRrInterval());
        }
    }

    private void putSampleInCache(RRIntervalModel iRrIntervalModel){
        try{
            mLocalDataRepository.cacheRRSample(iRrIntervalModel);
        }
        catch (Exception e){
            Log.d(TAG, "Fail to cache RrSample");
        }
    }

}
