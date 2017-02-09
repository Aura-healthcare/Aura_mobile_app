package com.wearablesensor.aura;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by lecoucl on 08/01/17.
 */

@DynamoDBTable(tableName = "UserPrefs")
public class UserPrefs {
    private String user;
    private String lastSync;

    public UserPrefs()
    {
        this.user = "";
        this.lastSync = "";
    }
    public UserPrefs(String user, String lastSync){
        this.user = user;
        this.lastSync = lastSync;
    }

    @DynamoDBHashKey(attributeName = "User")
    public String getUser(){
        return this.user;
    }

    public void setUser(String user){
        this.user = user;
    }

    @DynamoDBAttribute(attributeName = "LastSync")
    public String getLastSync(){
        return this.lastSync;
    }

    public void setLastSync(String lastSync){
        this.lastSync = lastSync;
    }
}
