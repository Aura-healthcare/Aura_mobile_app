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


import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;

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
     * @brief helper method use to parse a PhysioEvents built from GattCharacteristic and convert it into a
     * physiological data
     *
     * @param event gatt characteristic event
     * @return true if read succeed, false otherwise
     */
    @Override
    public Boolean read(PhysioEvent event) {
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (mHasBeenRead) {
            return false;
        }

        if (!Uuids.HEART_RATE_MEASUREMENT.equals(event.getUuid())) {
            mHasBeenRead = true;
            return false;
        }

        if(event == null){
            return false;
        }

        mHeartRate = event.getmHeartRate();
        mEnergy = event.getmEnergy();
        mRrIntervalCount = event.getmRrIntervalCount();
        mRrInterval = event.getmRrInterval();

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
    public Integer[] getRrInterval(){
        if(mRrIntervalCount > 0) {
            return mRrInterval;
        }
        else{
            return null;
        }
    }
}
