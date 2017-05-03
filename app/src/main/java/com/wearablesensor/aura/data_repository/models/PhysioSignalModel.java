package com.wearablesensor.aura.data_repository.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.UUID;

/**
 * Created by lecoucl on 03/05/17.
 */
@DynamoDBTable(tableName = "PhysioSignal")
public class PhysioSignalModel {
    protected String mUuid;          // sample data uuid
    protected String mDeviceAdress;  // device adress uuid
    protected String mUser;          // user uuid

    protected String mTimestamp;     // timestamp formatted on Iso8601

    protected String mType;          // data type

    public PhysioSignalModel() {

    }

    public PhysioSignalModel(String iDeviceAdress, String iUser, String iTimestamp, String iType) {
        mUuid = UUID.randomUUID().toString();
        mDeviceAdress = iDeviceAdress;
        mUser = iUser;
        mTimestamp = iTimestamp;
        mType = iType;
    }

    public PhysioSignalModel(String iUuid, String iDeviceAdress, String iUser, String iTimestamp, String iType) {
        mUuid = iUuid;
        mDeviceAdress = iDeviceAdress;
        mUser = iUser;
        mTimestamp = iTimestamp;
        mType = iType;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid(){
        return mUuid;
    }

    public void setUuid(String iUuid){
        mUuid = iUuid;
    }

    @DynamoDBAttribute(attributeName = "DeviceAdress")
    public String getDeviceAdress(){
        return mDeviceAdress;
    }

    public void setDeviceAdress(String iDeviceAdress){
        mDeviceAdress = iDeviceAdress;
    }

    @DynamoDBIndexHashKey(attributeName = "User", globalSecondaryIndexName = "User-index")
    public String getUser(){
        return mUser;
    }

    public void setUser(String iUser){
        mUser = iUser;
    }

    @DynamoDBIndexHashKey(attributeName = "Timestamp", globalSecondaryIndexName = "Timestamp-index")
    public String getTimestamp(){
        return mTimestamp;
    }

    public void setTimestamp(String iTimestamp){
        mTimestamp = iTimestamp;
    }

    @DynamoDBIndexHashKey(attributeName = "Type", globalSecondaryIndexName = "Type-index")
    public String getType(){
        return mType;
    }

    public void setType(String iType){
        mType = iType;
    }
}