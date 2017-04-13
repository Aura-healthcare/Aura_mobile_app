package com.wearablesensor.aura.device_pairing.notifications;

/**
 * Created by lecoucl on 13/04/17.
 */
public class DevicePairingDisconnectedNotification extends DevicePairingNotification {
    public DevicePairingDisconnectedNotification() {
        super(DevicePairingStatus.DISCONNECTED);
    }
}
