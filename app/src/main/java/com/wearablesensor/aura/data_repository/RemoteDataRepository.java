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

package com.wearablesensor.aura.data_repository;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserPreferencesModel;

import java.util.ArrayList;

/**
 * Created by lecoucl on 29/03/17.
 */

public interface RemoteDataRepository {
    void connect(String lAuthToken) throws Exception;

    void saveRRSample(final ArrayList<RRIntervalModel> iRrSamples) throws Exception;

    UserModel queryUser(String iAmazonId) throws Exception;
    void saveUser(final UserModel iUserMode) throws Exception;

    UserPreferencesModel queryUserPreferences(String iUserId) throws Exception;
    void saveUserPreferences(final UserPreferencesModel iUserPreferencesModel) throws Exception;
}
