/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.data_repository.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.UUID;

/**
 * Created by lecoucl on 10/12/16.
 */

@DynamoDBTable(tableName = "PhysioSignal")
public class RRIntervalModel extends PhysioSignalModel {

    private int mRrInterval; // RR interval in ms
    private static final String RR_INTERVAL_TYPE = "RrInterval";


    public RRIntervalModel(){

    }

    public RRIntervalModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, int iRrInterval){
        super(iUuid, iDeviceAdress, iUser, iTimestamp, RR_INTERVAL_TYPE);
        mRrInterval = iRrInterval;
    }

    public RRIntervalModel(String iDeviceAdress, String iUser, String iTimestamp, int iRrInterval){
        super(iDeviceAdress, iUser, iTimestamp, RR_INTERVAL_TYPE);
        mRrInterval = iRrInterval;
    }

    @DynamoDBAttribute(attributeName = "RrInterval")
    public int getRrInterval() {
        return mRrInterval;
    }

    public void setRrInterval(int iRrInterval) {
        mRrInterval = iRrInterval;
    }
}
