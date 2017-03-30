package com.wearablesensor.aura;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.wearablesensor.aura.data.DataRepository;
import com.wearablesensor.aura.data.DataRepositoryComponent;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignInActivity extends AppCompatActivity {
    private final static String TAG = SignInActivity.class.getSimpleName();
    private static final int REQUEST_SIGNUP = 0;

    private CallbackManager mFbCallbackManager;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.btn_login_fb) LoginButton _fbLoginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    @OnClick (R.id.btn_login)
    public void loginCallback(View v) {
        login();
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

        //TODO: to remove,only here for testing DaraRepository component loading
        AuraApplication app = (AuraApplication) getApplication();
        DataRepositoryComponent dataComponent = app.getDataRepositoryComponent();
        DataRepository dataRep = dataComponent.getDataRepository();

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);

        mFbCallbackManager = CallbackManager.Factory.create();
        //_fbLoginButton.setReadPermissions(Arrays.asList("public_profile"));
        /*_fbLoginButton.registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                android.util.Log.d(TAG, "FB Login Success");
                onLoginSuccess(getApplicationContext());
            }

            @Override
            public void onCancel() {
                android.util.Log.d(TAG, "FB Login Cancel");
                onLoginFailed();
            }

            @Override
            public void onError(FacebookException error) {
                android.util.Log.d(TAG, "FB Login Error");
                onLoginFailed();
            }
        });*/
    }


    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        final Context context = getApplicationContext();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess(context);
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
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
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(Context context) {
        _loginButton.setEnabled(true);

        Intent intent = new Intent(context, SeizureMonitoringActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        this.finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
