/**
 * @file SeizureStatusPresenter
 * @author clecoued <clement.lecouedic@aura.healthcare>
 * @author rogeral <raalysonroger@gmail.com>
 *
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
 *
 */
package com.wearablesensor.aura.seizure_report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_sync.DataSyncFragment;
import com.wearablesensor.aura.data_visualisation.PhysioSignalVisualisationFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsFragment;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.util.Date;

public class SeizureStatusPresenter implements SeizureStatusContract.Presenter {

    private SeizureStatusContract.View mView;
    private Fragment mReportFragment;
    private FragmentActivity mActivity;

    public SeizureStatusPresenter(SeizureStatusContract.View iView, Fragment iReportFragment, FragmentActivity iActivity){
        mActivity = iActivity;
        mView = iView;
        mView.setPresenter(this);
        mReportFragment = iReportFragment;
    }

    @Override
    public void start() {

    }

    @Override
    public void startReportSeizureDetails() {
        FragmentTransaction lTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        lTransaction.remove(mActivity.getSupportFragmentManager().findFragmentByTag(DevicePairingDetailsFragment.class.getSimpleName()));
        lTransaction.remove(mActivity.getSupportFragmentManager().findFragmentByTag(DataSyncFragment.class.getSimpleName()));
        lTransaction.remove(mActivity.getSupportFragmentManager().findFragmentByTag(PhysioSignalVisualisationFragment.class.getSimpleName()));
        lTransaction.remove(mActivity.getSupportFragmentManager().findFragmentByTag(SeizureStatusFragment.class.getSimpleName()));

        lTransaction.add(R.id.content_frame, mReportFragment, SeizureReportFragment.class.getSimpleName());
        lTransaction.addToBackStack(null);

        // Commit the transaction
        lTransaction.commit();
    }

}
