/**
 * @file PhysioSignalModel.java
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
 * abstract data model for physiological sigmal sample
 */
package com.wearablesensor.aura.data_repository.models;

import java.util.UUID;


public class PhysioSignalModel {
    protected String mUuid;          // sample data uuid
    protected String mDeviceAdress;  // device adress uuid
    protected String mUser;          // user uuid

    protected String mTimestamp;     // timestamp formatted on Iso8601

    protected String mType;          // data type

    protected PhysioSignalModel() {

    }

    protected PhysioSignalModel(String iDeviceAdress, String iTimestamp, String iType) {
        mUuid = UUID.randomUUID().toString();
        mDeviceAdress = iDeviceAdress;
        mUser = null;
        mTimestamp = iTimestamp;
        mType = iType;
    }

    protected PhysioSignalModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, String iType) {
        mUuid = iUuid;
        mDeviceAdress = iDeviceAdress;
        mUser = iUser;
        mTimestamp = iTimestamp;
        mType = iType;
    }

    public String getUuid(){
        return mUuid;
    }
    public void setUuid(String iUuid){
        mUuid = iUuid;
    }

    public String getDeviceAdress(){
        return mDeviceAdress;
    }
    public void setDeviceAdress(String iDeviceAdress){
        mDeviceAdress = iDeviceAdress;
    }

    public String getUser(){
        return mUser;
    }

    public void setUser(String iUser){
        mUser = iUser;
    }

    public String getTimestamp(){
        return mTimestamp;
    }
    public void setTimestamp(String iTimestamp){
        mTimestamp = iTimestamp;
    }

    public String getType(){
        return mType;
    }
    public void setType(String iType){
        mType = iType;
    }
}