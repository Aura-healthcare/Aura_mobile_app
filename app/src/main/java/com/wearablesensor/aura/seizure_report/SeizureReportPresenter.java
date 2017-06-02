package com.wearablesensor.aura.seizure_report;

import android.app.Activity;
import android.content.Intent;

import com.wearablesensor.aura.SeizureReportActivity;

public class SeizureReportPresenter implements SeizureReportContract.Presenter {

    SeizureReportContract.View mView;
    Activity mActivity;

    public SeizureReportPresenter(SeizureReportContract.View iView, Activity iActivity){
        mActivity = iActivity;

        mView = iView;
        mView.setPresenter(this);
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
    public void reportSeizure() {

    }

}
