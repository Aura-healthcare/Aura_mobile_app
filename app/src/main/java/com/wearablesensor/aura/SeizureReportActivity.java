package com.wearablesensor.aura;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.wearablesensor.aura.seizure_report.SeizureReportContract;
import com.wearablesensor.aura.seizure_report.SeizureReportPresenter;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by alyson on 25/04/17.
 */

public class SeizureReportActivity extends AppCompatActivity implements SeizureReportContract.View {
    private final static String TAG = SeizureReportActivity.class.getSimpleName();

    private SeizureReportContract.Presenter mSeizureReportPresenter;

    @BindView(R.id.seizure_report_confirm_button) AppCompatButton mConfirmButton; /** confirm button */
    @OnClick(R.id.seizure_report_confirm_button)
    public void confirmCallback(View v) {

        Date seizureDate = new Date(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
        String comment = "blabla";
        mSeizureReportPresenter.reportSeizure(seizureDate, comment);
    }

    @BindView(R.id.seizure_report_cancel_button) AppCompatButton mCancelButton;   /** cancel button  */
    @OnClick(R.id.seizure_report_cancel_button)
    public void cancelCallback(View v) {

    }

    @BindView(R.id.seizure_report_date_picker) DatePicker mDatePicker;
    @BindView(R.id.seizure_report_time_picker) TimePicker mTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seizure_report);

        mSeizureReportPresenter = new SeizureReportPresenter(this, this, ((AuraApplication) getApplication()).getLocalDataRepository());

        ButterKnife.bind(this);
    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mSeizureReportPresenter = iPresenter;
    }
}
