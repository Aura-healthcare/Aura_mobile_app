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

package com.wearablesensor.aura;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.wearablesensor.aura.authentification.SignInContract;
import com.wearablesensor.aura.authentification.SignInPresenter;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends AppCompatActivity implements SignInContract.View{
    private final String TAG = this.getClass().getSimpleName();
    private static final int REQUEST_SIGNUP = 0;
    private static final int FIRST_TIME_SIGN_IN = 1;

    private CallbackManager mFbCallbackManager;

    private SignInContract.Presenter mPresenter;

    private ProgressDialog mProgressDialog;
    protected AlertDialog mAlertDialog;


    @BindView(R.id.input_username) EditText mUsernameText;
    @BindView(R.id.input_password) EditText mPasswordText;
    @BindView(R.id.btn_login) AppCompatButton mLoginButton;
    @BindView(R.id.btn_login_fb) LoginButton mFbLoginButton;
    @BindView(R.id.link_signup) TextView mSignupLink;

    @OnClick(R.id.btn_login)
    public void loginCallback(View v) {
        String lUsername = mUsernameText.getText().toString();
        String lPassword = mPasswordText.getText().toString();
        mPresenter.signIn(lUsername, lPassword);
    }

    @OnClick(R.id.link_signup)
    public void signupCallback(View v){
        // Start the Signup activity
        Intent intent = new Intent(getApplicationContext(), SeizureMonitoringActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);

        mPresenter = new SignInPresenter(this,
                                        getApplicationContext(),
                                        this,
                                        ((AuraApplication) getApplication()).getAuthentificationHelper(),
                                        ((AuraApplication) getApplication()).getUserSessionService());

        mFbCallbackManager = CallbackManager.Factory.create();
        mFbLoginButton.setReadPermissions(Arrays.asList("public_profile"));
        mFbLoginButton.registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                android.util.Log.d(TAG, "FB Login Success");
                mPresenter.signInSucceed();
            }

            @Override
            public void onCancel() {
                android.util.Log.d(TAG, "FB Login Cancel");
                mPresenter.signInFails();
            }

            @Override
            public void onError(FacebookException error) {
                android.util.Log.d(TAG, "FB Login Error");
                mPresenter.signInFails();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
         if(requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
        else if(requestCode == FIRST_TIME_SIGN_IN){
             if(resultCode == RESULT_OK) {
                 mPresenter.continueWithFirstSignIn();
             }
         }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    public void displayValidationError(String iErrorMessage) {
        mUsernameText.setError(iErrorMessage);
    }

    @Override
    public void displayAuthentificationProgressDialog() {
        mProgressDialog = new ProgressDialog(SignInActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Authenticating...");
        mProgressDialog.show();
    }

    @Override
    public void closeAuthentificationProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void enableLoginButton() {
        mLoginButton.setEnabled(true);
    }

    @Override
    public void disableLoginButton() {
        mLoginButton.setEnabled(false);
    }

    @Override
    public void displayFailLoginMessage() {
        mAlertDialog = new AlertDialog.Builder(SignInActivity.this)
                .setMessage("Login failed")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        mAlertDialog.show();
    }

    @Override
    public void setPresenter(SignInContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }
}
