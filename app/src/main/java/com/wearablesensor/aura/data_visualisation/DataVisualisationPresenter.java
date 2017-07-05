/**
 * @file DataVisualisationPresenter.java
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
 *
 */

package com.wearablesensor.aura.data_visualisation;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import java.util.Calendar;
import java.util.Date;


public class DataVisualisationPresenter extends DevicePairingServiceObserver implements DataVisualisationContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final DevicePairingService mDevicePairingService;
    private final DataVisualisationContract.View mView;

    public DataVisualisationPresenter(DevicePairingService iDevicePairingService,
                                      DataVisualisationContract.View iView){
            mDevicePairingService = iDevicePairingService;

            mView = iView;
            mView.setPresenter(this);
    }

    @Override
    public void start() {
        listenDevicePairingObserver();
    }

    @Override
    public void receiveNewHRVSample(RRIntervalModel iSampleRR) {
        mView.refreshRRSamplesVisualisation(iSampleRR);
    }

    private void listenDevicePairingObserver(){
        mDevicePairingService.addObserver(this);
    }

    @Override
    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();

        if(lStatus == DevicePairingStatus.CONNECTED){
            mView.enableRRSamplesVisualisation();
        }
        else if(lStatus == DevicePairingStatus.DISCONNECTED){
            mView.disableRRSamplesVisualisation();
        }
        if(lStatus == DevicePairingStatus.RECEIVED_DATA){
            DevicePairingReceivedDataNotification lDevicePairingNotification = (DevicePairingReceivedDataNotification) iDevicePairingNotification;
                receiveNewHRVSample(lDevicePairingNotification.getSampleRrInterval());
        }
    }
}
