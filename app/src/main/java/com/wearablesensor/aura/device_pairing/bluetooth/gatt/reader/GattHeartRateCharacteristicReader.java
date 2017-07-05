/**
 * @file GattCharacteristicReader.java
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
 * GattHeartRateCharacteristicReader is an helper that allows to parse a HeartRateGattProfile
 * and convert it into a physiological data model
 *
 */

package com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.wearablesensor.aura.device_pairing.bluetooth.BluetoothLeConstant;


public class GattHeartRateCharacteristicReader  implements GattCharacteristicReader{

    public final String TAG = this.getClass().getSimpleName();

    private int mHeartRate; // heart rate in bpm
    private int mEnergy; // energy expended in kJ
    private int mRrIntervalCount; // number of R-R interval samples
    private Integer[] mRrInterval; // R-R interval samples in ms

    private Boolean mHasBeenRead;

    /**
     * @brief constructor
     */
    public GattHeartRateCharacteristicReader(){
        mHeartRate = 0;
        mEnergy = 0;
        mRrIntervalCount = 0;

        mHasBeenRead = false;
    }


    /**
     * @brief helper method use to parse a GattCharacteristic and convert it into a
     * physiological data
     *
     * @param iGattCharacteristic gatt characteristic
     * @return true if read succeed, false otherwise
     */
    @Override
    public Boolean read(BluetoothGattCharacteristic iGattCharacteristic) {
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (mHasBeenRead) {
            return false;
        }

        if (!BluetoothLeConstant.UUID_HEART_RATE_MEASUREMENT.equals(iGattCharacteristic.getUuid())) {
            mHasBeenRead = true;
            return false;
        }

        int lFlag = iGattCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
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
        mHeartRate = iGattCharacteristic.getIntValue(lFormat, 1);
        Log.d(TAG, "Received heart rate:" + mHeartRate);
        if ((lFlag & 0x08) != 0) {
            // calories present
            mEnergy = iGattCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, lOffset);
            lOffset += 2;
            Log.d(TAG, "Received energy: " + energy);
        }
        else{
            mEnergy = 0;
        }

        if ((lFlag & 0x10) != 0) {
            // RR stuff.
            mRrIntervalCount = ((iGattCharacteristic.getValue()).length - lOffset) / 2;
            mRrInterval = new Integer[mRrIntervalCount];
            for (int i = 0; i < mRrIntervalCount; i++) {
                mRrInterval[i] = iGattCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, lOffset);
                lOffset += 2;
                Log.d(TAG, "Received RR: " + mRrInterval[i]);
            }

        }
        else{
            mRrIntervalCount = 0;
        }

        mHasBeenRead = true;
        return true;

    }

    /**
     * @brief getter
     *
     * @return heart rate in bpm(beat per minutes)
     */
    public int getHeartRate(){
        return mHeartRate;
    }

    /**
     * @brief getter
     *
     * @return energy expended in kJ
     */
    public int getEnergy(){
        return mEnergy;
    }

    /**
     * @brief getter
     *
     * @return first R-R interval in ms
     */
    public Integer getRrInterval(){
        if(mRrIntervalCount > 0) {
            return mRrInterval[0];
        }
        else{
            return 0;
        }
    }
}
