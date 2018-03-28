package com.wearablesensor.aura.authentification;

/**
 * Created by octo_tbr on 27/02/18.
 */

public interface AuthenticationManager {

    void performAsyncAuthentication(String user);

    void continueWithFirstSignIn() throws Exception;

    String getJWTToken();
}
