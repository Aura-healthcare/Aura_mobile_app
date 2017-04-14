package com.wearablesensor.aura.real_time_data_caching;

import android.util.Log;

import com.wearablesensor.aura.data.LocalDataRepository;
import com.wearablesensor.aura.data.SampleRRInterval;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;
import com.wearablesensor.aura.utils.ApplicationScoped;

import javax.inject.Inject;

/**
 * Created by lecoucl on 13/04/17.
 */
@ApplicationScoped
public class RealTimeDataCachingService extends DevicePairingServiceObserver{

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothDevicePairingService mBluetoothDevicePairingService;
    private LocalDataRepository mLocalDataRepository;

    @Inject
    public RealTimeDataCachingService(BluetoothDevicePairingService iBluetoothDevicePairingService,
                                      LocalDataRepository iLocalDataRepository){
        mBluetoothDevicePairingService = iBluetoothDevicePairingService;
        mLocalDataRepository = iLocalDataRepository;
    }

    public void init(){
        mBluetoothDevicePairingService.addObserver(this);
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
        try {
            mLocalDataRepository.saveRRSample(iSampleRrInterval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
