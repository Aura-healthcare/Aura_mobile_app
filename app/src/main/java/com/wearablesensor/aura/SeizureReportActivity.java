package com.wearablesensor.aura;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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

        Date lSeizureDate = new Date(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
        String lComments = mSeizureReportComments.getText().toString();
        mSeizureReportPresenter.reportSeizure(lSeizureDate, lComments);
    }

    @BindView(R.id.seizure_report_cancel_button) AppCompatButton mCancelButton;   /** cancel button  */
    @OnClick(R.id.seizure_report_cancel_button)
    public void cancelCallback(View v) {
        mSeizureReportPresenter.cancelReportSeizureDetails();
    }

    @BindView(R.id.seizure_report_date_picker) DatePicker mDatePicker;
    @BindView(R.id.seizure_report_time_picker) TimePicker mTimePicker;

    @BindView(R.id.seizure_report_comments) EditText mSeizureReportComments;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_seizure_report);

        ButterKnife.bind(this);

        mSeizureReportPresenter = new SeizureReportPresenter(this, this, ((AuraApplication) getApplication()).getLocalDataRepository(), ((AuraApplication) getApplication()).getUserSessionService());

    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mSeizureReportPresenter = iPresenter;
    }
}
