/**
 * @file SeizureReportPresenter
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

public class SeizureReportPresenter implements SeizureReportContract.Presenter {

    private SeizureReportContract.View mView;
    private FragmentActivity mActivity;
    private LocalDataRepository mLocalDataRepository;
    private UserSessionService mUserSessionService;

    private Date mCurrentDate;
    private String mCurrentIntensity;

    public SeizureReportPresenter(SeizureReportContract.View iView, FragmentActivity iActivity, LocalDataRepository iLocalDataRepository, UserSessionService iUserSessionService){
        mActivity = iActivity;
        mView = iView;
        mView.setPresenter(this);
        mLocalDataRepository = iLocalDataRepository;
        mUserSessionService = iUserSessionService;

        mCurrentDate = new Date();
        mCurrentIntensity = "";
    }

    @Override
    public void start() {

    }

    @Override
    public void setCurrentDate(Date iDate){
        mCurrentDate = iDate;
    }

    @Override
    public void setCurrentIntensity(String iIntensity){
        mCurrentIntensity = iIntensity;
    }

    public void endReportSeizureDetails(){


        FragmentTransaction lTransaction = mActivity.getSupportFragmentManager().beginTransaction();

        lTransaction.remove(mActivity.getSupportFragmentManager().findFragmentByTag(SeizureReportFragment.class.getSimpleName()));

        lTransaction.add(R.id.content_frame, mActivity.getSupportFragmentManager().findFragmentByTag(DevicePairingDetailsFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, mActivity.getSupportFragmentManager().findFragmentByTag(PhysioSignalVisualisationFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, mActivity.getSupportFragmentManager().findFragmentByTag(DataSyncFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, mActivity.getSupportFragmentManager().findFragmentByTag(SeizureStatusFragment.class.getSimpleName()));

        lTransaction.addToBackStack(null);

        // Commit the transaction
        lTransaction.commit();
    }

    @Override
    public void cancelReportSeizureDetails(){
        endReportSeizureDetails();
    }

    @Override
    public void reportSeizure() {
        SeizureEventModel lNewSeizureEvent = new SeizureEventModel(mUserSessionService.getUser().getUuid(), DateIso8601Mapper.getString(new Date()), DateIso8601Mapper.getString(mCurrentDate), mCurrentIntensity);
        try {
            mLocalDataRepository.saveSeizure(lNewSeizureEvent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        endReportSeizureDetails();
    }

}
