/**
 * @file FreeTextTaskFragment
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

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.wearablesensor.aura.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class FreeTextTaskFragment extends Fragment implements SeizureReportContract.View{

    private static final String QUESTION_KEY = "question";
    private static final String TASK_NAME = "task-name";
    private static final String TASK_INDEX = "task-index";

    private static final String UNKNOWN_OPTION = "unknown";

    private int mTaskIndex;
    private String mTaskName;
    private String mQuestion;

    private SeizureReportContract.Presenter mPresenter;

    @BindView(R.id.free_text_question)TextView mQuestionText;

    @BindView(R.id.free_text_section) TextInputEditText mFreeText;
    @OnEditorAction(R.id.free_text_section)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (actionId == EditorInfo.IME_ACTION_SEND) {
            hideKeyboard();
            completeFreeTextValidation();
            handled = true;
        }
        return handled;
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public FreeTextTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment YesNoTaskFragment
     */

    public static FreeTextTaskFragment newInstance(String iQuestion, String iTaskName, int iIndex) {
        FreeTextTaskFragment lFragment = new FreeTextTaskFragment();
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

        View view = inflater.inflate(R.layout.fragment_free_text_task, container, false);
        ButterKnife.bind(this, view);

        mQuestionText.setText(mQuestion);
        return view;
    }

    @Override
    public void onResume(){
        mPresenter.setQuestionResult(mTaskName, UNKNOWN_OPTION);
        mFreeText.getText().clear();
        super.onResume();
    }

    @Override
    public void onStop(){
        hideKeyboard();
        super.onStop();
    }

    @Override
    public void setPresenter(SeizureReportContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public void displaySaveSeizureValidation() {

    }

    private void completeFreeTextValidation(){
        String lComment = mFreeText.getText().toString();
        if(lComment.length() < 100) {
            mPresenter.setQuestionResult(mTaskName, lComment);
        }
        else{
            mPresenter.setQuestionResult(mTaskName, UNKNOWN_OPTION);
        }
        mPresenter.nextAdditionalInformationSeizureOnSeizure(mTaskIndex);
    }
}
