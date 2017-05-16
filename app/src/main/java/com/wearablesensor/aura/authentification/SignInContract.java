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
 * SignInContract is the interface that defines the user "sign-in" functionnality
 * This functionnality handles:
 * - user credentials pre-validation
 * - user authentification with AmazonCognito user pool
 * - user first time sign-in validation with AmazonCognito user pool
 * - user session initialization
 * - user sign-in fail notification
 */


package com.wearablesensor.aura.authentification;

import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

public class SignInContract {
    public interface View extends BaseView<Presenter> {

        /**
         * @brief display tooltip when user sign-in pre-validation fails
         *
         * @param iErrorMessage tooltip message
         */
        void displayValidationError(String iErrorMessage);

        /**
         * @brief start authentification pending message display
         */
        void displayAuthentificationProgressDialog();

        /**
         * @brief end authenfication pending message display
         */
        void closeAuthentificationProgressDialog();

        /**
         * @brief enable login button to allow to sign-in
         */
        void enableLoginButton();

        /**
         * @brief disable sign-in button during authentification process to avoid multiple sign-in attempts
         */
        void disableLoginButton();

        /**
         * @brief notify user that sign-in fail and give clues of what could be wrong
         *
         * @param iFailExtraMessage message displayed to help user to solve sign-in issue
         */
        void displayFailLoginMessage(String iFailExtraMessage);
    }

    public interface Presenter extends BasePresenter{
        /**
         *
         * @brief user attempts to sign-in
         *
         * @param iUsername username credential provided to sign-in
         * @param iPassword password credential provided to sign-in
         */
        void signIn(String iUsername, String iPassword);

        /**
         * @brief authentification succeed, start user session
         */
        void signInSucceed();

        /**
         * @brief authentification fails, go back to sign-in page and provides extra message to help user
         *
         * @param iFailExtraMessage message displayed to help user to solve sign-in issue
         */
        void signInFails(String iFailExtraMessage);

        /**
         * @brief user credentials pre-validation before trying to authenticate
         *
         * @param iUsername username credential provided to sign-in
         * @param iPassword password credential provided to sign-in
         *
         * @return true if pre-validation succeed, otherwise display tooltip to user
         */
        boolean validate(String iUsername, String iPassword);

        /**
         * @brief attempt to validate user account on first sign-in
         */
        void firstSignIn();

        /**
         * @brief user account is validated on first sign-in
         */
        void continueWithFirstSignIn();
    }
}
