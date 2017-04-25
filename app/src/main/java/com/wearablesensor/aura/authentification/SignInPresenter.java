package com.wearablesensor.aura.authentification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.wearablesensor.aura.FirstSignInActivity;
import com.wearablesensor.aura.SeizureMonitoringActivity;

import java.util.Map;

/**
 * Created by lecoucl on 21/04/17.
 */
public class SignInPresenter implements SignInContract.Presenter{

    private String TAG = this.getClass().getSimpleName();
    private SignInContract.View mView;
    private Context mApplicationContext;
    private Activity mActivity;
    private static final int FIRST_TIME_SIGN_IN = 1;

    // Amazon Cognito Authentification attribute
    private AmazonCognitoAuthentificationHelper mAuthentificationHelper;
    private NewPasswordContinuation mNewPasswordContinuation;

    private String mCurrentPassword; // argument exist because AmazonCognito does not take it as input


    public SignInPresenter(SignInContract.View iView, Context iApplicationContext, Activity iActivity, AmazonCognitoAuthentificationHelper iAuthentificationHelper){
        mView = iView;
        mView.setPresenter(this);

        mApplicationContext = iApplicationContext;
        mActivity = iActivity;

        mAuthentificationHelper = iAuthentificationHelper;
    }

    @Override
    public void start() {

    }

    @Override
    public void signIn(String iUsername, String iPassword) {
        Log.d(TAG, "Login");
        if (!validate(iUsername, iPassword)) {
            signInFails();
            return;
        }

        mView.disableLoginButton();
        mView.displayAuthentificationProgressDialog();

        setCurrentPassword(iPassword);
        mAuthentificationHelper.getPool().getUser(iUsername).getSessionInBackground(mAuthenticationHandler);

        // TODO: Implement your own authentication logic here.
        Log.d(TAG, "try to log on Amazon pool");

    }

    public void signInSucceed(){
        mView.enableLoginButton();
        mView.closeAuthentificationProgressDialog();

        Intent intent = new Intent(mApplicationContext, SeizureMonitoringActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApplicationContext.startActivity(intent);
        mActivity.finish();
    }

    public void signInFails(){
        mView.displayFailLoginMessage();

        mView.enableLoginButton();
        mView.closeAuthentificationProgressDialog();
    }

    @Override
    public boolean validate(String iUsername, String iPassword) {
        boolean valid = true;

        if (iUsername.isEmpty() || iUsername.length() < 3) {
            mView.displayValidationError("enter a valid email address");
            valid = false;
        } else {
            mView.displayValidationError(null);
        }

        return valid;
    }

    public void firstSignIn(){
        Intent newPasswordActivity = new Intent(mActivity, FirstSignInActivity.class);
        mActivity.startActivityForResult(newPasswordActivity, FIRST_TIME_SIGN_IN);
    }

    public void continueWithFirstSignIn(){
        mNewPasswordContinuation.setPassword(mAuthentificationHelper.getPasswordForFirstTimeLogin());
        Map<String, String> newAttributes = mAuthentificationHelper.getUserAttributesForFirstTimeLogin();
        if (newAttributes != null) {
            for(Map.Entry<String, String> attr: newAttributes.entrySet()) {
                Log.e(TAG, String.format("Adding attribute: %s, %s", attr.getKey(), attr.getValue()));
                mNewPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
            }
        }
        try {
            mNewPasswordContinuation.continueTask();
        } catch (Exception e) {
            signInFails();
        }
    }
    //
    AuthenticationHandler mAuthenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession iCognitoUserSession, CognitoDevice iDevice) {
            Log.e(TAG, "Auth Success");
            mAuthentificationHelper.setCurrSession(iCognitoUserSession);
            mAuthentificationHelper.newDevice(iDevice);
            signInSucceed();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation iAuthenticationContinuation, String iUsername) {
            mAuthentificationHelper.setUser(iUsername);
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(iUsername, getCurrentPassword(), null);
            iAuthenticationContinuation.setAuthenticationDetails(authenticationDetails);
            iAuthenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            //disable
        }

        @Override
        public void onFailure(Exception e) {
            Log.d(TAG, "Auth Fail");
            signInFails();
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            /**
             * For Custom authentication challenge, implement your logic to present challenge to the
             * user and pass the user's responses to the continuation.
             */
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                // This is the first sign-in attempt for an admin created user
                mNewPasswordContinuation = (NewPasswordContinuation) continuation;
                mAuthentificationHelper.setUserAttributeForDisplayFirstLogIn(mNewPasswordContinuation.getCurrentUserAttributes(),
                        mNewPasswordContinuation.getRequiredAttributes());
                firstSignIn();
            }
        }
    };

    private void setCurrentPassword(String iCurrentPassword) {
        mCurrentPassword = iCurrentPassword;
    }

    private String getCurrentPassword(){
        return mCurrentPassword;
    }
}
