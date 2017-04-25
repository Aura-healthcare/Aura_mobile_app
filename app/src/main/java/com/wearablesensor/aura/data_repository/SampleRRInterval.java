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

package com.wearablesensor.aura.data_repository;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.UUID;

/**
 * Created by lecoucl on 10/12/16.
 */

@DynamoDBTable(tableName = "RRSamples")
public class SampleRRInterval {
    private String uuid;
    private String user;
    private String deviceAdress;
    private String timestamp;
    private int RR;

    public SampleRRInterval(String uuid, String user, String deviceAdress, String timestamp, int RR){
        this.uuid = uuid;
        this.user = user;
        this.deviceAdress = deviceAdress;
        this.timestamp = timestamp;
        this.RR = RR;
    }

    public SampleRRInterval(String user, String deviceAdress, String timestamp, int RR){
        this.uuid = UUID.randomUUID().toString();
        this.user = user;
        this.deviceAdress = deviceAdress;
        this.timestamp = timestamp;
        this.RR = RR;
    }

    public SampleRRInterval(){
        this.uuid = UUID.randomUUID().toString();
        this.user = "";
        this.deviceAdress = "";
        this.timestamp = "";
        this.RR = 0;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid() {
        return uuid;
    }

    public String setUuid() {
        return uuid;
    }

    @DynamoDBAttribute(attributeName = "User")
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @DynamoDBAttribute(attributeName = "DeviceAdress")
    public String getDeviceAdress(){ return this.deviceAdress; }

    public void setDeviceAdress(String deviceAdress){ this.deviceAdress = deviceAdress; }

    @DynamoDBAttribute(attributeName = "Timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName = "RR")
    public int getRR() {
        return RR;
    }

    public void setRR(int RR) {
        this.RR = RR;
    }
}
