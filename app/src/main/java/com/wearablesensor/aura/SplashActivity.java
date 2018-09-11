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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.wearablesensor.aura.user_session.UserSessionService;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences lSharedPref = getSharedPreferences(UserSessionService.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String lUserUUID = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_UUID, null);

        if(lUserUUID != null){
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }

        this.finish();
    }
}
