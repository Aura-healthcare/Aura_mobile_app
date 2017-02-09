package com.wearablesensor.aura;

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
import com.wearablesensor.aura.data.DateIso8601Mapper;
import com.wearablesensor.aura.data.SampleRRInterval;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HRVRealTimeDisplayFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.hrv_realtime_graph) GraphView mGraphView;
    @BindView(R.id.hrv_realtime_image_view) ImageView mImageView;
    @BindView(R.id.hrv_realtime_value) TextView mHrvTextView;

    private OnFragmentInteractionListener mListener;

    private final int GRAPH_POINT_NB = 3600;
    private final int GRAPH_POINT_SIZE = 5;
    private final int GRAPH_HORIZONTAL_LABEL_NB = 3;
    private final int MAX_HRV_VALUE = 2000;
    private final int MIN_HRV_VALUE = 0;
    private PointsGraphSeries<DataPoint> mSeries;
    public HRVRealTimeDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HRVRealTimeDisplayFragment.
     */
    public static HRVRealTimeDisplayFragment newInstance() {
        HRVRealTimeDisplayFragment fragment = new HRVRealTimeDisplayFragment();
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

        mSeries = new PointsGraphSeries<>();

        mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), new SimpleDateFormat("HH:mm")));
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(GRAPH_HORIZONTAL_LABEL_NB);

        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(MIN_HRV_VALUE);
        mGraphView.getViewport().setMaxY(MAX_HRV_VALUE);

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

    public void initHRVRealTimeData(ArrayList<SampleRRInterval> mRrSamples) {
        DataPoint[] lData;
        if(mRrSamples.size() == 0){
            lData = new DataPoint[]{};
        }
        else {
            int lNbRrSamples = mRrSamples.size();
            lData = new DataPoint[lNbRrSamples];

            for (int i = 0; i < (mRrSamples.size()); i++) {
                lData[i] = new DataPoint(DateIso8601Mapper.getDate(mRrSamples.get(i).getTimestamp()), mRrSamples.get(i).getRR());
            }
        }

        mSeries.resetData(lData);

        int lHrvGrey = getContext().getResources().getColor(R.color.hrv_grey);
        mSeries.setColor(lHrvGrey);
        mSeries.setSize(GRAPH_POINT_SIZE);
    }

    public void displayHRVRealTimeData(Date iWindowStart, Date iWindowEnd){
        mGraphView.addSeries(mSeries);

        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(iWindowStart.getTime());
        mGraphView.getViewport().setMaxX(iWindowEnd.getTime());
    }

    public void enableHRVRealTime() {
        Drawable lEnableHrvPulse = ContextCompat.getDrawable(getContext(), R.drawable.hrv_pulse_enable);
        mImageView.setImageDrawable(lEnableHrvPulse);

        int lHrvBlue = getContext().getResources().getColor(R.color.hrv_blue);
        mSeries.setColor(lHrvBlue);
        mGraphView.invalidate();
    }

    public void disableHRVRealTime(){
        mHrvTextView.setText(getString(R.string.default_hrv));

        Drawable lDisableHrvPulse = ContextCompat.getDrawable(getContext(), R.drawable.hrv_pulse_disable);
        mImageView.setImageDrawable(lDisableHrvPulse);

        int lHrvGrey = getContext().getResources().getColor(R.color.hrv_grey);
        mSeries.setColor(lHrvGrey);
    }

    public void addNewHRVData(SampleRRInterval iSampleRrInterval)
    {
        int lCurrentRr = iSampleRrInterval.getRR();
        Date lCurrentDate = DateIso8601Mapper.getDate(iSampleRrInterval.getTimestamp());

        mHrvTextView.setText(lCurrentRr + " ms");

        mSeries.appendData( new DataPoint(lCurrentDate, lCurrentRr), true, GRAPH_POINT_NB);
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
