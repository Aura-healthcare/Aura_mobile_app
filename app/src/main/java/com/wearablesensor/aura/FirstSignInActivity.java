package com.wearablesensor.aura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;

import com.facebook.FacebookSdk;



import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstSignInActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.first_signin_new_password_input) EditText mNewPasswordInput;
    @BindView(R.id.btn_continue_signin) AppCompatButton mContinueSignInButton;

    @OnClick(R.id.btn_continue_signin)
    public void signInCallback(View v) {
        String lNewUserPassword = mNewPasswordInput.getText().toString();
        if (lNewUserPassword != null) {
            ((AuraApplication) getApplication()).getAuthentificationHelper().setPasswordForFirstTimeLogin(lNewUserPassword);
            exit(true);
        }
    }

    private void exit(Boolean continueWithSignIn) {
        Intent intent = new Intent();
        intent.putExtra("continueSignIn", continueWithSignIn);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_first_sign_in);

        ButterKnife.bind(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

}

