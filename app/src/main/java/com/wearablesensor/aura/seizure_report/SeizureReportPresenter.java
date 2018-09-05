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

import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;
import com.wearablesensor.aura.navigation.NavigationWithIndexNotification;
import com.wearablesensor.aura.user_session.UserSessionService;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

public class SeizureReportPresenter implements SeizureReportContract.Presenter {

    private SeizureReportContract.View mView;
    private LocalDataRepository mLocalDataRepository;
    private UserSessionService mUserSessionService;

    private Date mCurrentDate;
    private String mCurrentIntensity;

    private String mQuestionTag;
    private String mResultTag;

    public SeizureReportPresenter(SeizureReportContract.View iView, LocalDataRepository iLocalDataRepository, UserSessionService iUserSessionService){
        mView = iView;
        mView.setPresenter(this);
        mLocalDataRepository = iLocalDataRepository;
        mUserSessionService = iUserSessionService;

        mCurrentDate = new Date();
        mCurrentIntensity = "";

        mQuestionTag = "";
        mResultTag = "";
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

    public void setQuestionResult(String iQuestionTag, String iResultTag){
        mQuestionTag = iQuestionTag;
        mResultTag = iResultTag;
    }

    public void endReportSeizure(){
        EventBus.getDefault().post(new NavigationNotification(NavigationConstants.NAVIGATION_SEIZURE_MONITORING));
    }

    @Override
    public void cancelReportSeizure(){
        mLocalDataRepository.clearSeizure();
        endReportSeizure();
    }

    @Override
    public void reportSeizure() {
        mLocalDataRepository.cacheSeizureBasicInformation(mUserSessionService.getUser().getUuid(), DateIso8601Mapper.getString(new Date()), DateIso8601Mapper.getString(mCurrentDate), mCurrentIntensity);
        try {
            mLocalDataRepository.saveSeizure();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        mLocalDataRepository.clearSeizure();
        endReportSeizure();
    }

    @Override
    public void giveAdditionalInformationOnSeizure() {
        mLocalDataRepository.cacheSeizureBasicInformation(mUserSessionService.getUser().getUuid(), DateIso8601Mapper.getString(new Date()), DateIso8601Mapper.getString(mCurrentDate), mCurrentIntensity);
        EventBus.getDefault().post(new NavigationWithIndexNotification(NavigationConstants.NAVIGATION_SEIZURE_NEXT_QUESTION, 0));
    }

    @Override
    public void nextAdditionalInformationSeizureOnSeizure(int iIndex) {
        mLocalDataRepository.cacheSeizureAdditionalInformation(mQuestionTag, mResultTag);
        EventBus.getDefault().post(new NavigationWithIndexNotification(NavigationConstants.NAVIGATION_SEIZURE_NEXT_QUESTION, iIndex));
    }
}
