/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.data_visualisation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RRSamplesVisualisationFragment extends Fragment implements DataVisualisationContract.View {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.hrv_realtime_graph) GraphView mGraphView;
    @BindView(R.id.hrv_realtime_image_view) ImageView mImageView;
    @BindView(R.id.hrv_realtime_value) TextView mHrvTextView;

    private OnFragmentInteractionListener mListener;

    private DataVisualisationContract.Presenter mPresenter;

    private final int GRAPH_POINT_NB = 3600;
    private final int GRAPH_POINT_SIZE = 5;
    private final int GRAPH_HORIZONTAL_LABEL_NB = 3;
    private final int MAX_HRV_VALUE = 2000;
    private final int MIN_HRV_VALUE = 0;
    // TODO: points graph series should be handled by DataSyncPresenter
    private PointsGraphSeries<DataPoint> mSeries;
    public RRSamplesVisualisationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RRSamplesVisualisationFragment.
     */
    public static RRSamplesVisualisationFragment newInstance() {
        RRSamplesVisualisationFragment fragment = new RRSamplesVisualisationFragment();
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
    public void initRRSamplesVisualisation(Date iWindowStart, Date iWindowEnd) {
        mSeries = new PointsGraphSeries<>();

        mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), new SimpleDateFormat("HH:mm:ss")));
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(GRAPH_HORIZONTAL_LABEL_NB);

        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(MIN_HRV_VALUE);
        mGraphView.getViewport().setMaxY(MAX_HRV_VALUE);

        DataPoint[] lData = new DataPoint[]{};
        mSeries.resetData(lData);

        int lHrvGrey = getContext().getResources().getColor(R.color.hrv_grey);
        mSeries.setColor(lHrvGrey);
        mSeries.setSize(GRAPH_POINT_SIZE);

        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(iWindowStart.getTime());
        mGraphView.getViewport().setMaxX(iWindowEnd.getTime());
        mGraphView.addSeries(mSeries);
    }

    @Override
    public void enableRRSamplesVisualisation() {
        Drawable lEnableHrvPulse = ContextCompat.getDrawable(getContext(), R.drawable.hrv_pulse_enable);
        mImageView.setImageDrawable(lEnableHrvPulse);

        int lHrvBlue = getContext().getResources().getColor(R.color.hrv_blue);
        mSeries.setColor(lHrvBlue);
        mGraphView.invalidate();
    }

    @Override
    public void disableRRSamplesVisualisation() {
        mHrvTextView.setText(getString(R.string.default_hrv));

        Drawable lDisableHrvPulse = ContextCompat.getDrawable(getContext(), R.drawable.hrv_pulse_disable);
        mImageView.setImageDrawable(lDisableHrvPulse);

        int lHrvGrey = getContext().getResources().getColor(R.color.hrv_grey);
        mSeries.setColor(lHrvGrey);
        mGraphView.invalidate();
    }

    @Override
    public void refreshRRSamplesVisualisation(RRIntervalModel iSampleRR) {
        int lCurrentRr = iSampleRR.getRrInterval();
        Date lCurrentDate = DateIso8601Mapper.getDate(iSampleRR.getTimestamp());

        mHrvTextView.setText(lCurrentRr + " ms");

        mSeries.appendData( new DataPoint(lCurrentDate, lCurrentRr), true, GRAPH_POINT_NB);
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
