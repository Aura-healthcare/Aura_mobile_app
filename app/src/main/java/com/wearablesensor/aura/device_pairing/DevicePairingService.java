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
import android.util.Log;

import com.wearablesensor.aura.device_pairing.notifications.DevicePairingConnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDisconnectedNotification;

public class DevicePairingService extends Observable{
    private final String TAG = this.getClass().getSimpleName();
    protected Context mContext;

    protected Boolean mPaired;


    public DevicePairingService(Context iContext){
        mContext = iContext;

        mPaired = false;
    }

    public void automaticPairing(){
        Log.d(TAG, "Start automatic Pairing");
    }

    public void startPairing(){
        Log.d(TAG, "start Pairing ");

        mPaired = true;

        this.setChanged();
        this.notifyObservers( new DevicePairingConnectedNotification() );
    }

    public void endPairing(){
        Log.d(TAG, "end Pairing");

        mPaired = false;

        this.setChanged();
        this.notifyObservers(new DevicePairingDisconnectedNotification());
    }

    public Boolean isPaired(){
        return mPaired;
    }

    public LinkedList<DeviceInfo> getDeviceList(){
        return new LinkedList<>();
    }
}
