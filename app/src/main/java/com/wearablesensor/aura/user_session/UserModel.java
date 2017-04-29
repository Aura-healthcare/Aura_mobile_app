package com.wearablesensor.aura.user_session;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.UUID;

/**
 * Created by lecoucl on 28/04/17.
 */
@DynamoDBTable(tableName = "Users")
public class UserModel {

    private String mUUID;
    private String mAmazonId; // corresponding to amazon 'sub' field provided by Cognito User Pool
    private String mAlias;

    public UserModel(){

    }

    public UserModel(String iAmazonId){
        mUUID = UUID.randomUUID().toString();
        mAmazonId = iAmazonId;
        mAlias = "";
    }

    public UserModel(String iUUID, String iAmazonId, String iAlias){
        mUUID = iUUID;
        mAmazonId = iAmazonId;
        mAlias = iAlias;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid() {
        return mUUID;
    }

    public void setUuid(String iUUID) {
        mUUID = iUUID;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName="AmazonId-index", attributeName = "AmazonId")
    public String getAmazonId(){
        return mAmazonId;
    }

    public void setAmazonId(String iAmazonId){
        mAmazonId = iAmazonId;
    }

    @DynamoDBAttribute(attributeName = "Alias")
    public String getAlias(){
        return mAlias;
    }

    public void setAlias(String iAlias){
        mAlias = iAlias;
    }
}
