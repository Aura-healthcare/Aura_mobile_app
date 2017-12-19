/**
 * @file MotionMagnetometerModel.java
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
 * data model used to handle Magnetometer sample
 *
 */
package com.wearablesensor.aura.data_repository.models;

public class MotionMagnetometerModel extends PhysioSignalModel{

    private float[] mMagnetometer; // magnetometer in the three axis x, y, z in microTesla

    public static final String MOTION_MAGNETOMETER_MODEL = "MotionMagnetometer";

    public MotionMagnetometerModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, float[] iMagnetometer){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, MOTION_MAGNETOMETER_MODEL);

        mMagnetometer = iMagnetometer;
    }

    public MotionMagnetometerModel(String iDeviceAdress, String iTimestamp, float[] iMagnetometer){
        super(iDeviceAdress, iTimestamp, MOTION_MAGNETOMETER_MODEL);

        mMagnetometer = iMagnetometer;
    }

    public float[] getMagnetometer(){
        return mMagnetometer;
    }

    public void setMagnetometer(float[] iMagnetometer){
        mMagnetometer = iMagnetometer;
    }

    @Override
    public String toString(){
        return super.toString() + " " +String.valueOf(mMagnetometer[0]) + " " + String.valueOf(mMagnetometer[1]) + " " + String.valueOf(mMagnetometer[2]);
    }
}
