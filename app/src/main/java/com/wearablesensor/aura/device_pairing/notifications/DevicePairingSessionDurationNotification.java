package com.wearablesensor.aura.device_pairing.notifications;

public class DevicePairingSessionDurationNotification extends DevicePairingNotification{

    private long mSessionDuration; //in milliseconds

    public DevicePairingSessionDurationNotification(DevicePairingStatus iStatus, long iSessionDuration){
        super(iStatus);
        mSessionDuration = iSessionDuration;
    }

    public long getSessionDuration(){
        return mSessionDuration;
    }
}

