package com.wearablesensor.aura.device_pairing.notifications;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.LinkedList;

public class DevicePairingStartDiscoveryNotification extends DevicePairingNotification {

    private LinkedList<BleDevice> mDeviceList;

    public DevicePairingStartDiscoveryNotification(LinkedList<BleDevice> iDeviceList) {
        super(DevicePairingStatus.START_DISCOVERY);
        mDeviceList = iDeviceList;
    }

    public DevicePairingStatus getStatus(){
        return mStatus;
    }

    public LinkedList<BleDevice> getDeviceList(){
        return mDeviceList;
    }
}

