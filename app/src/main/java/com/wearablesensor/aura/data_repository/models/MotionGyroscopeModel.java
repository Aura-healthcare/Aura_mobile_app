/**
 * @file MotionGyroscopeModel.java
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
 * data model used to handle Gyroscope sample
 *
 */
package com.wearablesensor.aura.data_repository.models;

public class MotionGyroscopeModel extends PhysioSignalModel{

    private float[] mGyroscope; // gyroscope in the three axis x, y, z in deg/s

    public static final String MOTION_GYROSCOPE_MODEL = "MotionGyroscope";

    public MotionGyroscopeModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, float[] iGyroscope){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, MOTION_GYROSCOPE_MODEL);

        mGyroscope = iGyroscope;
    }

    public MotionGyroscopeModel(String iDeviceAdress, String iTimestamp, float[] iGyroscope){
        super(iDeviceAdress, iTimestamp, MOTION_GYROSCOPE_MODEL);

        mGyroscope = iGyroscope;
    }

    public float[] getGyroscope(){
        return mGyroscope;
    }

    public void setGyroscope(float[] iGyroscope){
        mGyroscope = iGyroscope;
    }

    @Override
    public String toString(){
        return super.toString() + " " +String.valueOf(mGyroscope[0]) + " " + String.valueOf(mGyroscope[1]) + " " + String.valueOf(mGyroscope[2]);
    }
}
