package com.wearablesensor.aura.device_pairing.notifications;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

public class DevicePairingReceivedDataNotification extends DevicePairingNotification {
    private PhysioSignalModel mPhysioSignalModel;

    public DevicePairingReceivedDataNotification(PhysioSignalModel iPhysioSignalModel) {
        super(DevicePairingStatus.RECEIVED_DATA);
        mPhysioSignalModel = iPhysioSignalModel;
    }

    public PhysioSignalModel getPhysioSignal(){
        return mPhysioSignalModel;
    }
}
