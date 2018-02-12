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

import android.util.Log;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DataVisualisationPresenter implements DataVisualisationContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final DataVisualisationContract.View mView;

    public DataVisualisationPresenter(DataVisualisationContract.View iView){
        mView = iView;
        mView.setPresenter(this);

        EventBus.getDefault().register(this);
    }

    @Override
    public void start() {

    }

    /**
     * @brief handle receiving a new data sample
     *
     * @param iPhysioSignal physiological data sample
     */
    @Override
    public void receiveNewPhysioSample(PhysioSignalModel iPhysioSignal) {
        mView.refreshPhysioSignalVisualisation(iPhysioSignal);
    }


    /**
     * @brief method executed by observer class when receiving a device pairing notification event
     *
     * @param iDevicePairingNotification notification to be processed by observer class
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePairingEvent(DevicePairingNotification iDevicePairingNotification){
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();

        if(lStatus == DevicePairingStatus.CONNECTED){
            mView.enablePhysioSignalVisualisation();
        }
        else if(lStatus == DevicePairingStatus.DISCONNECTED){
            mView.disablePhysioSignalVisualisation();
        }
        if(lStatus == DevicePairingStatus.RECEIVED_DATA){
            DevicePairingReceivedDataNotification lDevicePairingNotification = (DevicePairingReceivedDataNotification) iDevicePairingNotification;
            Log.d(TAG, "ReceivedData" + lDevicePairingNotification.getPhysioSignal().toString());
            receiveNewPhysioSample(lDevicePairingNotification.getPhysioSignal());
        }    }

    @Override
    public void finalize(){
        EventBus.getDefault().unregister(this);
    }
}
