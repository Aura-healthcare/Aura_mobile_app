/**
 * @file SkinTemperatureModel.java
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
 * data model used to describe skin temperature sample
 */

package com.wearablesensor.aura.data_repository.models;

public class SkinTemperatureModel extends PhysioSignalModel {
    private float mTemperature; // temperature in degree celsius

    public static final String SKIN_TEMPERATURE_TYPE = "SkinTemperature";

    public SkinTemperatureModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, float iTemperature){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, SKIN_TEMPERATURE_TYPE);
        mTemperature = iTemperature;
    }

    public SkinTemperatureModel(String iDeviceAdress, String iTimestamp, float iTemperature){
        super(iDeviceAdress, iTimestamp, SKIN_TEMPERATURE_TYPE);
        mTemperature = iTemperature;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public void setTemperature(float iTemperature) {
        mTemperature = iTemperature;
    }

    @Override
    public String toString(){
        return super.toString() + " " +String.valueOf(mTemperature);
    }
}
