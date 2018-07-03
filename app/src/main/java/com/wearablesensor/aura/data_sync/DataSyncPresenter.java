
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

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.wearablesensor.aura.data_repository.FileStorage;
import com.wearablesensor.aura.data_repository.LocalDataFileRepository;
import com.wearablesensor.aura.data_sync.notifications.DataSyncNotification;
import com.wearablesensor.aura.data_sync.notifications.DataSyncServiceObserver;
import com.wearablesensor.aura.data_sync.notifications.DataSyncStatus;
import com.wearablesensor.aura.data_sync.notifications.DataSyncUpdateStateNotification;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

public class DataSyncPresenter extends DataSyncServiceObserver implements DataSyncContract.Presenter {
    private final String TAG = this.getClass().getSimpleName();

    private DataSyncService mDataSyncService;
    private DataSyncContract.View mView;

    private Context mApplicationContext;

    /**
     * @brief constructor
     *
     * @param iDataSyncService data sync service
     * @param iView view to be updated as it is done in MVP architecture
     */
    public DataSyncPresenter(Context iApplicationContext, DataSyncService iDataSyncService, DataSyncContract.View iView) {
        mDataSyncService = iDataSyncService;

        mView = iView;

        mView.setPresenter(this);

        mApplicationContext = iApplicationContext;

        EventBus.getDefault().register(this);
    }

    /**
     * @brief presenter initialisation method executed at the creation of the view fragment
     */
    @Override
    public void start() {
        Integer lDataPacketNumber = getDataPacketsNumber();
        mView.refreshDataPackerNumber(lDataPacketNumber);

        if(mDataSyncService == null){
            mView.endPushDataOnCloud();
            return;
        }

        if(mDataSyncService.isDataSyncInProgress()){
            mView.startPushDataOnCloud();
        }
        else{
            mView.endPushDataOnCloud();
        }
    }

    /**
     * @brief method executed by observer class when receiving a data sync notification event
     *
     * @param iDataSyncNotification notification to be processed by observer class
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataSyncServiceEvent(DataSyncNotification iDataSyncNotification){
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
            mView.refreshDataPackerNumber(getDataPacketsNumber());
        }
    }
    @Override
    public void finalize(){
        EventBus.getDefault().unregister(this);
    }

    private Integer getDataPacketsNumber() {
        String path = mApplicationContext.getFilesDir().getPath();
        File directory = new File(path);
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.toString().contains(FileStorage.CACHE_FILENAME)) {
                    return true;
                }

                return false;
            }
        });

        return files.length;
    }

    public void setDataSyncService(DataSyncService iDataSyncService){
        mDataSyncService = iDataSyncService;
    }
}