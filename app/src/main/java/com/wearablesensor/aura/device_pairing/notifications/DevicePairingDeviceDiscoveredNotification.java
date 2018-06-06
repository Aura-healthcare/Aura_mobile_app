package com.wearablesensor.aura.device_pairing.notifications;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.LinkedList;

public class DevicePairingDeviceDiscoveredNotification extends DevicePairingNotification {

    private LinkedList<BleDevice> mDeviceList;

    public DevicePairingDeviceDiscoveredNotification(LinkedList<BleDevice> iDeviceList) {
        super(DevicePairingStatus.DEVICE_DISCOVERED);
        mDeviceList = iDeviceList;
    }

    public DevicePairingStatus getStatus(){
        return mStatus;
    }

    public LinkedList<BleDevice> getDeviceList(){
        return mDeviceList;
    }
}

