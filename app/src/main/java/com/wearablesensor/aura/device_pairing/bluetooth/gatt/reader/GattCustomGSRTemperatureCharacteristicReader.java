/**
 * @file GattCustomGSRTemperatureCharacteristicReader.java
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
 * GattHeartRateCharacteristicReader is an helper that allows to parse custom protocol provided by
 * MaximIntegrated MAXREFDES73 device
 *
 */

package com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;

public class GattCustomGSRTemperatureCharacteristicReader implements GattCharacteristicReader {

    private final String TAG = this.getClass().getSimpleName();

    private short mFrameNumber;
    private double mElectroDermalActivity; // galvanic skin response or electro dermal activity in microSiemens
    private float mTemperature; // temperature in degree celsius with a precision 0.01

    private Boolean mHasBeenRead;
    private static int FRAME_SIZE = 10; // number of byte in the frame

    public GattCustomGSRTemperatureCharacteristicReader(){
        mFrameNumber = 0;
        mTemperature = 0;
        mElectroDermalActivity = 0;

        mHasBeenRead = false;
    }

    /**
     * @brief helper method use to parse a GattCharacteristic and convert it into a
     * physiological data
     *
     * @param event gatt characteristic
     * @return true if read succeed, false otherwise
     */
    @Override
    public Boolean read(PhysioEvent event) {

        if (mHasBeenRead) {
            return false;
        }

        if (!Uuids.HEART_RATE_MEASUREMENT.equals(event.getUuid())) {
            mHasBeenRead = true;
            return false;
        }

        byte[] lData = event.getData();

        if(lData.length < FRAME_SIZE){
            mHasBeenRead = true;
            return false;
        }

        byte[] lTrameBytes = new byte[2];
        lTrameBytes[0] = lData[0];
        lTrameBytes[1] = lData[1];
        mFrameNumber = java.nio.ByteBuffer.wrap(lTrameBytes).getShort();


        int lIntegerTemperature = lData[4];
        int lDecimalTemperature = lData[5];
        mTemperature = lIntegerTemperature + (float)0.01 * lDecimalTemperature;

        byte[] lEDABytes = new byte[4];
        lEDABytes[0] = lData[6];
        lEDABytes[1] = lData[7];
        lEDABytes[2] = lData[8];
        lEDABytes[3] = lData[9];
        mElectroDermalActivity = 1000000 * 1.0 / java.nio.ByteBuffer.wrap(lEDABytes).getInt();

        Log.d(TAG, "Parse custom characteristic " + mFrameNumber + " " + mTemperature + " " + mElectroDermalActivity);
        mHasBeenRead = true;
        return true;
    }

    /**
     * @brief getter
     *
     * @return skin temperature in Celsius
     */
    public float getSkinTemperature(){
        return mTemperature;
    }

    /**
     * @brief getter
     *
     * @return electro dermal activity in microSiemens
     */
    public double getElectroDermalActivity(){
        return mElectroDermalActivity;
    }
}
