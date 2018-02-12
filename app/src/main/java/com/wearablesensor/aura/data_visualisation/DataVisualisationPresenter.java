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

import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

public class DataVisualisationPresenter implements DataVisualisationContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final DataVisualisationContract.View mView;

    private LinkedList<Float> mTemperatureDataSeries;
    private LinkedList<Float> mEDADataSeries;
    private LinkedList<Float> mRRIntervalDataSeries;

    public DataVisualisationPresenter(DataVisualisationContract.View iView){
        mView = iView;
        mView.setPresenter(this);

        mTemperatureDataSeries = new LinkedList<>();
        mEDADataSeries = new LinkedList<>();
        mRRIntervalDataSeries = new LinkedList<>();

        EventBus.getDefault().register(this);
    }

    @Override
    public void start() {

    }

    public void cachePhysioSignal(PhysioSignalModel iPhysioSignal){
        if(iPhysioSignal.getType().equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            ElectroDermalActivityModel lElectroDermalActivity = (ElectroDermalActivityModel) iPhysioSignal;
            mEDADataSeries.addLast((float)lElectroDermalActivity.getElectroDermalActivity());
            if(mEDADataSeries.size() >= 10){
                mEDADataSeries.removeFirst();
            }
        }
        else if(iPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE)){
            RRIntervalModel lRRIntervalModel = (RRIntervalModel) iPhysioSignal;
            mRRIntervalDataSeries.addLast((float)(lRRIntervalModel.getRrInterval()));
            if(mRRIntervalDataSeries.size() >= 10){
                mRRIntervalDataSeries.removeFirst();
            }
        }
        else if(iPhysioSignal.getType().equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE)){
            SkinTemperatureModel lSkinTemperature = (SkinTemperatureModel) iPhysioSignal;
            mTemperatureDataSeries.addLast((float)lSkinTemperature.getTemperature());
            if(mTemperatureDataSeries.size() >= 10){
                mTemperatureDataSeries.removeFirst();
            }
        }
    }

    public void clearCache(){
        mTemperatureDataSeries.clear();
        mEDADataSeries.clear();
        mRRIntervalDataSeries.clear();
    }

    /**
     * @brief handle receiving a new data sample
     *
     * @param iPhysioSignal physiological data sample
     */
    @Override
    public void receiveNewPhysioSample(PhysioSignalModel iPhysioSignal) {
        mView.refreshPhysioSignalVisualisation(iPhysioSignal);
        cachePhysioSignal(iPhysioSignal);
        mView.updateDataSeriesStatus(iPhysioSignal.getType(), isDataSeriesValidate(iPhysioSignal));
    }

    private Boolean isDataSeriesValidate(PhysioSignalModel iPhysioSignal) {
        Log.d(TAG, "isDataSeriesValidate");
        if(iPhysioSignal.getType().equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            return isEDADataSeriesValidate((ElectroDermalActivityModel) iPhysioSignal);
        }
        else if(iPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE)){
            return isRRIntervalDataSeriesValidate((RRIntervalModel) iPhysioSignal);
        }
        else if(iPhysioSignal.getType().equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE)){
            return isTemperatureDataSeriesValidate((SkinTemperatureModel) iPhysioSignal);
        }

        return Boolean.FALSE;
    }

    private Boolean isTemperatureDataSeriesValidate(SkinTemperatureModel iPhysioSignal) {
        double lStandardDev = 0;
        double lAverage = 0;

        if(mTemperatureDataSeries.size() < 10){
            return Boolean.FALSE;
        }
        else{
            for(Float lTemp: mTemperatureDataSeries){
                lAverage += lTemp;
            }
            lAverage = lAverage / 10.0;

            for(Float lTemp: mTemperatureDataSeries){
                lStandardDev += (lTemp - lAverage)*(lTemp - lAverage);
            }
            lStandardDev = lStandardDev / 9.0;
            lStandardDev = Math.sqrt(lStandardDev);

            Log.d(TAG, "Temperature average:"+ lAverage +" standardDev:" + lStandardDev);
            if(lAverage < 30.0 || lAverage> 42.0){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }

    private Boolean isRRIntervalDataSeriesValidate(RRIntervalModel iPhysioSignal) {
        double lStandardDev = 0;
        double lAverage = 0;

        if(mRRIntervalDataSeries.size() < 10){
            return Boolean.FALSE;
        }
        else{
            for(Float lRR: mTemperatureDataSeries){
                lAverage += lRR;
            }
            lAverage = lAverage / 10.0;

            for(Float lRR: mRRIntervalDataSeries){
                lStandardDev += (lRR - lAverage)*(lRR - lAverage);
            }
            lStandardDev = lStandardDev / 9.0;
            lStandardDev = Math.sqrt(lStandardDev);

            Log.d(TAG, "RRInterval average:"+ lAverage +" standardDev:" + lStandardDev);
            if(lAverage < 500 || lAverage > 2000){
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }
    }

    private Boolean isEDADataSeriesValidate(ElectroDermalActivityModel iPhysioSignal) {
        Log.d(TAG, "isEDADataSeriesValidate");
        double lStandardDev = 0;
        double lAverage = 0;

        if(mEDADataSeries.size() < 10){
            return Boolean.FALSE;
        }
        else{
            for(Float lRR: mEDADataSeries){
                lAverage += lRR;
            }
            lAverage = lAverage / 10.0;

            for(Float lRR: mEDADataSeries){
                lStandardDev += (lRR - lAverage)*(lRR - lAverage);
            }
            lStandardDev = lStandardDev / 9.0;
            lStandardDev = Math.sqrt(lStandardDev);

            Log.d(TAG, "EDA average:"+ lAverage +" standardDev:" + lStandardDev);
            if(lAverage < 3.0 || (lStandardDev/lAverage) > 0.30){
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
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
