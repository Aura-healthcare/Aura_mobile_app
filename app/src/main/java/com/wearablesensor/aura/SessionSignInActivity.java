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
 * SessionSignInActivity is an activity class that handles user session start
 * It does binding between authentification component and user session service
 */

package com.wearablesensor.aura;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wearablesensor.aura.authentification.AmazonCognitoAuthentificationHelper;
import com.wearablesensor.aura.user_session.UserSessionService;

public class SessionSignInActivity extends AppCompatActivity{
    private final String TAG = this.getClass().getSimpleName();

    private AmazonCognitoAuthentificationHelper mAuthentificationHelper; /** Amazon authentification API */
    private UserSessionService mUserSessionService; /** User session service */

    private ProgressDialog mProgressDialog; /** session initialisation progress bar */

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

    /**
     * @brief start session sign-in
     *
     * @param iAmazonId Amazon unique username
     */
    private void sessionSignIn(String iAmazonId){
        displayAuthentificationProgressDialog();
        mUserSessionService.initSession(iAmazonId, this);
    }

    /**
     * @brief display session sign-in pending message
     */
    public void displayAuthentificationProgressDialog() {
        mProgressDialog = new ProgressDialog(SessionSignInActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getResources().getString(R.string.session_signin_pending_message));
        mProgressDialog.show();
    }

    /**
     * @brief end session sign-in message
     */
    public void closeAuthentificationProgressDialog() {
        mProgressDialog.dismiss();
    }
}
