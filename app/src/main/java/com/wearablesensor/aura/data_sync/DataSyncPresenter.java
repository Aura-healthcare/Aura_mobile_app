
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
 * DataSyncPresenter is the presentation component that handles action related to data sync with Cloud
 * It implements the DataSyncContract.Presenter interface
 *
 */

package com.wearablesensor.aura.data_sync;

import android.util.Log;
import com.wearablesensor.aura.data_sync.notifications.DataSyncNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncServiceObserver;
import com.wearablesensor.aura.data_sync.notifications.DataSyncStatus;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;

import java.util.Date;

public class DataSyncPresenter extends DataSyncServiceObserver implements DataSyncContract.Presenter {
    private final String TAG = this.getClass().getSimpleName();

    private DataSyncService mDataSyncService;
    private DataSyncContract.View mView;

    /**
     * @brief constructor
     *
     * @param iDataSyncService data sync service
     * @param iView view to be updated as it is done in MVP architecture
     */
    public DataSyncPresenter(DataSyncService iDataSyncService, DataSyncContract.View iView) {
        mDataSyncService = iDataSyncService;
        mDataSyncService.addObserver(this);

        mView = iView;

        mView.setPresenter(this);
    }

    /**
     * @brief presenter initialisation method executed at the creation of the view fragment
     */
    @Override
    public void start() {
        Date lLastSync = mDataSyncService.getLastSync();
        mView.refreshLastSync(lLastSync);

    }

    /**
     * @brief method executed by observer class when receiving a data sync notification event
     *
     * @param iDataSyncNotification notification to be processed by observer class
     */
    @Override
    public void onDataSyncServiceNotification(DataSyncNotification iDataSyncNotification){
        Log.d(TAG, "DataSyncNotification " + iDataSyncNotification.getStatus());
        if(iDataSyncNotification.getStatus() == DataSyncStatus.START_SYNC){
            mView.startPushDataOnCloud();
        }
        else if(iDataSyncNotification.getStatus() == DataSyncStatus.END_SYNC){
            mView.endPushDataOnCloud();
        }
        else if(iDataSyncNotification.getStatus() == DataSyncStatus.SIGNAL_STATE_LOW){
            mView.displayLowSignalState();
        }
        else if(iDataSyncNotification.getStatus() == DataSyncStatus.SIGNAL_STATE_NONE){
            mView.displayNoSignalState();
        }
        else if(iDataSyncNotification.getStatus() == DataSyncStatus.UPDATE_SYNC_STATE){
            DataSyncUpdateStateNotification lDataSyncUpdateStateNotification = (DataSyncUpdateStateNotification) iDataSyncNotification;
            mView.refreshLastSync(lDataSyncUpdateStateNotification.getLastSync());
        }
    }

}