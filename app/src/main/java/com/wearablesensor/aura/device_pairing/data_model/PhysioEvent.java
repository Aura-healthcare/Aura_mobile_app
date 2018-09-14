/**
 * @file DeviceInfo.java
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 *
 *
 */
package com.wearablesensor.aura.device_pairing.data_model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import java.util.UUID;

import static com.idevicesinc.sweetblue.utils.Uuids.HEART_RATE_MEASUREMENT;


public class PhysioEvent {
    private final String TAG = this.getClass().getSimpleName();

    private boolean isDataReceivedNotification = false;
    private byte[] data;
    private UUID uuid;
    private String macAddress;
    private int mHeartRate; // heart rate in bpm
    private int mEnergy; // energy expended in kJ
    private int mRrIntervalCount; // number of R-R interval samples
    private Integer[] mRrInterval; // R-R interval samples in ms

    public PhysioEvent(BleDevice.ReadWriteListener.ReadWriteEvent e){
        isDataReceivedNotification = e.wasSuccess() && e.type() == BleDevice.ReadWriteListener.Type.NOTIFICATION;

        data = e.characteristic().getValue();
        uuid = e.characteristic().getUuid();
        if(uuid.equals(HEART_RATE_MEASUREMENT)) {
            fillHeartBeatSpecificData(e.characteristic());
        }

        macAddress = e.device().getMacAddress();
    }

    public PhysioEvent(){}



    private void fillHeartBeatSpecificData(BluetoothGattCharacteristic characteristic) {
        int lFlag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        int lFormat = -1;
        int energy = -1;
        int lOffset = 1;
        if ((lFlag & 0x01) != 0) {
            lFormat = BluetoothGattCharacteristic.FORMAT_UINT16;
            lOffset = 3;
            Log.d(TAG, "Heart rate format UINT16.");
        } else {
            lFormat = BluetoothGattCharacteristic.FORMAT_UINT8;
            lOffset = 2;
            Log.d(TAG, "Heart rate format UINT8.");
        }
        mHeartRate = characteristic.getIntValue(lFormat, 1);
        Log.d(TAG, "Received heart rate:" + mHeartRate);
        if ((lFlag & 0x08) != 0) {
            // calories present
            mEnergy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, lOffset);
            lOffset += 2;
            Log.d(TAG, "Received energy: " + energy);
        }
        else{
            mEnergy = 0;
        }

        if ((lFlag & 0x16) != 0) {
            // RR stuff.
            mRrIntervalCount = ((characteristic.getValue()).length - lOffset) / 2;

            if(mRrIntervalCount > 0) {
                mRrInterval = new Integer[mRrIntervalCount];
                for (int i = 0; i < mRrIntervalCount; i++) {
                    Integer lRrInterval = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, lOffset);
                    mRrInterval[i] = (int)(lRrInterval * 1000.0 / 1024.0);
                    lOffset += 2;
                    Log.d(TAG, "Received RR: " + mRrInterval[i]);
                }
            }
        }
        else{
            mRrIntervalCount = 0;
        }
    }

    public boolean isDataReceivedNotification() {
        return isDataReceivedNotification;
    }

    public void setDataReceivedNotification(boolean dataReceivedNotification) {
        isDataReceivedNotification = dataReceivedNotification;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getmHeartRate() {
        return mHeartRate;
    }

    public void setmHeartRate(int mHeartRate) {
        this.mHeartRate = mHeartRate;
    }

    public int getmEnergy() {
        return mEnergy;
    }

    public void setmEnergy(int mEnergy) {
        this.mEnergy = mEnergy;
    }

    public int getmRrIntervalCount() {
        return mRrIntervalCount;
    }

    public void setmRrIntervalCount(int mRrIntervalCount) {
        this.mRrIntervalCount = mRrIntervalCount;
    }

    public Integer[] getmRrInterval() {
        return mRrInterval;
    }

    public void setmRrInterval(Integer[] mRrInterval) {
        this.mRrInterval = mRrInterval;
    }
}