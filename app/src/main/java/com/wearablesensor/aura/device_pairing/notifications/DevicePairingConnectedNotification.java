package com.wearablesensor.aura.device_pairing.notifications;

public class DevicePairingConnectedNotification extends DevicePairingNotification {

    private String mDeviceName;
    private String mDeviceAdress;

    public DevicePairingConnectedNotification(String iDeviceName, String iDeviceAdress) {
        super(DevicePairingStatus.CONNECTED);
        mDeviceName = iDeviceName;
        mDeviceAdress = iDeviceAdress;
    }

    public String getDeviceName(){
        return mDeviceName;
    }

    public String getDeviceAdress(){
        return mDeviceAdress;
    }

}
