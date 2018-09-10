/**
 * @file YesNoTaskFragment
 * @author clecoued <clement.lecouedic@aura.healthcare>
 *
 *
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2018 Aura Healthcare
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

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.SeizureMonitoringActivity;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;
import com.wearablesensor.aura.navigation.NavigationWithIndexNotification;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import lib.kingja.switchbutton.SwitchMultiButton;

public class YesNoTaskFragment extends Fragment implements SeizureReportContract.View{

    private static final String QUESTION_KEY = "question";
    private static final String TASK_NAME = "task-name";
    private static final String TASK_INDEX = "task-index";
    private static final String YES_OPTION = "yes";
    private static final String NO_OPTION = "no";

    private int mTaskIndex;
    private String mTaskName;
    private String mQuestion;

    private SeizureReportContract.Presenter mPresenter;

    @BindView(R.id.yes_no_question)TextView mQuestionText;
    @BindView(R.id.next_button) Button mNextButton;
    @OnClick(R.id.next_button)
    public void goToNext(View v){
        mPresenter.nextAdditionalInformationSeizureOnSeizure(mTaskIndex);
    }
    @BindView(R.id.switch_button_yes_no) SwitchMultiButton mYesNoSwitchButton;

    public YesNoTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment YesNoTaskFragment
     */

    public static YesNoTaskFragment newInstance(String iQuestion, String iTaskName, int iIndex) {
        YesNoTaskFragment lFragment = new YesNoTaskFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_KEY, iQuestion);
        args.putString(TASK_NAME, iTaskName);
        args.putInt(TASK_INDEX, iIndex);

        lFragment.setArguments(args);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mQuestion = getArguments().getString(QUESTION_KEY);
            mTaskName = getArguments().getString(TASK_NAME);
            mTaskIndex = getArguments().getInt(TASK_INDEX);
        }

        View view = inflater.inflate(R.layout.fragment_yes_no_task, container, false);
        ButterKnife.bind(this, view);

        mYesNoSwitchButton.setText(getString(R.string.yes), getString(R.string.no));
        mPresenter.setQuestionResult(mTaskName, YES_OPTION);

        mYesNoSwitchButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                if(position == 0){
                    mPresenter.setQuestionResult(mTaskName, YES_OPTION);
                }
                else if(position == 1){
                    mPresenter.setQuestionResult(mTaskName, NO_OPTION);
                }
            }
        });
        mQuestionText.setText(mQuestion);
        return view;
    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public void displaySaveSeizureValidation() {

    }
}
