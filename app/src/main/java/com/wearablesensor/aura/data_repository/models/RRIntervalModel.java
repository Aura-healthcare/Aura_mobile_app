/**
 * @file RRIntervalModel.java
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
 * data model used to describe cardiac RR-interval sample
 */
package com.wearablesensor.aura.data_repository.models;

public class RRIntervalModel extends PhysioSignalModel {

    private int mRrInterval; // RR interval in ms
    public static final String RR_INTERVAL_TYPE = "RrInterval";

    public RRIntervalModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, int iRrInterval){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, RR_INTERVAL_TYPE);
        mRrInterval = iRrInterval;
    }

    public RRIntervalModel(String iDeviceAdress, String iTimestamp, int iRrInterval){
        super(iDeviceAdress, iTimestamp, RR_INTERVAL_TYPE);
        mRrInterval = iRrInterval;
    }

    public int getRrInterval() {
        return mRrInterval;
    }

    public void setRrInterval(int iRrInterval) {
        mRrInterval = iRrInterval;
    }

    public String toString(){
        return super.toString() + " " + String.valueOf(mRrInterval);
    }
}
