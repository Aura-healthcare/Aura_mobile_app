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

package com.wearablesensor.aura.data_visualisation;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lecoucl on 21/04/17.
 */
public class DataVisualisationPresenter extends DevicePairingServiceObserver implements DataVisualisationContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final Integer GRAPH_WINDOW_SIZE = 1; // in minutes
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

        Calendar c = Calendar.getInstance();

        Date lCurrentTime = c.getTime();
        c.setTime(lCurrentTime);
        c.add(Calendar.MINUTE, GRAPH_WINDOW_SIZE);
        Date lWindowEnd = c.getTime();

        Date lWindowStart = lCurrentTime;
        mView.initRRSamplesVisualisation(lWindowStart, lWindowEnd);
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
