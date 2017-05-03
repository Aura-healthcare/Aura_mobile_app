/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.data_repository;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserPreferencesModel;
import com.wearablesensor.aura.authentification.AmazonCognitoAuthentificationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by lecoucl on 07/04/17.
 */
public class RemoteDataDynamoDBRepository implements RemoteDataRepository{
    private final String TAG = this.getClass().getSimpleName();

    private static final String DYNAMO_DB_IDENTITY_POOL_ID = "eu-west-1:8dbf4eef-78e6-4ac9-9ace-fa164cd83538";

    private Context mApplicationContext;
    private AmazonDynamoDBClient mAmazonDynamoDBClient;
    private DynamoDBMapper mDynamoDBMapper;

    public RemoteDataDynamoDBRepository(Context iApplicationContext){
        Log.d(TAG, "RemoteData DynamoDB repository init");
        mApplicationContext = iApplicationContext;
    }

    public void connect(String lAuthToken) throws Exception{

        Log.d(TAG, "MyToken - " + lAuthToken);

        try {
            CognitoCachingCredentialsProvider lCredentialsProvider = new CognitoCachingCredentialsProvider(
                    mApplicationContext,    /* get the context for the application */
                    DYNAMO_DB_IDENTITY_POOL_ID,    /* Identity Pool ID */
                    Regions.EU_WEST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );

            Map<String, String> lLogins = new HashMap<String, String>();

            lLogins.put("cognito-idp.eu-west-1.amazonaws.com/" + AmazonCognitoAuthentificationHelper.userPoolId, lAuthToken);
            lCredentialsProvider.setLogins(lLogins);

            mAmazonDynamoDBClient = new AmazonDynamoDBClient(lCredentialsProvider);
            mAmazonDynamoDBClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
            mDynamoDBMapper = new DynamoDBMapper(mAmazonDynamoDBClient);
        } catch (Exception e) {
            Log.d(TAG, "DynamoDB initialization fail" + e.getMessage());
            throw e;
        }
    }

    @Override
    public void saveRRSample(final ArrayList<RRIntervalModel> iRrSamples) throws Exception {
        Log.d(TAG, "save RR Samples: " + iRrSamples.size());

        try{
            mDynamoDBMapper.batchSave(iRrSamples);
            Log.d(TAG, "Success RR Samples DynamoDB");
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error RR Samples DynamoDB" + e.getMessage());
            throw e;
        }
    }

    @Override
    public UserModel queryUser(String iAmazonId) throws Exception{

        try{
            UserModel lUserModel = new UserModel();
            lUserModel.setAmazonId(iAmazonId);

            DynamoDBQueryExpression lQueryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(lUserModel)
                    .withConsistentRead(false);

            PaginatedQueryList<UserModel> lUserModelList = mDynamoDBMapper.query(UserModel.class, lQueryExpression);
            Log.d(TAG, "Query Fetched User Number: " + lUserModelList.size() + " " + iAmazonId );

            if(lUserModelList.size() == 1) {
                return lUserModelList.get(0);
            }
            else{
                throw new Exception();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error Fail GetUser" + e.getMessage());
            throw e;
        }
    }

    @Override
    public void saveUser(final UserModel iUserModel) throws Exception {
        try{
            mDynamoDBMapper.save(iUserModel);
            Log.d(TAG, "Success Save User");
        }
        catch(NotAuthorizedException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error Save User");
            throw e;
        }
    }

    @Override
    public UserPreferencesModel queryUserPreferences(String iUserId) {
        UserPreferencesModel lUserPrefsModel = null;
        try{
            lUserPrefsModel = mDynamoDBMapper.load(UserPreferencesModel.class, iUserId);
            Log.d(TAG, "Success GetUserPrefs");
            return lUserPrefsModel;
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error GetUserPrefs" + e.getMessage());
            throw e;
        }
    }

    @Override
    public void saveUserPreferences(final UserPreferencesModel iUserPreferencesModel) throws Exception {
        try{
            mDynamoDBMapper.save(iUserPreferencesModel);
            Log.d(TAG, "Success Save UserPreferences");
        }
        catch(Exception e){
            Log.d(TAG, "Error Save UserPreferences" + e.getMessage());
            throw e;
        }
    }
}
