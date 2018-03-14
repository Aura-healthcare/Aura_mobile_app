/**
 * @file GattMovementCharacteristicReader.java
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
 * GattMovementCharacteristicReader is an helper that allows to parse motion raw data
 *
 */

package com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;

public class GattMovementCharacteristicReader implements GattCharacteristicReader {

    private final String TAG = this.getClass().getSimpleName();

    private static int FRAME_SIZE = 13; // in bits

    private float[] mGyroscope; // get the gyroscope values in the three axis (x, y, z) in deg/s
    private float[] mAccelerometer; // get the accelerometer values in the three axis (x, y, z) in -2G -> 2G scale
    private float[] mMagnetometer; // get the magnetometer values in the three axis (x, y, z) in microTesla

    private Boolean mHasBeenRead;

    public GattMovementCharacteristicReader() {
        mGyroscope = new float[3];
        mAccelerometer = new float[3];
        mMagnetometer = new float[3];

        mHasBeenRead = false;
    }

    /**
     * @param event gatt characteristic
     * @return true if read succeed, false otherwise
     * @brief helper method use to parse a GattCharacteristic and convert it into a
     * physiological data
     */
    @Override
    public Boolean read(PhysioEvent event) {

        if (mHasBeenRead) {
            return false;
        }

        byte[] lData = event.getData();

        if (lData.length != FRAME_SIZE || lData[0] !='L') {
            mHasBeenRead = true;
            return false;
        }

        for (int i = 0; i < mAccelerometer.length; i++) {
            byte[] lAccBytes = new byte[2];

            lAccBytes[0] = lData[2 * i + 1];
            lAccBytes[1] = lData[2 * i + 2];
            short lAccShort = java.nio.ByteBuffer.wrap(lAccBytes).getShort();
            mAccelerometer[i] = (float) ((lAccShort * 1.0) / (32768 / 2));
        }

        for (int i = 0; i < mGyroscope.length; i++) {
            byte[] lGyroBytes = new byte[2];
            lGyroBytes[0] = lData[2 * i + 7];
            lGyroBytes[1] = lData[2 * i + 1 + 7];
            short lGyroShort = java.nio.ByteBuffer.wrap(lGyroBytes).getShort();
            mGyroscope[i] = (float) (lGyroShort * 1.0) / (65536 / 500);
        }

        Log.d(TAG, "Parse custom characteristic " + mGyroscope[0] + " " + mGyroscope[1] + " " + mGyroscope[2] + " \n" + mAccelerometer[0] + " " + mAccelerometer[1] + " " + mAccelerometer[2] + " \n" );
        mHasBeenRead = true;
        return true;
    }

    /**
     * @return accelerometer value along 3 axis (x, y, z) in G
     */
    public float[] getAccelerometer() {
        return mAccelerometer;
    }


    /**
     * @return gyroscope value along 3 axis (x, y, z) in deg/s
     */
    public float[] getGyroscope(){
        return mGyroscope;
    }


    /**
     * @return magnetometer value along 3 axis (x, y, z) in microTesla
     */
    public float[] getMagnetometer(){
        return mMagnetometer;
    }
}