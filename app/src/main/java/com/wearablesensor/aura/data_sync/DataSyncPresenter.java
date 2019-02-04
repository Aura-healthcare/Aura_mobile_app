
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
import android.os.Environment;
import android.util.Log;

import com.wearablesensor.aura.data_repository.FileStorage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileFilter;

import static com.wearablesensor.aura.data_repository.FileStorage.EXPORT_AURA_DATA_DIR;

public class DataSyncPresenter implements DataSyncContract.Presenter {
    private final String TAG = this.getClass().getSimpleName();

    private DataSyncContract.View mView;

    private Context mApplicationContext;

    /**
     * @brief constructor
     *
     * @param iView view to be updated as it is done in MVP architecture
     */
    public DataSyncPresenter(Context iApplicationContext, DataSyncContract.View iView) {
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

        mView.endPushDataOnCloud();
        return;
    }

    /**
      * @brief method executed by observer class when receiving a data sync notification event
      *
      * @param iDataSyncNotification notification to be processed by observer class
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataSyncServiceEvent(DataSyncUpdateStateNotification iDataSyncNotification){
        Log.d(TAG, "DataSyncNotification ");
        mView.refreshDataPackerNumber(getDataPacketsNumber());
    }

    @Override
    public void finalize(){
        EventBus.getDefault().unregister(this);
    }

    private Integer getDataPacketsNumber() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + EXPORT_AURA_DATA_DIR);
        Log.d(TAG, "Dir" + directory.toString());
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.toString().contains(FileStorage.CACHE_FILENAME)) {
                    return true;
                }

                return false;
            }
        });

        if (files == null){
            return 0;
        }

        Log.d(TAG, "Nb Packet " + files.length);

        return files.length;
    }
}