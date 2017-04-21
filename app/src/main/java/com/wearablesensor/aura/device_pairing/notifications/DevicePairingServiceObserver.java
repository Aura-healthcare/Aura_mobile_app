package com.wearablesensor.aura.device_pairing.notifications;

import android.util.Log;

import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingServiceObserver implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof DevicePairingNotification){
            this.onDevicePairingServiceNotification((DevicePairingNotification) arg);
        }
    }

    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification){}
}
