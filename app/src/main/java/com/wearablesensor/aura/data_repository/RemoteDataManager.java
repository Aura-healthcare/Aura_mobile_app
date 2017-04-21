package com.wearablesensor.aura.data_repository;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.wearablesensor.aura.UserPrefs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lecoucl on 14/12/16.
 */

public class RemoteDataManager {

    private final static String TAG = RemoteDataManager.class.getSimpleName();

    private AmazonDynamoDBClient mAmazonDynamoDBClient;
    private DynamoDBMapper mDynamoDBMapper;

    private String mUser;
    /** public constructor */
    public RemoteDataManager() {}

    protected void init(Context iApplicationContext, String iUser) {
        try {
            CognitoCachingCredentialsProvider lCredentialsProvider = new CognitoCachingCredentialsProvider(
                    iApplicationContext,    /* get the context for the application */
                    "eu-west-1:8dbf4eef-78e6-4ac9-9ace-fa164cd83538",    /* Identity Pool ID */
                    Regions.EU_WEST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );

            Map<String, String> lLogins = new HashMap<String, String>();
            Log.d(TAG, "Facebook token "+ AccessToken.getCurrentAccessToken().getToken());

            lLogins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            lCredentialsProvider.setLogins(lLogins);

            mAmazonDynamoDBClient = new AmazonDynamoDBClient(lCredentialsProvider);
            mAmazonDynamoDBClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
            mDynamoDBMapper = new DynamoDBMapper(mAmazonDynamoDBClient);
        } catch (Exception e) {
            Log.d(TAG, "DynamoDB initialization fail" + e.getMessage());
        }

        mUser = iUser;
    }

    protected void saveRRBatch(final List<SampleRRInterval> iBatchRR){

        Log.d(TAG, "SaveRR intervals" + iBatchRR.size());

        try{
            mDynamoDBMapper.batchSave(iBatchRR);
            Log.d(TAG, "Success Save Batch DynamoDB");
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error Save Batch DynamoDB" + e.getMessage());
            throw e;
        }
    }


    protected void saveLastSync(Date iLastSync){
        String iLastSyncStr = DateIso8601Mapper.getString(iLastSync);
        final UserPrefs lUserPrefs = new UserPrefs(mUser, iLastSyncStr);

        try{
            mDynamoDBMapper.save(lUserPrefs);
            Log.d(TAG, "Success Save UserPrefs");
        }
        catch(Exception e){
            Log.d(TAG, "Error Save UserPrefs" + e.getMessage());
            throw e;
        }
    }

    protected Date getLastSync() {
        UserPrefs lUserPrefs = null;
        try{
            lUserPrefs = mDynamoDBMapper.load(UserPrefs.class, mUser);
            return DateIso8601Mapper.getDate(lUserPrefs.getLastSync());
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error getLastSync" + e.getMessage());
            return null;
        }

    }
}
