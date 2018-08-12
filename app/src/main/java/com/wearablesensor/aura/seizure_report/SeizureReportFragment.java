/**
 * @file SeizureReportFragment
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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.SeizureMonitoringActivity;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lib.kingja.switchbutton.SwitchMultiButton;

/**
 * Created by lecoucl on 09/08/17.
 */
public class SeizureReportFragment extends Fragment implements SeizureReportContract.View{

    private final String TAG = this.getClass().getSimpleName();
    private SeizureReportContract.Presenter mPresenter;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.seizure_report_confirm_button)
    AppCompatButton mConfirmButton; /** confirm button */
    @OnClick(R.id.seizure_report_confirm_button)
    public void confirmCallback(View v) {

        mPresenter.reportSeizure();
    }

    @BindView(R.id.seizure_report_cancel_button) AppCompatButton mCancelButton;   /** cancel button  */
    @OnClick(R.id.seizure_report_cancel_button)
    public void cancelCallback(View v) {
        mPresenter.cancelReportSeizureDetails();
    }

    @BindView(R.id.single_time_date_picker) SingleDateAndTimePicker mSingleDateAndTimePicker;
    @BindView(R.id.switch_button_seizure_impact) SwitchMultiButton mSwitchMultiButton;
    @BindView(R.id.seizure_report_additionnal_questions) Button mAdditionnalQuestions;
    @OnClick(R.id.seizure_report_additionnal_questions)
    public void goToAdditionnalQuestions(){
        //TODO: replace by proper event driven Navigation component
        ((SeizureMonitoringActivity) getActivity()).goToAdditionnalQuestions(0);
    }

    public static SeizureReportFragment newInstance() {
        SeizureReportFragment fragment = new SeizureReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seizure_report, container, false);
        ButterKnife.bind(this, view);

        mSwitchMultiButton.setText(getResources().getString(R.string.seizure_intensity_small), getResources().getString(R.string.seizure_intensity_medium), getResources().getString(R.string.seizure_intensity_big));
        mSwitchMultiButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                if(position == 0){
                    mPresenter.setCurrentIntensity("Small");
                }
                else if(position == 1){
                    mPresenter.setCurrentIntensity("Medium");
                }
                else if(position == 2){
                    mPresenter.setCurrentIntensity("Big");
                }
            }
        });
        mSwitchMultiButton.setSelectedTab(0);

        mSingleDateAndTimePicker.setListener(new SingleDateAndTimePicker.Listener() {
            @Override
            public void onDateChanged(String displayed, Date date) {
                mPresenter.setCurrentDate(date);
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSeizureReportFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onSeizureReportFragmentInteraction(Uri uri);
    }
}

