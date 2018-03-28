package com.wearablesensor.aura.authentification;

import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

import java.util.Map;

import lombok.Builder;

/**
 * Created by octo_tbr on 27/02/18.
 */

public class AuraAuthenticationHandler implements AuthenticationHandler, AuthenticationManager {

    private String TAG = this.getClass().getSimpleName();

    private SignInPresenter signInPresenter;

    private String user;
    private String password;

    private boolean isFirstSignIn;

    // Amazon Cognito Authentification attributes
    private AmazonCognitoAuthentificationHelper authentificationHelper;/** Amazon authentification API */
    private NewPasswordContinuation newPasswordContinuation;

    @Builder
    AuraAuthenticationHandler(SignInPresenter presenter, AmazonCognitoAuthentificationHelper authentificationHelper){
        this.signInPresenter = presenter;
        this.authentificationHelper = authentificationHelper;
    }

    @Override
    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
        Log.e(TAG, "Auth Success");
        authentificationHelper.setCurrSession(userSession);
        authentificationHelper.newDevice(newDevice);
        signInPresenter.signInSucceed();
    }

    @Override
    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String UserId) {
        authentificationHelper.setUser(UserId);
        // pass
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        // pass
    }

    @Override
    public void onFailure(Exception exception) {
        // always succeed
        signInPresenter.signInSucceed();
    }

    @Override
    public void performAsyncAuthentication(String user) {
        signInPresenter.signInSucceed();
    }

    @Override
    public void continueWithFirstSignIn() throws Exception {
        newPasswordContinuation.setPassword(authentificationHelper.getPasswordForFirstTimeLogin());
        Map<String, String> newAttributes = authentificationHelper.getUserAttributesForFirstTimeLogin();
        if (newAttributes != null) {
            for(Map.Entry<String, String> attr: newAttributes.entrySet()) {
                newPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
            }
        }
        isFirstSignIn = true;
        newPasswordContinuation.continueTask();
    }

    @Override
    public String getJWTToken() {
        return "QQQ";
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
