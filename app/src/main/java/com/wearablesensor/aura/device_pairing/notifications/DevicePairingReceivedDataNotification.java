package com.wearablesensor.aura.device_pairing.notifications;

import android.util.Log;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

public class DevicePairingReceivedDataNotification extends DevicePairingNotification {
    private PhysioSignalModel mPhysioSignalModel;

    public DevicePairingReceivedDataNotification(PhysioSignalModel iPhysioSignalModel) {
        super(DevicePairingStatus.RECEIVED_DATA);
        mPhysioSignalModel = iPhysioSignalModel;
        Log.d("RealTimeDataProcessor", "Physio Signal");
    }

    public PhysioSignalModel getPhysioSignal(){
        return mPhysioSignalModel;
    }
}
