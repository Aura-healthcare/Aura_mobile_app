package com.wearablesensor.aura.device_pairing.notifications;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

/**
 * Created by lecoucl on 13/04/17.
 */
public class DevicePairingReceivedDataNotification extends DevicePairingNotification {
    private RRIntervalModel mRrIntervalModel;

    public DevicePairingReceivedDataNotification(RRIntervalModel iRrIntervalModel) {
        super(DevicePairingStatus.RECEIVED_DATA);
        mRrIntervalModel = iRrIntervalModel;
    }

    public RRIntervalModel getSampleRrInterval(){
        return mRrIntervalModel;
    }
}
