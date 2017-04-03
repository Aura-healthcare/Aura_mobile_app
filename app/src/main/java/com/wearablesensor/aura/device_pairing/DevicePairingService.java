package com.wearablesensor.aura.device_pairing;

import android.content.Context;
import android.util.Log;

import javax.inject.Inject;

/**
 * Created by lecoucl on 31/03/17.
 */
public class DevicePairingService {
    private final String TAG = this.getClass().getSimpleName();
    protected Context mContext;

    protected String mPairedDeviceName;
    protected String mPairedDeviceAddress;

    protected Boolean mPaired;

    @Inject
    public DevicePairingService(Context iContext){
        mContext = iContext;

        mPairedDeviceName = null;
        mPairedDeviceAddress = null;

        mPaired = false;
    }

    public void automaticPairing(){
        Log.d(TAG, "Start automatic Pairing");
    }

    public void startPairing(){
        Log.d(TAG, "start Pairing with Device: " + mPairedDeviceName + " - "+ mPairedDeviceAddress);
    }

    public void endPairing(){
        Log.d(TAG, "end Pairing");
        mPairedDeviceName = null;
        mPairedDeviceAddress = null;
    }

    public Boolean isPaired(){
        return mPaired;
    }
}
