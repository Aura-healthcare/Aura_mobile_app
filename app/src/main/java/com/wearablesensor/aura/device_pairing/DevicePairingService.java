/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.device_pairing;

import android.content.Context;

import java.util.LinkedList;
import java.util.Observable;

import android.os.Vibrator;
import android.util.Log;

import com.wearablesensor.aura.device_pairing.notifications.DevicePairingConnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDisconnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingInProgressNotification;

import org.greenrobot.eventbus.EventBus;

public class DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();

    private final static int PHONE_VIBRATION_DURATION = 1500; // in milliseconds
    protected Context mContext;

    protected Boolean mPaired;
    protected Boolean mIsPairing;


    public DevicePairingService(Context iContext){
        mContext = iContext;

        mPaired = false;
        mIsPairing = false;
    }

    public void automaticPairing(Context applicationContext){
        Log.d(TAG, "Start automatic Pairing");
        mIsPairing = true;

        EventBus.getDefault().post(new DevicePairingInProgressNotification());
    }

    public void startPairing(){
        Log.d(TAG, "start Pairing ");

        mPaired = true;
        mIsPairing = false;

        EventBus.getDefault().post(new DevicePairingConnectedNotification());

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(PHONE_VIBRATION_DURATION);


    }

    public void endPairing(){
        Log.d(TAG, "end Pairing");

        mPaired = false;
        mIsPairing = false;

        EventBus.getDefault().post(new DevicePairingDisconnectedNotification());

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(PHONE_VIBRATION_DURATION);
    }

    public Boolean isPaired(){
        return mPaired;
    }

    public Boolean isPairing(){ return mIsPairing; }

    public LinkedList<DeviceInfo> getDeviceList(){
        return new LinkedList<>();
    }

    public void close() {
        Log.d(TAG, "close Service");
    }
}
