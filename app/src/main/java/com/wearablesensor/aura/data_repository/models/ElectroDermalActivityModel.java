/**
 * @file ElectroDermalActivityModel.java
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
 * data model used to handle ElectroDermalActivity sample
 *
 */
package com.wearablesensor.aura.data_repository.models;

public class ElectroDermalActivityModel extends PhysioSignalModel{

    private int mSensorOutputFrequency; // sensor output frenquency in Hz
    private int mElectroDermalActivity; // electro dermal activity aka skin conductance aka galvanic skin response in Ohm

    public static final String ELECTRO_DERMAL_ACTIVITY = "ElectroDermalActivity";

    public ElectroDermalActivityModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, int iSeonsorOutputFrequency, int iElectroDermalActivity){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, ELECTRO_DERMAL_ACTIVITY);

        mSensorOutputFrequency = iSeonsorOutputFrequency;
        mElectroDermalActivity = iElectroDermalActivity;
    }

    public ElectroDermalActivityModel(String iDeviceAdress, String iTimestamp, int iSensorOutputFrequency, int iElectroDermalActivity){
        super(iDeviceAdress, iTimestamp, ELECTRO_DERMAL_ACTIVITY);

        mSensorOutputFrequency = iSensorOutputFrequency;
        mElectroDermalActivity = iElectroDermalActivity;;
    }

    public int getSensorOutputFrequency(){
        return mSensorOutputFrequency;
    }

    public void setSensorOutputFrequency(int iSensorOutputFrequency){
        mSensorOutputFrequency = iSensorOutputFrequency;
    }

    public int getElectroDermalActivity(){
        return mElectroDermalActivity;
    }

    public void setElectroDermalActivity(int iElectroDermalActivity){
        mElectroDermalActivity = iElectroDermalActivity;
    }
}
