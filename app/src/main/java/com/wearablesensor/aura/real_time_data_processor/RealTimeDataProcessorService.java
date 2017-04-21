package com.wearablesensor.aura.real_time_data_processor;

import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.SampleRRInterval;
import com.wearablesensor.aura.device_pairing.DevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

/**
 * Created by lecoucl on 13/04/17.
 */
public class RealTimeDataProcessorService extends DevicePairingServiceObserver{

    private final String TAG = this.getClass().getSimpleName();

    private DevicePairingService mDevicePairingService;
    private LocalDataRepository mLocalDataRepository;

    public RealTimeDataProcessorService(DevicePairingService iBluetoothDevicePairingService,
                                        LocalDataRepository iLocalDataRepository){
        mDevicePairingService = iBluetoothDevicePairingService;
        mLocalDataRepository = iLocalDataRepository;
    }

    public void init(){
        mDevicePairingService.addObserver(this);
    }


    @Override
    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();

        if(lStatus == DevicePairingStatus.RECEIVED_DATA){
            DevicePairingReceivedDataNotification lDevicePairingNotification = (DevicePairingReceivedDataNotification) iDevicePairingNotification;
            putSampleInCache(lDevicePairingNotification.getSampleRrInterval());
        }
    }

    private void putSampleInCache(SampleRRInterval iSampleRrInterval){
        // filter empty values
        if (iSampleRrInterval.getTimestamp() == "" && iSampleRrInterval.getRR() == 0) {
            return;
        }

        try {
            mLocalDataRepository.saveRRSample(iSampleRrInterval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
