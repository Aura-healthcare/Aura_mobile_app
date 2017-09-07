package com.wearablesensor.aura.device_pairing.notifications;

import com.wearablesensor.aura.device_pairing.DeviceInfo;

/**
 * Created by lecoucl on 05/09/17.
 */
public class DevicePairingBatteryLevelNotification extends DevicePairingNotification{
    private DeviceInfo mDeviceInfo;

    public DevicePairingBatteryLevelNotification(DeviceInfo iDeviceInfo) {
        super(DevicePairingStatus.RECEIVED_BATTERY_LEVEL);
        mDeviceInfo = iDeviceInfo;
    }

    public DeviceInfo getDeviceInfo(){
        return mDeviceInfo;
    }
}
