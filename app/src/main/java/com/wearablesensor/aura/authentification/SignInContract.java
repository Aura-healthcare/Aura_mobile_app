package com.wearablesensor.aura.authentification;

import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

/**
 * Created by lecoucl on 21/04/17.
 */
public class SignInContract {
    public interface View extends BaseView<Presenter> {
        void displayValidationError(String iErrorMessage);

        void displayAuthentificationProgressDialog();
        void closeAuthentificationProgressDialog();

        void enableLoginButton();
        void disableLoginButton();

        void displayFailLoginMessage();
    }

    public interface Presenter extends BasePresenter{
        void signIn(String iUsername, String iPassword);

        void signInSucceed();
        void signInFails();
        boolean validate(String iUsername, String iPassword);

        void firstSignIn();
        void continueWithFirstSignIn();
    }
}
