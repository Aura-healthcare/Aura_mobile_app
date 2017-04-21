package com.wearablesensor.aura.data_visualisation;

import com.wearablesensor.aura.data_repository.SampleRRInterval;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import java.util.ArrayList;
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
    public void receiveNewHRVSample(SampleRRInterval iSampleRR) {
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
