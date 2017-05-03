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

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

import java.util.Date;

/**
 * Created by lecoucl on 21/04/17.
 */
public class DataVisualisationContract {
    interface View extends BaseView<Presenter>{
        void initRRSamplesVisualisation(Date iWindowStart, Date iWindowEnd);
        void enableRRSamplesVisualisation();
        void disableRRSamplesVisualisation();

        void refreshRRSamplesVisualisation(RRIntervalModel iSampleRR);
    }

    interface Presenter extends BasePresenter{
        void receiveNewHRVSample(RRIntervalModel iSampleRR);
    }
}
