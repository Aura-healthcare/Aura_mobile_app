package com.wearablesensor.aura.data_repository.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Date;
import java.util.UUID;

/**
 * Created by alyson on 26/04/17.
 */

@DynamoDBTable(tableName = "SeizureReportModel")
public class SeizureReportModel {
    private String uuid;
    private Date seizureDate;

    public SeizureReportModel(String uuid, Date seizureDate){
        this.uuid = uuid;
        this.seizureDate = seizureDate;
    }

    public SeizureReportModel(){
        this.uuid = UUID.randomUUID().toString();
        this.seizureDate = new Date();
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUuid() {
        return uuid;
    }

    public String setUuid() {
        return uuid;
    }

    @DynamoDBAttribute(attributeName = "seizureDate")
    public Date getUser() {
        return seizureDate;
    }

    public void setUser(Date date) {
        this.seizureDate = date;
    }
}
