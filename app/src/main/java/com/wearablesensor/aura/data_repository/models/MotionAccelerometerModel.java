/**
 * @file MotionAccelerometerModel.java
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
 * data model used to handle Accelerometer sample
 *
 */
package com.wearablesensor.aura.data_repository.models;

public class MotionAccelerometerModel extends PhysioSignalModel{

    private float[] mAccelerometer; // accelerometer in the three axis x, y, z
    private String mAccelerationScale; // acceleration scale of the sensor

    public static final String MOTION_ACCELEROMETER_MODEL = "MotionAccelerometer";

    public MotionAccelerometerModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, float[] iAccelerometer, String iAccelerationScale){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, MOTION_ACCELEROMETER_MODEL);

        mAccelerometer = iAccelerometer;
        mAccelerationScale = iAccelerationScale;
    }

    public MotionAccelerometerModel(String iDeviceAdress, String iTimestamp, float[] iAccelerometer, String iAccelerationScale){
        super(iDeviceAdress, iTimestamp, MOTION_ACCELEROMETER_MODEL);

        mAccelerometer = iAccelerometer;
        mAccelerationScale = iAccelerationScale;
    }

    public float[] getAccelerometer(){
        return mAccelerometer;
    }

    public void setAccelerometer(float[] iAccelerometer){
        mAccelerometer = iAccelerometer;
    }

    public String getAccelerationScale(){
        return mAccelerationScale;
    }

    public void setAccelerationScale(String iAccelerationScale){
        mAccelerationScale = iAccelerationScale;
    }

    @Override
    public String toString(){
        return super.toString() + " " +String.valueOf(mAccelerometer[0]) + " " + String.valueOf(mAccelerometer[1]) + " " + String.valueOf(mAccelerometer[2])+ " " + mAccelerationScale;
    }
}
