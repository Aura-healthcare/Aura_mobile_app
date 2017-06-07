package com.wearablesensor.aura.seizure_report;

import android.app.Activity;
import android.content.Intent;

import com.wearablesensor.aura.SeizureReportActivity;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;

import java.util.Date;

public class SeizureReportPresenter implements SeizureReportContract.Presenter {

    private SeizureReportContract.View mView;
    private Activity mActivity;
    private LocalDataRepository mLocalDataRepository;

    public SeizureReportPresenter(SeizureReportContract.View iView, Activity iActivity, LocalDataRepository iLocalDataRepository){
        mActivity = iActivity;
        mView = iView;
        mView.setPresenter(this);
        mLocalDataRepository = iLocalDataRepository;
    }

    @Override
    public void start() {

    }

    @Override
    public void startReportSeizureDetails() {
        Intent lIntent = new Intent(mActivity, SeizureReportActivity.class);
        mActivity.startActivity(lIntent);
        mActivity.finish();
    }

    @Override
    public void reportSeizure(Date iDate, String iComment) {
        SeizureEventModel seizureEvent = new SeizureEventModel(DateIso8601Mapper.getString(new Date()), DateIso8601Mapper.getString(iDate), iComment);
        try {
            mLocalDataRepository.saveSeizure(seizureEvent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
