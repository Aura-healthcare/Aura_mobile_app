package com.wearablesensor.aura.data_repository.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Date;
import java.util.UUID;

/**
 * Created by alyson on 26/04/17.
 */

@DynamoDBTable(tableName = "SensitiveEvent")
public class SeizureEventModel {
    private String mUuid; // sample data uuid
    private String mTimestamp; // sample collected timestamp
    private String mSensitiveEventTimestamp; // sample sensitive event timestamp
    private String mComments; // sensitive event comments

    public SeizureEventModel(String iUuid, String iTimestamp, String iSensitiveEventTimestamp, String iComments){
        mUuid = iUuid;
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mComments = iComments;
    }

    public SeizureEventModel(String iTimestamp, String iSensitiveEventTimestamp, String iComments){
        mUuid = UUID.randomUUID().toString();
        mTimestamp = iTimestamp;
        mSensitiveEventTimestamp = iSensitiveEventTimestamp;
        mComments = iComments;
    }

    public SeizureEventModel(){
        mUuid = UUID.randomUUID().toString();
        mTimestamp = "";
        mSensitiveEventTimestamp = "";
        mComments = "";
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String iUuid) {
        mUuid = iUuid;
    }

    @DynamoDBAttribute(attributeName = "CollectedTimestamp")
    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String iTimestamp) {
        mTimestamp = iTimestamp;
    }

    @DynamoDBAttribute(attributeName = "EventTimestamp")
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
        mComments = iComments;;
    }
}
