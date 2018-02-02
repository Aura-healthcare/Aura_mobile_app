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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhysioSignalGraphVisualisationFragment extends Fragment implements DataVisualisationContract.View {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.chart) LineChart mLineChart;
    @BindView(R.id.chart2) LineChart mLineChart2;
    @BindView(R.id.chart3) LineChart mLineChart3;

    private final int sColorBlue1 = Color.rgb(100,200, 255);
    private final int sColorBlue2 = Color.rgb(130,130,255);
    private final int sColorBlue3 = Color.rgb(200,200, 255);
    private OnFragmentInteractionListener mListener;

    private DataVisualisationContract.Presenter mPresenter;

    private double mMaxY = 8.0;

    public PhysioSignalGraphVisualisationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RRSamplesVisualisationFragment.
     */
    public static PhysioSignalGraphVisualisationFragment newInstance() {
        PhysioSignalGraphVisualisationFragment fragment = new PhysioSignalGraphVisualisationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}


    }

    private void setupChart() {
        mLineChart.setVisibleXRangeMaximum(100);
        mLineChart2.setVisibleXRangeMaximum(100);
        mLineChart3.setVisibleXRangeMaximum(100);
    }

    private void setupAxes() {
        XAxis xl = mLineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        XAxis xl2 = mLineChart2.getXAxis();
        xl2.setTextColor(Color.WHITE);
        xl2.setDrawGridLines(false);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setEnabled(true);

        XAxis xl3 = mLineChart3.getXAxis();
        xl3.setTextColor(Color.WHITE);
        xl3.setDrawGridLines(false);
        xl3.setAvoidFirstLastClipping(true);
        xl3.setEnabled(true);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(8f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis2 = mLineChart2.getAxisLeft();
        leftAxis2.setTextColor(Color.WHITE);
        leftAxis2.setAxisMaximum(2000);
        leftAxis2.setAxisMinimum(500);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = mLineChart2.getAxisRight();
        rightAxis2.setEnabled(false);

        YAxis leftAxis3 = mLineChart3.getAxisLeft();
        leftAxis3.setTextColor(Color.WHITE);
        leftAxis3.setAxisMaximum(45);
        leftAxis3.setAxisMinimum(30);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = mLineChart3.getAxisRight();
        rightAxis3.setEnabled(false);
    }

    private void setupData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.WHITE);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.WHITE);
        // add empty data
        mLineChart.setData(data);
        mLineChart2.setData(data2);
        mLineChart3.setData(data3);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);

        // get the legend (only possible after setting data)
        Legend l2 = mLineChart2.getLegend();

        // modify the legend ...
        l2.setForm(Legend.LegendForm.CIRCLE);
        l2.setTextColor(Color.WHITE);

        // get the legend (only possible after setting data)
        Legend l3 = mLineChart3.getLegend();

        // modify the legend ...
        l3.setForm(Legend.LegendForm.CIRCLE);
        l3.setTextColor(Color.WHITE);
    }

    private LineDataSet createSet(int iColorTemplate) {
        LineDataSet set = new LineDataSet(null, "Memory Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(iColorTemplate);
        set.setCircleColor(Color.DKGRAY);
        set.setLineWidth(2f);
        set.setCircleRadius(3.0f);
        set.setValueTextColor(Color.GRAY);
        set.setValueTextSize(5f);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph_visualisation, container, false);
        ButterKnife.bind(this, view);

        setupChart();
        setupAxes();
        setupData();
        setLegend();

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
        mPresenter.clearCache();
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

    }

    @Override
    /**
     * @brief disable data visualisation on app
     */
    public void disablePhysioSignalVisualisation() {

    }

    @Override
    /**
     * @brief  refresh data visualisation when receiving a new data sample
     *
     * @param physiological data sample
     */
    public void refreshPhysioSignalVisualisation(PhysioSignalModel iPhysioSignal) {
        if(iPhysioSignal.getType() == ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY) {
            addElectroDermalEntry((ElectroDermalActivityModel)iPhysioSignal);
        }
        else if(iPhysioSignal.getType() == SkinTemperatureModel.SKIN_TEMPERATURE_TYPE){
            addTemperatureEntry((SkinTemperatureModel)iPhysioSignal);
        }
        else if(iPhysioSignal.getType() == RRIntervalModel.RR_INTERVAL_TYPE){
            addRRIntervalEntry((RRIntervalModel)iPhysioSignal);
        }
    }

    @Override
    public void updateDataSeriesStatus(String iType, Boolean iDataSerieValidate) {
        Log.d(TAG, "updateDataSeriesStatus");

        if(iType.equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            LineDataSet lSet = ((LineDataSet) mLineChart.getLineData().getDataSets().get(0));
            if(!iDataSerieValidate) {
                lSet.setColors(Color.RED);
                Log.d(TAG, " DataSerieINVALID");
            }
            else{
                lSet.setColors(sColorBlue1);
                Log.d(TAG, " DataSerieVALID");
            }
        }
        else if(iType.equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE)){
            LineDataSet lSet = ((LineDataSet) mLineChart3.getLineData().getDataSets().get(0));
            if(!iDataSerieValidate) {
                lSet.setColors(Color.RED);
            }
            else{
                lSet.setColors(sColorBlue2);
            }
        }
        else if(iType.equals(RRIntervalModel.RR_INTERVAL_TYPE)){
            LineDataSet lSet = ((LineDataSet) mLineChart2.getLineData().getDataSets().get(0));
            if(!iDataSerieValidate) {
                lSet.setColors(Color.RED);
            }
            else{
                lSet.setColors(sColorBlue3);
            }
        }
    }

    private void addRRIntervalEntry(RRIntervalModel iPhysioSignal) {
        if (mLineChart2 == null) {
            return;
        }
        LineData data = mLineChart2.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet(sColorBlue3);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) iPhysioSignal.getRrInterval()), 0);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mLineChart2.notifyDataSetChanged();

            // limit the number of visible entries
            mLineChart2.setVisibleXRangeMaximum(20);

            // move to the latest entry
            mLineChart2.moveViewToX(data.getEntryCount());
        }
    }

    private void addTemperatureEntry(SkinTemperatureModel iPhysioSignal) {
        if(mLineChart3 == null){
            return;
        }
        LineData data = mLineChart3.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet(sColorBlue2);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) iPhysioSignal.getTemperature()), 0);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mLineChart3.notifyDataSetChanged();

            // limit the number of visible entries
            mLineChart3.setVisibleXRangeMaximum(20);

            // move to the latest entry
            mLineChart3.moveViewToX(data.getEntryCount());
        }
    }

    private void addElectroDermalEntry(ElectroDermalActivityModel iPhysioSignal) {
        if(mLineChart == null){
            return;
        }
        LineData data = mLineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet(sColorBlue1);
                data.addDataSet(set);
            }

            mMaxY = iPhysioSignal.getElectroDermalActivity() + 2;
            mLineChart.getAxisLeft().setAxisMaximum((float) (mMaxY + 2.0));
            mLineChart.getAxisLeft().resetAxisMaximum();

            data.addEntry(new Entry(set.getEntryCount(), (float) iPhysioSignal.getElectroDermalActivity()), 0);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mLineChart.notifyDataSetChanged();

            // limit the number of visible entries
            mLineChart.setVisibleXRangeMaximum(20);

            // move to the latest entry
            mLineChart.moveViewToX(data.getEntryCount());
        }
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
