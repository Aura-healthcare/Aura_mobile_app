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

package com.wearablesensor.aura.data_visualisation;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

/**
 * Created by lecoucl on 21/04/17.
 */
public class DataVisualisationContract {
    interface View extends BaseView<Presenter>{
        /**
         * @brief enable data visualisation on app
         */
        void enablePhysioSignalVisualisation();

        /**
         * @brief disable data visualisation on app
         */
        void disablePhysioSignalVisualisation();

        /**
         * brief refresh data visualisation when receiving a new data sample
         *
         * @param iPhysioSignal physiological data sample
         */
        void refreshPhysioSignalVisualisation(PhysioSignalModel iPhysioSignal);
    }

    interface Presenter extends BasePresenter{
        /**
         * @brief handle receiving a new data sample
         *
         * @param iPhysioSignal physiological data sample
         */
        void receiveNewPhysioSample(PhysioSignalModel iPhysioSignal);
    }
}
