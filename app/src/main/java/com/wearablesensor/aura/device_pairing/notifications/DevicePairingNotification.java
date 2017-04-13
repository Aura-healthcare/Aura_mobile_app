package com.wearablesensor.aura.device_pairing.notifications;

public class DevicePairingNotification{

    protected DevicePairingStatus mStatus;

    public DevicePairingNotification(DevicePairingStatus iStatus){
        mStatus = iStatus;
    }

    public DevicePairingStatus getStatus(){
        return mStatus;
    }
}

