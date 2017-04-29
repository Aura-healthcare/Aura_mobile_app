package com.wearablesensor.aura.user_session;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "UserPreferences")
public class UserPreferencesModel {
    private String mUserId;
    private String mLastSync;

    public UserPreferencesModel()
    {
        mUserId = "";
        mLastSync = "";
    }

    public UserPreferencesModel(String iUserId, String iLastSync){
        mUserId = iUserId;
        mLastSync = iLastSync;
    }

    @DynamoDBHashKey(attributeName = "UserId")
    public String getUserId(){
        return mUserId;
    }

    public void setUserId(String iUserId){
        this.mUserId = iUserId;
    }

    @DynamoDBAttribute(attributeName = "LastSync")
    public String getLastSync(){
        return mLastSync;
    }

    public void setLastSync(String iLastSync){
        mLastSync = iLastSync;
    }
}
