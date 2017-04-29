package com.wearablesensor.aura;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.wearablesensor.aura.authentification.AmazonCognitoAuthentificationHelper;
import com.wearablesensor.aura.user_session.UserSessionService;

/**
 * Created by lecoucl on 28/04/17.
 */
public class SessionSignInActivity extends AppCompatActivity{
    private final String TAG = this.getClass().getSimpleName();

    private final static String AMAZON_ID_ATTRIBUTE = "sub";

    private AmazonCognitoAuthentificationHelper mAuthentificationHelper;
    private UserSessionService mUserSessionService;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_session_sign_in);
        mAuthentificationHelper = ((AuraApplication) getApplication()).getAuthentificationHelper();
        mUserSessionService =((AuraApplication) getApplication()).getUserSessionService();

        Boolean lIsFirstSignIn = getIntent().getBooleanExtra("firstSignIn", false);
        mUserSessionService.setIsFirstSignIn(lIsFirstSignIn);

        String lAuthToken = getIntent().getStringExtra("AuthToken");
        mUserSessionService.setAuthToken(lAuthToken);

        String lAmazonId = mAuthentificationHelper.getCurrUser();
        Log.d(TAG, "startSession " + lIsFirstSignIn + " " +  lAuthToken + " " + lAmazonId);

        sessionSignIn(lAmazonId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAuthentificationProgressDialog();
    }

    private void sessionSignIn(String iAmazonId){
        displayAuthentificationProgressDialog();
       mUserSessionService.initSession(iAmazonId, this);
    }

    private void sessionSignInFail() {
        closeAuthentificationProgressDialog();
        displayFailLoginMessage();
    }

    public void displayAuthentificationProgressDialog() {
        mProgressDialog = new ProgressDialog(SessionSignInActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Enter Session...");
        mProgressDialog.show();
    }

    public void closeAuthentificationProgressDialog() {
        mProgressDialog.dismiss();
    }

    public void displayFailLoginMessage() {
        mAlertDialog = new AlertDialog.Builder(SessionSignInActivity.this)
                .setMessage("Login failed")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserSessionService.closeSession(SessionSignInActivity.this);
                    }
                }).create();
        mAlertDialog.show();
    }
}
