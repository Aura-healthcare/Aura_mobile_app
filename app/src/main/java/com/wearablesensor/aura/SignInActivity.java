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
 * SignInActivity is the view component that allows user to sign-in
 * It implements SignInContract.View interface
 */
package com.wearablesensor.aura;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;

import com.wearablesensor.aura.authentification.SignInContract;
import com.wearablesensor.aura.authentification.SignInPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends AppCompatActivity implements SignInContract.View{
    private final String TAG = this.getClass().getSimpleName();
    private static final int FIRST_TIME_SIGN_IN = 1;

    private SignInContract.Presenter mPresenter; /** presenter component as it is done in MVP architecture */

    private ProgressDialog mProgressDialog; /** user sign-in pending dialog */
    protected AlertDialog mAlertDialog; /** user sign-in fails popup */


    @BindView(R.id.input_username) EditText mUsernameText; /** username input field */
    @BindView(R.id.input_password) EditText mPasswordText; /** password input field */
    @BindView(R.id.btn_login) AppCompatButton mLoginButton; /** sign-in button */

    @OnClick(R.id.btn_login)
    public void loginCallback(View v) {
        String lUsername = mUsernameText.getText().toString();
        String lPassword = mPasswordText.getText().toString();
        mPresenter.signIn(lUsername, lPassword);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);

        mPresenter = new SignInPresenter(this,
                                        getApplicationContext(),
                                        this,
                                        ((AuraApplication) getApplication()).getAuthentificationHelper());
    }

    @Override
    protected void onResume(){
        super.onResume();

        // display error message from session sign-in fail
        String lSessionFailSignInExtraMessage = getIntent().getStringExtra("sessionFailSignInExtraMessage");

        if(lSessionFailSignInExtraMessage != null){
            displayFailLoginMessage(lSessionFailSignInExtraMessage);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == FIRST_TIME_SIGN_IN){
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

    /**
     * @brief display tooltip when user sign-in pre-validation fails
     *
     * @param iErrorMessage tooltip message
     */
    @Override
    public void displayValidationError(String iErrorMessage) {
        mUsernameText.setError(iErrorMessage);
    }

    /**
     * @brief start authentification pending message display
     */
    @Override
    public void displayAuthentificationProgressDialog() {
        mProgressDialog = new ProgressDialog(SignInActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.signin_pending_message));
        mProgressDialog.show();
    }

    /**
     * @brief end authenfication pending message display
     */
    @Override
    public void closeAuthentificationProgressDialog() {
        mProgressDialog.dismiss();
    }

    /**
     * @brief enable login button to allow to sign-in
     */
    @Override
    public void enableLoginButton() {
        mLoginButton.setEnabled(true);
    }

    /**
     * @brief disable sign-in button during authentification process to avoid multiple sign-in attempts
     */
    @Override
    public void disableLoginButton() {
        mLoginButton.setEnabled(false);
    }

    /**
     * @brief notify user that sign-in fail and give clues of what could be wrong
     *
     * @param iFailExtraMessage message displayed to help user to solve sign-in issue
     */
    @Override
    public void displayFailLoginMessage(String iFailExtraMessage) {
        mAlertDialog = new AlertDialog.Builder(SignInActivity.this)
                .setMessage(getResources().getString(R.string.signin_failed) + "\n\n"+ iFailExtraMessage)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        mAlertDialog.show();
    }

    /**
     * @brief setter
     *
     * @param iPresenter attached presenter 
     */
    @Override
    public void setPresenter(SignInContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }
}
