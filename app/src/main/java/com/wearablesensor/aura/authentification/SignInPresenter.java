/**
 * @file
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 * SignInPresenter is the presentation component that handles actions related to user sign-in
 * It implements the SignInContract.Presenter interface
 */


package com.wearablesensor.aura.authentification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException;
import com.wearablesensor.aura.FirstSignInActivity;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.SessionSignInActivity;



public class SignInPresenter implements SignInContract.Presenter{

    private String TAG = this.getClass().getSimpleName();

    private SignInContract.View mView;
    private Context mApplicationContext; /** application context */
    private Activity mActivity; /** presenter starting activity */

    private static final int FIRST_TIME_SIGN_IN = 1;
    private Boolean mIsFirstSignIn; /** first time sign in flag */

    private String mCurrentPassword; // argument exist because AmazonCognito does not take it as input

    private AuraAuthenticationHandler authenticationHandler;

    /**
     * @brief constructor
     *
     * @param iView view attached to presenter as it is done in MVP architecture
     * @param iApplicationContext application context
     * @param iActivity presenter launcher activity
     * @param iAuthentificationHelper AmazonCognito user pool authentification API
     */
    public SignInPresenter(SignInContract.View iView,
                           Context iApplicationContext,
                           Activity iActivity,
                           AmazonCognitoAuthentificationHelper iAuthentificationHelper){
        mView = iView;
        mView.setPresenter(this);

        mApplicationContext = iApplicationContext;
        mActivity = iActivity;

        authenticationHandler = AuraAuthenticationHandler.builder()
                .authentificationHelper(iAuthentificationHelper)
                .presenter(this)
                .build();

        mIsFirstSignIn = false;
    }

    /**
     * @brief method executed at view creation
     */
    @Override
    public void start() {

    }

    /**
     * @brief user attempts to sign-in
     *
     * @param iUsername username credential provided to sign-in
     * @param iPassword password credential provided to sign-in
     */
    @Override
    public void signIn(String iUsername, String iPassword) {
        Log.d(TAG, "Login");
        if (!validate(iUsername, iPassword)) {
            return;
        }

        mView.disableLoginButton();
        mView.displayAuthentificationProgressDialog();

        authenticationHandler.setPassword(iPassword);
        authenticationHandler.setUser(iUsername);

        authenticationHandler.performAsyncAuthentication(iUsername);
    }

    /**
     * @brief authentification succeed, start user session
     */
    public void signInSucceed(){
        mView.enableLoginButton();
        mView.closeAuthentificationProgressDialog();

        Intent intent = new Intent(mApplicationContext, SessionSignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("firstSignIn", mIsFirstSignIn);
        String lAuthToken = authenticationHandler.getJWTToken();
        intent.putExtra("AuthToken", lAuthToken);
        mIsFirstSignIn = false;

        mApplicationContext.startActivity(intent);
        mActivity.finish();
    }

    /**
     * @brief authentification fails, go back to sign-in page and provides extra message to help user
     *
     * @param iFailExtraMessage message displayed to help user to solve sign-in issue
     */
    public void signInFails(String iFailExtraMessage){
        mView.displayFailLoginMessage(iFailExtraMessage);

        mView.enableLoginButton();
        mView.closeAuthentificationProgressDialog();
    }

    /**
     * @brief user credentials pre-validation before trying to authenticate
     *
     * @param iUsername username credential provided to sign-in
     * @param iPassword password credential provided to sign-in
     *
     * @return true if pre-validation succeed, otherwise display tooltip to user
     */
    @Override
    public boolean validate(String iUsername, String iPassword) {
        boolean valid = true;

        if (iUsername.isEmpty() || iUsername.length() < 3) {
            mView.displayValidationError(mApplicationContext.getResources().getString(R.string.signin_username_tooltip));
            valid = false;
        } else {
            mView.displayValidationError(null);
        }

        return valid;
    }

    /**
      * @brief attempt to validate user account on first sign-in
      */
    public void firstSignIn(){
        Intent newPasswordActivity = new Intent(mActivity, FirstSignInActivity.class);
        mActivity.startActivityForResult(newPasswordActivity, FIRST_TIME_SIGN_IN);
    }

    /**
     * @brief user account is validated on first sign-in
     */
    public void continueWithFirstSignIn(){
        try {
            mIsFirstSignIn = true;
            authenticationHandler.continueWithFirstSignIn();
        } catch (Exception e) {
            signInFails(getFailExtraMessage(e));
        }
    }

    /**
     * @brief setter
     *
     * @param iCurrentPassword password to be transmitted to Amazon authentification callback
     */
    private void setCurrentPassword(String iCurrentPassword) {
        mCurrentPassword = iCurrentPassword;
    }

    /**
     * @brief getter
     *
     * @return password transmitted to Amazon authentification callback
     */
    String getCurrentPassword(){
        return mCurrentPassword;
    }

    /**
     * @brief get information message to display to user following a fail sign-in attempt
     *
     * @param iException exception received during fail sign-in attempt
     *
     * @return information message to display to user
     */
    String getFailExtraMessage(Exception iException){
        if(iException.getClass() == AmazonClientException.class || iException.getClass() == CognitoInternalErrorException.class){
            return mApplicationContext.getResources().getString(R.string.fail_extra_message_no_internet);
        }
        else{
            return mApplicationContext.getResources().getString(R.string.fail_extra_message_invalid_credentials);
        }
    }

}
