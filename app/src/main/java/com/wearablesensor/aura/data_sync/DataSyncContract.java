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
 * DataSyncContract is the interface that defines the "data sync" functionality
 * This functionality allows the user to manually push local data to Cloud
 *
 */
package com.wearablesensor.aura.data_sync;

import android.content.Context;

import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

import java.util.Date;

/**
 * Created by lecoucl on 14/04/17.
 */
public class DataSyncContract {
    interface View extends BaseView<Presenter>{

        /**
         * @brief start data push display state
         */
        void startPushDataOnCloud();

        /**
         * @brief end data push display state
         */
        void endPushDataOnCloud();

        /**
         * @brief refresh last syncing date display
         *
         * @param iLastSync last time data has been push on Cloud
         */
        void refreshLastSync(Date iLastSync);

        /**
         * @brief display an error popup with corresponding message to user
         *
         * @param iContext application context
         * @param iFailMessage message to be displayed
         */
        void displayFailMessageOnPushData(Context iContext, String iFailMessage);

        /**
         * @brief attach presenter to view as it is done in MVP architecture
         *
         * @param iPresenter presenter to be attached
         */
        void setPresenter(Presenter iPresenter);

        /**
         * @brief display low wifi signal state
         */
        void displayLowSignalState();

        /**
         * @brief display no wifi signal state
         */
        void displayNoSignalState();
    }

    interface Presenter extends BasePresenter{


    }
}
