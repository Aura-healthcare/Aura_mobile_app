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
