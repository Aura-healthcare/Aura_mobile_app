package com.wearablesensor.aura.device_pairing.notifications;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.LinkedList;

public class DevicePairingEndDiscoveryNotification extends DevicePairingNotification {

    private LinkedList<BleDevice> mDeviceList;

    public DevicePairingEndDiscoveryNotification(LinkedList<BleDevice> iDeviceList) {
        super(DevicePairingStatus.END_DISCOVERY);
        mDeviceList = iDeviceList;
    }

    public DevicePairingStatus getStatus(){
        return mStatus;
    }

    public LinkedList<BleDevice> getDeviceList(){
        return mDeviceList;
    }
}

