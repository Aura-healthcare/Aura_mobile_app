package com.wearablesensor.aura.device_pairing.notifications;

import com.wearablesensor.aura.data.SampleRRInterval;

/**
 * Created by lecoucl on 13/04/17.
 */
public class DevicePairingReceivedDataNotification extends DevicePairingNotification {
    private SampleRRInterval mSampleRrInterval;

    public DevicePairingReceivedDataNotification(SampleRRInterval iSampleRrInterval) {
        super(DevicePairingStatus.RECEIVED_DATA);
        mSampleRrInterval = iSampleRrInterval;
    }

    public SampleRRInterval getSampleRrInterval(){
        return mSampleRrInterval;
    }
}
