package com.wearablesensor.aura.data;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AccessToken;
import com.wearablesensor.aura.UserPrefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by lecoucl on 07/04/17.
 */
@Singleton
public class RemoteDataDynamoDBRepository implements RemoteDataRepository{
    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;
    private AmazonDynamoDBClient mAmazonDynamoDBClient;
    private DynamoDBMapper mDynamoDBMapper;

    private String mUser;

    @Inject
    public RemoteDataDynamoDBRepository(Context iApplicationContext){
        Log.d(TAG, "RemoteData DynamoDB repository init");
        mApplicationContext = iApplicationContext;
    }

    public void connect(String iUser, String iAccesToken) throws Exception{
        try {
            CognitoCachingCredentialsProvider lCredentialsProvider = new CognitoCachingCredentialsProvider(
                    mApplicationContext,    /* get the context for the application */
                    "eu-west-1:8dbf4eef-78e6-4ac9-9ace-fa164cd83538",    /* Identity Pool ID */
                    Regions.EU_WEST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );

            Map<String, String> lLogins = new HashMap<String, String>();
            Log.d(TAG, "Facebook token "+ iAccesToken);

            lLogins.put("graph.facebook.com", iAccesToken);
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
    public void saveRRSample(final ArrayList<SampleRRInterval> iRrSamples) throws Exception {
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
    public Date queryLastSync() throws Exception {
        UserPrefs lUserPrefs = null;
        try{
            lUserPrefs = mDynamoDBMapper.load(UserPrefs.class, mUser);
            return DateIso8601Mapper.getDate(lUserPrefs.getLastSync());
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "Error getLastSync" + e.getMessage());
            throw e;
        }

    }

    @Override
    public void saveLastSync(Date iLastSync) throws Exception {
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
}
