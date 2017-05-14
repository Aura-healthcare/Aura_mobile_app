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
 *
 * LocalDataRepository is an interface that describes the acces to local storage
 * The local storage implementation classes should be derivated from this interface
 *
 */

package com.wearablesensor.aura.data_repository;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import java.util.ArrayList;
import java.util.Date;


public interface LocalDataRepository {
    /**
     * @brief query a list of R-R interval samples in a time range [iStartDate, iEndDate]
     *
     * @param iStartDate collected data sample timestamps are newer than iStartData
     * @param iEndDate collected data samples timestamp is older than iEndData
     *
     * @return a list of R-R interval samples
     *
     * @throws Exception
     */
    ArrayList<RRIntervalModel> queryRRSample(Date iStartDate, Date iEndDate) throws Exception;

    /**
     * @brief save a single R-R interval in the local storage
     *
     * @param iSampleRR R-R interval to be stored
     *
     * @throws Exception
     */
    void saveRRSample(final RRIntervalModel iSampleRR) throws Exception;

    /**
     * @brief clear entirely the local data storage
     */
    void clear();
}
