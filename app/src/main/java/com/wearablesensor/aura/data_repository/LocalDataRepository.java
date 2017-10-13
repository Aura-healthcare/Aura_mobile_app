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

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;

import java.util.ArrayList;
import java.util.Date;


public interface LocalDataRepository {
    /**
     * @brief query a list of physiological data samples from a cache file
     *
     * @param iFilename cache file storing the physio signal samples
     *
     * @return a list of physiological data samples
     *
     * @throws Exception
     */

    public ArrayList<PhysioSignalModel> queryPhysioSignalSamples(String iFilename) throws Exception;

    /**
     * @brief save a batch of physiological signal sample in the local storage
     *
     * @param iPhysioSignalSamples physiological data list to be stored
     *
     * @throws Exception
     */
    public void savePhysioSignalSamples(final ArrayList<PhysioSignalModel> iPhysioSignalSamples) throws Exception;

    /**
     * @brief remove a batch of physiological signal samples from the local storage
     *
     * @param iFilename physiological data list to be removed
     *
     * @throws Exception
     */
    public void removePhysioSignalSamples(String iFilename) throws Exception;

    /**
     * @brief cache a physiological data sample in the heap and periodically clear cache and save data to local
     * storage
     *
     * @param iPhysioSignal physiological data sample to be cached
     */
    void cachePhysioSignalSample(PhysioSignalModel iPhysioSignal) throws Exception;


    /**
     * @brief query a list of  sensitive events
     *
     * @param iFilename cache file storing the physio signal samples
     *
     * @return a list of physiological data samples
     *
     * @throws Exception
     */

    public ArrayList<SeizureEventModel> querySeizures(String iFilename) throws Exception;

    /**
     * @brief save a seizure event
     *
     * @param iSeizureEventModel seizure event
     *
     * @throws Exception
     */
    void saveSeizure(final SeizureEventModel iSeizureEventModel) throws Exception;

    /**
     * @brief clear cache and store data from heap to local data storage
     *
     */
    void clearCache() throws Exception;
}
