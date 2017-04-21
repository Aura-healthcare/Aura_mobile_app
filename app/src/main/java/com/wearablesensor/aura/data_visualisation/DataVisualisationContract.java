package com.wearablesensor.aura.data_visualisation;

import com.wearablesensor.aura.data_repository.SampleRRInterval;
import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

import java.util.Date;

/**
 * Created by lecoucl on 21/04/17.
 */
public class DataVisualisationContract {
    interface View extends BaseView<Presenter>{
        void initRRSamplesVisualisation(Date iWindowStart, Date iWindowEnd);
        void enableRRSamplesVisualisation();
        void disableRRSamplesVisualisation();

        void refreshRRSamplesVisualisation(SampleRRInterval iSampleRR);
    }

    interface Presenter extends BasePresenter{
        void receiveNewHRVSample(SampleRRInterval iSampleRR);
    }
}
