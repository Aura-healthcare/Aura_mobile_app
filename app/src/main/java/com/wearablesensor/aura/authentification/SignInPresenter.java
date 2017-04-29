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

package com.wearablesensor.aura.authentification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.wearablesensor.aura.FirstSignInActivity;
import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.SessionSignInActivity;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.util.Map;


public class SignInPresenter implements SignInContract.Presenter{

    private String TAG = this.getClass().getSimpleName();
    private SignInContract.View mView;
    private Context mApplicationContext;
    private Activity mActivity;
    private UserSessionService mUserSessionService;

    private static final int FIRST_TIME_SIGN_IN = 1;
    private Boolean mIsFirstSignIn;
    // Amazon Cognito Authentification attribute
    private AmazonCognitoAuthentificationHelper mAuthentificationHelper;
    private NewPasswordContinuation mNewPasswordContinuation;

    private String mCurrentPassword; // argument exist because AmazonCognito does not take it as input
    private String mCurrentUsername;

    public SignInPresenter(SignInContract.View iView,
                           Context iApplicationContext,
                           Activity iActivity,
                           AmazonCognitoAuthentificationHelper iAuthentificationHelper,
                           UserSessionService iUserSessionService){
        mView = iView;
        mView.setPresenter(this);

        mApplicationContext = iApplicationContext;
        mActivity = iActivity;
        mUserSessionService = iUserSessionService;

        mAuthentificationHelper = iAuthentificationHelper;

        mIsFirstSignIn = false;
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
        mAuthentificationHelper.setUser(iUsername);

        mAuthentificationHelper.getPool().getUser(iUsername).getSessionInBackground(mAuthenticationHandler);
    }

    public void signInSucceed(){
        mView.enableLoginButton();
        mView.closeAuthentificationProgressDialog();

        Intent intent = new Intent(mApplicationContext, SessionSignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("firstSignIn", mIsFirstSignIn);
        String lAuthToken = mAuthentificationHelper.getCurrSession().getIdToken().getJWTToken();
        intent.putExtra("AuthToken", lAuthToken);
        mIsFirstSignIn = false;

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
            mIsFirstSignIn = true;
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
            Log.d(TAG, "Auth Fail " + e.toString());
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
