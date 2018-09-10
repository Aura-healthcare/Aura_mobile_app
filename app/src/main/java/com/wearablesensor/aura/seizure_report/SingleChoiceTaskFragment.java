/**
 * @file SingleChoiceTaskFragment
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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class SingleChoiceTaskFragment extends Fragment implements SeizureReportContract.View{

    private static final String QUESTION_KEY = "question";
    private static final String TASK_NAME = "task-name";
    private static final String TASK_INDEX = "task-index";
    private static final String CHOICE_LIST = "choice_list";

    private static final String UNKNOWN_OPTION = "unknown";

    private int mTaskIndex;
    private String mTaskName;
    private String mQuestion;
    private SingleChoiceList mChoiceList;

    private SeizureReportContract.Presenter mPresenter;

    @BindView(R.id.yes_no_question)TextView mQuestionText;
    @BindView(R.id.next_button) Button mNextButton;
    @OnClick(R.id.next_button)
    public void goToNext(View v){
        mPresenter.nextAdditionalInformationSeizureOnSeizure(mTaskIndex);
    }

    @BindView(R.id.choices_list) RadioGroup mRadioGroup;
    @BindView(R.id.option_description) TextView mOptionDescription;

    public SingleChoiceTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment YesNoTaskFragment
     */

    public static SingleChoiceTaskFragment newInstance(String iQuestion, String iTaskName, SingleChoiceList iChoiceList, int iIndex) {
        SingleChoiceTaskFragment lFragment = new SingleChoiceTaskFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_KEY, iQuestion);
        args.putString(TASK_NAME, iTaskName);
        args.putInt(TASK_INDEX, iIndex);
        args.putParcelable(CHOICE_LIST, iChoiceList);

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
            mChoiceList = getArguments().getParcelable(CHOICE_LIST);
        }

        View view = inflater.inflate(R.layout.fragment_single_choice_task, container, false);
        ButterKnife.bind(this, view);

        mQuestionText.setText(mQuestion);
        buildRadioButtonOptions(mChoiceList);
        mPresenter.setQuestionResult(mTaskName, UNKNOWN_OPTION);
        mRadioGroup.setSelected(false);

        return view;
    }

    /**
     * @brief build radio button list from input choice list
     */
    private void buildRadioButtonOptions(SingleChoiceList iChoiceList) {
        for(SingleChoice lChoice : iChoiceList.getList()){
            RadioButton lRadioButton = buildRadioButtonOption(lChoice);
            mRadioGroup.addView(lRadioButton);
        }
    }

    /**
     * @brief build radio button option from single choice
     * @param iChoice single choice input
     * @return created radio button
     */
    private RadioButton buildRadioButtonOption(final SingleChoice iChoice){
        RadioButton lButton = new RadioButton(getActivity());
        lButton.setId(iChoice.getId());
        lButton.setButtonDrawable(null);
        lButton.setText(iChoice.getHeadline());
        lButton.setGravity(Gravity.CENTER);
        lButton.setTextColor(getResources().getColorStateList(R.color.single_choice_color_selector));
        lButton.setCompoundDrawablesWithIntrinsicBounds(0, iChoice.getButtonIconSelector(), 0, 0);
        lButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        lButton.setOnClickListener(new RadioButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                mOptionDescription.setText(iChoice.getDescription());
                mPresenter.setQuestionResult(mTaskName, iChoice.getValue());
            }
        });
        return lButton;
    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public void displaySaveSeizureValidation() {

    }
}
