/**
 * @file SeizureEventModel.java
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
 * data model used to describe seizure event
 */
package com.wearablesensor.aura.data_repository.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SeizureEventModel {
    public static final String SENSITIVE_EVENT_TYPE = "SensitiveEvent";

    private String mUuid; // sample data uuid
    private String mUser; // user concerned by sensitive event
    private String mTimestamp; // sample collected timestamp
    private String mSensitiveEventTimestamp; // sample sensitive event timestamp
    private String mIntensity; // sensitive event comments

    private String mType; // sample data type

    private HashMap<String, String> mAdditionalInformation;

    public SeizureEventModel(String iUuid, String iUser, String iTimestamp, String iSensitiveEventTimestamp, String iIntensity){
        mUuid = iUuid;
        mUser = iUser;
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mIntensity = iIntensity;

        mType = SENSITIVE_EVENT_TYPE;

        mAdditionalInformation = new HashMap<>();
    }

    public SeizureEventModel(String iUser, String iTimestamp, String iSensitiveEventTimestamp, String iIntensity){
        mUuid = UUID.randomUUID().toString();
        mUser = iUser;
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mIntensity = iIntensity;

        mType = SENSITIVE_EVENT_TYPE;

        mAdditionalInformation = new HashMap<>();
    }

    public SeizureEventModel(){
        mUuid = UUID.randomUUID().toString();
        mUser = "";
        mTimestamp = "";
        mSensitiveEventTimestamp = "";
        mIntensity = "";

        mType = SENSITIVE_EVENT_TYPE;

        mAdditionalInformation = new HashMap<>();
    }

    public String getUuid() {
        return mUuid;
    }
    public void setUuid(String iUuid) {
        mUuid = iUuid;
    }

    public String getUser(){
        return mUser;
    }
    public void setUser(String iUser){
        mUser = iUser;
    }

    public String getTimestamp() {
        return mTimestamp;
    }
    public void setTimestamp(String iTimestamp) {
        mTimestamp = iTimestamp;
    }

    public String getSensitiveEventTimestamp() {
        return mSensitiveEventTimestamp;
    }
    public void setSensitiveTimestamp(String iSensitiveTimestamp) {
        mSensitiveEventTimestamp = iSensitiveTimestamp;
    }

    public String getIntensity(){
        return mIntensity;
    }
    public void setIntensity(String iIntensity){
        mIntensity = iIntensity;
    }

    public String getType(){
        return mType;
    }
    public void setType(String iType){
        mType = iType;
    }

    public void addAdditionalInformation(String iQuestionTag, String iResultTag){
        mAdditionalInformation.put(iQuestionTag, iResultTag);
    }

    public Integer getNbAdditionalInformation(){
        return mAdditionalInformation.size();
    }

    public String toString(){
        String lDesc = mUuid + " " + mType + " " + mUser + " " + mTimestamp + " " + mSensitiveEventTimestamp + " " +mIntensity +"\n";
        for(Map.Entry<String, String> lEntry: mAdditionalInformation.entrySet()){
            lDesc+= lEntry.getKey() + ":" + lEntry.getValue() + " ";
        }

        return lDesc;
    }
}
