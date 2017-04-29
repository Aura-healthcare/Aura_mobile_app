package com.wearablesensor.aura.user_session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.wearablesensor.aura.AuraApplication;
import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.SessionSignInActivity;
import com.wearablesensor.aura.SignInActivity;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_sync.DataSyncPresenter;

import java.util.Date;

/**
 * Created by lecoucl on 28/04/17.
 */
public class UserSessionService {

    private final String TAG = this.getClass().getSimpleName();

    private Context mApplicationContext;
    private Activity mActivity;

    private RemoteDataRepository mRemoteDataRepository;

    private UserModel mUserModel;
    private UserPreferencesModel mUserPrefs;

    private String mAuthToken;
    private Boolean mIsFirstSignIn;

    public UserSessionService(RemoteDataRepository iRemoteDataRepository,
                              Context iApplicationContext){
        mApplicationContext = iApplicationContext;

        mRemoteDataRepository = iRemoteDataRepository;

        mUserModel = null;
        mUserPrefs = null;

        mAuthToken = "";
        mIsFirstSignIn = false;
    }

    public void initSession(String iAmazonId, Activity iActivity){
        try {
            mRemoteDataRepository.connect(mAuthToken);
        }catch (Exception e){
            closeSession(iActivity);
            return;
        }

        setCurrentActivity(iActivity);

        if(mIsFirstSignIn){
            registerUser(iAmazonId);
        }
        else {
            new LoadUserAsync().execute(iAmazonId);
        }
    }

    private void setCurrentActivity(Activity iActivity) {
        mActivity = iActivity;
    }

    private Activity getCurrentActivity(){
        return mActivity;
    }

    public void startSession(Activity iActivity){
        Intent intent = new Intent(mApplicationContext, SeizureMonitoringActivity.class);
        iActivity.startActivity(intent);
        iActivity.finish();
    }

    public void closeSession(Activity iActivity){
        mUserModel = null;
        mUserPrefs = null;

        Intent intent = new Intent(mApplicationContext, SignInActivity.class);
        iActivity.startActivity(intent);
        iActivity.finish();
    }

    public void setAuthToken(String iAuthToken) {
        this.mAuthToken = iAuthToken;
    }

    public void setIsFirstSignIn(Boolean lIsFirstSignIn) {
        this.mIsFirstSignIn = lIsFirstSignIn;
    }

    public void setUserPreferences(UserPreferencesModel iUserPrefs){
        mUserPrefs = iUserPrefs;
    }

    public UserPreferencesModel getUserPreferences() {
        return mUserPrefs;
    }

    class LoadUserAsync extends AsyncTask<String, Integer, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... iAmazonId) {
            try {
                mUserModel = mRemoteDataRepository.queryUser(iAmazonId[0]);
                mUserPrefs = mRemoteDataRepository.queryUserPreferences(mUserModel.getUuid());
                Log.d(TAG, "Success LoadUser" + mUserModel.getUuid() + " " + mUserModel.getAmazonId());

                return true;
            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "Fail LoadUser");
                return false;
            }
        }

        protected void onPostExecute(Boolean iLoadingSucceed) {
            if(iLoadingSucceed){
              startSession(getCurrentActivity());
            }
            else{
               closeSession(getCurrentActivity());
            }
        }
    }

    public void registerUser(String iAmazonId){
        UserModel lUser = new UserModel(iAmazonId);
        try {
            new RegisterUserAsync().execute(lUser);
        }catch (Exception e){
            Log.d(TAG, "");
        }
    }

    class RegisterUserAsync extends AsyncTask<UserModel, Integer, Boolean>
    {
        private String mRegisteredAmazonId;

        @Override
        protected Boolean doInBackground(UserModel... lUser) {
            try {
                mRemoteDataRepository.saveUser(lUser[0]);
                UserPreferencesModel lDefaultUserPreferencesModel = new UserPreferencesModel(lUser[0].getUuid(), "");
                mRemoteDataRepository.saveUserPreferences(lDefaultUserPreferencesModel);

                mRegisteredAmazonId = lUser[0].getAmazonId();
                Log.d(TAG, "Success RegisterUser" + lUser[0].getAmazonId() +" " +lUser[0].getUuid());
                return true;

            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "Fail RegisterUser");
                return false;
            }
        }

        protected void onPostExecute(Boolean iLoadingSucceed) {
            if(iLoadingSucceed){
                new LoadUserAsync().execute(mRegisteredAmazonId);
            }
            else{
                closeSession(getCurrentActivity());
            }
        }

    }
}
