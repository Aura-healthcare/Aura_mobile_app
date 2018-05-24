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

    @Override
    public String toString(){
        return mTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhysioSignalModel that = (PhysioSignalModel) o;

        if (mUuid != null ? !mUuid.equals(that.mUuid) : that.mUuid != null) return false;
        if (mDeviceAdress != null ? !mDeviceAdress.equals(that.mDeviceAdress) : that.mDeviceAdress != null)
            return false;
        if (mUser != null ? !mUser.equals(that.mUser) : that.mUser != null) return false;
        if (mTimestamp != null ? !mTimestamp.equals(that.mTimestamp) : that.mTimestamp != null)
            return false;
        return mType != null ? mType.equals(that.mType) : that.mType == null;
    }

    @Override
    public int hashCode() {
        int result = mUuid != null ? mUuid.hashCode() : 0;
        result = 31 * result + (mDeviceAdress != null ? mDeviceAdress.hashCode() : 0);
        result = 31 * result + (mUser != null ? mUser.hashCode() : 0);
        result = 31 * result + (mTimestamp != null ? mTimestamp.hashCode() : 0);
        result = 31 * result + (mType != null ? mType.hashCode() : 0);
        return result;
    }
}