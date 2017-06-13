package com.wearablesensor.aura.data_repository.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Date;
import java.util.UUID;

/**
 * Created by alyson on 26/04/17.
 */

@DynamoDBTable(tableName = "SensitiveEvent")
public class SeizureEventModel {
    public static final String SENSITIVE_EVENT_TYPE = "SensitiveEvent";

    private String mUuid; // sample data uuid
    private String mUser; // user concerned by sensitive event
    private String mTimestamp; // sample collected timestamp
    private String mSensitiveEventTimestamp; // sample sensitive event timestamp
    private String mComments; // sensitive event comments

    private String mType; // sample data type

    public SeizureEventModel(String iUuid, String iUser, String iTimestamp, String iSensitiveEventTimestamp, String iComments){
        mUuid = iUuid;
        mUser = iUser;
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mComments = iComments;

        mType = SENSITIVE_EVENT_TYPE;
    }

    public SeizureEventModel(String iUser, String iTimestamp, String iSensitiveEventTimestamp, String iComments){
        mUuid = UUID.randomUUID().toString();
        mUser = iUser;
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mComments = iComments;

        mType = SENSITIVE_EVENT_TYPE;
    }

    public SeizureEventModel(){
        mUuid = UUID.randomUUID().toString();
        mUser = "";
        mTimestamp = "";
        mSensitiveEventTimestamp = "";
        mComments = "";

        mType = SENSITIVE_EVENT_TYPE;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String iUuid) {
        mUuid = iUuid;
    }

    @DynamoDBIndexHashKey(attributeName = "User", globalSecondaryIndexName = "User-index")
    public String getUser(){
        return mUser;
    }

    public void setUser(String iUser){
        mUser = iUser;
    }

    @DynamoDBAttribute(attributeName = "CollectedTimestamp")
    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String iTimestamp) {
        mTimestamp = iTimestamp;
    }

    @DynamoDBIndexHashKey(attributeName = "EventTimestamp", globalSecondaryIndexName = "EventTimestamp-Index")
    public String getSensitiveEventTimestamp() {
        return mSensitiveEventTimestamp;
    }

    public void setSensitiveTimestamp(String iSensitiveTimestamp) {
        mSensitiveEventTimestamp = iSensitiveTimestamp;
    }

    @DynamoDBAttribute(attributeName = "Comments")
    public String getComments(){
        return mComments;
    }

    public void setComments(String iComments){
        mComments = iComments;
    }

    @DynamoDBAttribute(attributeName = "Type")
    public String getType(){
        return mType;
    }

    public void setType(String iType){
        mType = iType;
    }
}
