package com.wearablesensor.aura.seizure_report;

import android.app.Activity;
import android.content.Intent;

import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.SeizureReportActivity;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import java.util.Date;

public class SeizureReportPresenter implements SeizureReportContract.Presenter {

    private SeizureReportContract.View mView;
    private Activity mActivity;
    private LocalDataRepository mLocalDataRepository;
    private UserSessionService mUserSessionService;

    public SeizureReportPresenter(SeizureReportContract.View iView, Activity iActivity, LocalDataRepository iLocalDataRepository, UserSessionService iUserSessionService){
        mActivity = iActivity;
        mView = iView;
        mView.setPresenter(this);
        mLocalDataRepository = iLocalDataRepository;
        mUserSessionService = iUserSessionService;
    }

    @Override
    public void start() {

    }

    @Override
    public void startReportSeizureDetails() {
        Intent lIntent = new Intent(mActivity, SeizureReportActivity.class);
        mActivity.startActivity(lIntent);
    }

    public void cancelReportSeizureDetails(){
        mActivity.finish();
    }

    @Override
    public void reportSeizure(Date iDate, String iComments) {
        SeizureEventModel lNewSeizureEvent = new SeizureEventModel(mUserSessionService.getUser().getUuid(), DateIso8601Mapper.getString(new Date()), DateIso8601Mapper.getString(iDate), iComments);
        try {
            mLocalDataRepository.saveSeizure(lNewSeizureEvent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        mActivity.finish();
    }

}
