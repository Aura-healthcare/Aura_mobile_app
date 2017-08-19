/**
 * @file PhysioSignalVisualisationFragment.java
 * @author  clecoued <clement.lecouedic@aura.healthcare>
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

package com.wearablesensor.aura.data_visualisation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhysioSignalVisualisationFragment extends Fragment implements DataVisualisationContract.View {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.realtime_physio_signal_list_view) ListView mRealtimePhysioSignalListView;
    private RealTimePhysioSignalListAdapter mPhysioSignalListAdapter;

    private HashMap<String, PhysioSignalModel> mCurrentRRIntervals;

    private OnFragmentInteractionListener mListener;

    private DataVisualisationContract.Presenter mPresenter;

    public PhysioSignalVisualisationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RRSamplesVisualisationFragment.
     */
    public static PhysioSignalVisualisationFragment newInstance() {
        PhysioSignalVisualisationFragment fragment = new PhysioSignalVisualisationFragment();
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
        View view = inflater.inflate(R.layout.fragment_hrv_realtime_display, container, false);
        ButterKnife.bind(this, view);

        mCurrentRRIntervals = new HashMap<>();
        mPhysioSignalListAdapter = new RealTimePhysioSignalListAdapter(this.getContext(), R.layout.realtime_physio_signal_item);
        mRealtimePhysioSignalListView.setAdapter(mPhysioSignalListAdapter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHRVRealTimeDisplayFragmentInteraction(uri);
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
    /**
     * @brief enable data visualisation on app
     */
    public void enablePhysioSignalVisualisation() {
        mPhysioSignalListAdapter.clear();
        for(Map.Entry<String, PhysioSignalModel> lEntry : mCurrentRRIntervals.entrySet()) {
            mPhysioSignalListAdapter.add(lEntry.getValue());
        }

        mPhysioSignalListAdapter.notifyDataSetChanged();
    }

    @Override
    /**
     * @brief disable data visualisation on app
     */
    public void disablePhysioSignalVisualisation() {
        mCurrentRRIntervals.clear();

        mPhysioSignalListAdapter.clear();
        mPhysioSignalListAdapter.notifyDataSetChanged();
    }

    @Override
    /**
     * @brief  refresh data visualisation when receiving a new data sample
     *
     * @param physiological data sample
     */
    public void refreshPhysioSignalVisualisation(PhysioSignalModel iPhysioSignal) {
        mCurrentRRIntervals.put(iPhysioSignal.getDeviceAdress() + "-" + iPhysioSignal.getType(), iPhysioSignal);
        mPhysioSignalListAdapter.clear();
        for(Map.Entry<String, PhysioSignalModel> lEntry : mCurrentRRIntervals.entrySet()) {
            mPhysioSignalListAdapter.add(lEntry.getValue());
        }

        mPhysioSignalListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(DataVisualisationContract.Presenter iPresenter) {
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
        void onHRVRealTimeDisplayFragmentInteraction(Uri uri);
    }
}
