package com.wearablesensor.aura.authentification;

import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
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

    // Amazon Cognito Authentification attributes
    private AmazonCognitoAuthentificationHelper authentificationHelper;/** Amazon authentification API */
    private NewPasswordContinuation newPasswordContinuation; /** flag used to handle the new user 2-step validation */

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
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(UserId, signInPresenter.getCurrentPassword(), null);
        authenticationContinuation.setAuthenticationDetails(authenticationDetails);
        authenticationContinuation.continueTask();
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        /*
         * For Custom authentication challenge, implement your logic to present challenge to the
         * user and pass the user's responses to the continuation.
         */
        if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
            // This is the first sign-in attempt for an admin created user
            authentificationHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
                    newPasswordContinuation.getRequiredAttributes());
            signInPresenter.firstSignIn();
        }
    }

    @Override
    public void onFailure(Exception exception) {
        Log.d(TAG, "Auth Fail " + exception.toString());
        signInPresenter.signInFails(signInPresenter.getFailExtraMessage(exception));
    }

    @Override
    public void performAsyncAuthentication(String user) {
        authentificationHelper.getPool().getUser(user).getSessionInBackground(this);
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
        newPasswordContinuation.continueTask();
    }

    @Override
    public String getJWTToken() {
        return authentificationHelper.getCurrSession().getIdToken().getJWTToken();
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
