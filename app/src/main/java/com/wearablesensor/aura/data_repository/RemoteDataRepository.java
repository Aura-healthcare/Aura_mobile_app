/**
 * @file
 *
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
 *
 * RemoteDataRepository is an interface that describes the acces to remote storage
 * The remote storage implementation classes should be derivated from this interface
 *
 */

package com.wearablesensor.aura.data_repository;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserPreferencesModel;

import java.util.ArrayList;

public interface RemoteDataRepository {

    /**
     * @brief initialize connection between remote database and Aura application
     *
     * @param lAuthToken authentification token generated by identity provider
     * @throws Exception
     */
    void connect(String lAuthToken) throws Exception;

    /**
     * @brief save a list of R-R interval samples
     *
     * @param iRrSamples list of R-R interval samples to be saved
     *
     * @throws Exception
     */
    void saveRRSample(final ArrayList<RRIntervalModel> iRrSamples) throws Exception;


    /**
     * @brief query a user following authentification
     *
     * @param iAmazonId amazon cognito username
     *
     * @throws Exception
     */
    UserModel queryUser(String iAmazonId) throws Exception;

    /**
     * @brief save a newly created user with information related to Authentification provider
     *
     * @param iUserModel user information related to Authentification
     *
     * @throws Exception
     */
    void saveUser(final UserModel iUserModel) throws Exception;

    /**
     * @brief query user preferences for a single user
     *
     * @param iUserId selected user UUID
     *
     * @return user preferences
     *
     * @throws Exception
     */
    UserPreferencesModel queryUserPreferences(String iUserId) throws Exception;

    /**
     * @brief save user preferences for a single user
     *
     * @param iUserPreferencesModel user preferences to be saved 
     *
     * @throws Exception
     */
    void saveUserPreferences(final UserPreferencesModel iUserPreferencesModel) throws Exception;
}
