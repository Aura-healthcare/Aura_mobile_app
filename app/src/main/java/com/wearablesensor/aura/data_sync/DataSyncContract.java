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

        void startPushDataOnCloud();

        void endPushDataOnCloud();

        void refreshProgressPushDataOnCloud(Integer iProgress);

        void refreshLastSync(Date iLastSync);

        void displayFailMessageOnPushData(Context iContext, String iFailMessage);

        void setPresenter(Presenter iPresenter);
    }

    interface Presenter extends BasePresenter{

        void pushData();

    }
}
