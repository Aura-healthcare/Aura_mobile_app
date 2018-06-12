/**
 * @file RealTimePhysioSignalListAdapter.java
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.MotionMagnetometerModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import java.util.ArrayList;


public class RealTimePhysioSignalListAdapter extends ArrayAdapter<PhysioSignalModel> {

    private Bitmap lHrvBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hrv_connected);

    public RealTimePhysioSignalListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PhysioSignalModel lPhysioSignal = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        if(lPhysioSignal.getType().equals(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL) || lPhysioSignal.getType().equals(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL) || lPhysioSignal.getType().equals(MotionMagnetometerModel.MOTION_MAGNETOMETER_MODEL)){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.realtime_physio_chart_item, parent, false);
        }
        else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.realtime_physio_signal_item, parent, false);
        }

        // Lookup view for data population

        ImageView lImageView = (ImageView) convertView.findViewById(R.id.physio_signal_item_picture);
        TextView lValueView = (TextView) convertView.findViewById(R.id.physio_signal_item_value);

        HorizontalBarChart lChart = (HorizontalBarChart) convertView.findViewById(R.id.physio_signal_chart_data);
        // Populate the data into the template view using the data object
        if(lPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE)) {
            RRIntervalModel lRrInterval = ((RRIntervalModel) lPhysioSignal);

            //Bitmap lHrvBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hrv_picture);
            lImageView.setImageBitmap(lHrvBitmap);
            lValueView.setText(String.valueOf(Math.round(60000.0 / lRrInterval.getRrInterval() * 1.0)) + " bpm");
        }
        else if(lPhysioSignal.getType().equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE)){
            SkinTemperatureModel lSkinTemperatureModel = ((SkinTemperatureModel) lPhysioSignal);

            Bitmap lSkinTemperatureBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.skin_temperature_picture_connected);
            lImageView.setImageBitmap(lSkinTemperatureBitmap);
            lValueView.setText(String.valueOf(lSkinTemperatureModel.getTemperature()) + " Celsius");
        }
        else if(lPhysioSignal.getType().equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            ElectroDermalActivityModel lElectroDermalActivityModel = ((ElectroDermalActivityModel) lPhysioSignal);

            Bitmap lElectroDermalActivityBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.electro_dermal_activity_picture_connected);
            lImageView.setImageBitmap(lElectroDermalActivityBitmap);
            lValueView.setText(String.format("%.2f", lElectroDermalActivityModel.getElectroDermalActivity()) + " microSiemens" );
        }
        else if(lPhysioSignal.getType().equals(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL)){
            MotionAccelerometerModel lMotionAccelerometerModel = ((MotionAccelerometerModel) lPhysioSignal);

            Bitmap lMotionAccelerometerBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.accelerometer_picture_connected);
            lImageView.setImageBitmap(lMotionAccelerometerBitmap);
            float[] lAccelerometerValues = lMotionAccelerometerModel.getAccelerometer();

            lChart.setOnChartValueSelectedListener(null);

            lChart.setDrawBarShadow(false);
            lChart.setDrawValueAboveBar(false);
            lChart.getDescription().setEnabled(false);
            lChart.setMaxVisibleValueCount(3);
            lChart.setDrawGridBackground(false);

            XAxis xl = lChart.getXAxis();
            xl.setEnabled(false);

            YAxis yl = lChart.getAxisLeft();
            yl.setEnabled(false);
            yl.setAxisMinimum(-2.0f);
            yl.setAxisMaximum(2.0f);

            YAxis yr = lChart.getAxisRight();
            yr.setEnabled(false);
            yr.setAxisMinimum(-2.0f); // this replaces setStartAtZero(true)
            yr.setAxisMaximum(2.0f);

            setData(lAccelerometerValues, lChart);
            lChart.setFitBars(true);

            Legend l = lChart.getLegend();
            l.setEnabled(false);
        }
        else if(lPhysioSignal.getType().equals(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL)){
            MotionGyroscopeModel lMotionGyroscopeModel = ((MotionGyroscopeModel) lPhysioSignal);

            Bitmap lMotionGyroscopeBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gyroscope_picture_connected);
            lImageView.setImageBitmap(lMotionGyroscopeBitmap);
            float[] lGyroscopeValues = lMotionGyroscopeModel.getGyroscope();

            lChart.setOnChartValueSelectedListener(null);

            lChart.setDrawBarShadow(false);
            lChart.setDrawValueAboveBar(false);
            lChart.getDescription().setEnabled(false);

            lChart.setMaxVisibleValueCount(3);
            lChart.setDrawGridBackground(false);

            XAxis xl = lChart.getXAxis();
            xl.setEnabled(false);

            YAxis yl = lChart.getAxisLeft();
            yl.setEnabled(false);
            yl.setAxisMinimum(-250.0f); // this replaces setStartAtZero(true)
            yl.setAxisMaximum(250.0f);

            YAxis yr = lChart.getAxisRight();
            yr.setEnabled(false);
            yr.setAxisMinimum(-250.0f); // this replaces setStartAtZero(true)
            yr.setAxisMaximum(250.0f);

            setData(lGyroscopeValues, lChart);
            lChart.setFitBars(true);

            Legend l = lChart.getLegend();
            l.setEnabled(false);

        }
        else if(lPhysioSignal.getType().equals(MotionMagnetometerModel.MOTION_MAGNETOMETER_MODEL)){
            MotionMagnetometerModel lMotionMagnetometerModel = ((MotionMagnetometerModel) lPhysioSignal);

            Bitmap lMotionMagnetometerBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.magnetometer_picture);
            lImageView.setImageBitmap(lMotionMagnetometerBitmap);
            float[] lMagnetometerValues = lMotionMagnetometerModel.getMagnetometer();
            lValueView.setText(String.format("%.2f", lMagnetometerValues[0]/1000000) + " " + String.format("%.2f", lMagnetometerValues[1]/1000000) + " " + String.format("%.2f", lMagnetometerValues[2]/1000000));
        }
        // Return the completed view to render on screen
        return convertView;
    }

   private void setData(float[] iValues, HorizontalBarChart iChart) {

       float barWidth = 10f;
       float spaceForBar = 12f;
       ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

       for (int i = 0; i < 3; i++) {
           yVals1.add(new BarEntry(i * spaceForBar, iValues[i]));
       }

       BarDataSet iSet;

       if (iChart.getData() != null &&
               iChart.getData().getDataSetCount() > 0) {
           iSet = (BarDataSet)iChart.getData().getDataSetByIndex(0);
           iSet.setValues(yVals1);
           iChart.getData().notifyDataChanged();
           iChart.notifyDataSetChanged();
       } else {
           iSet = new BarDataSet(yVals1, "DataSet 1");

           iSet.setDrawIcons(false);
           iSet.setColor(getContext().getResources().getColor(R.color.splashscreen_light));
           ArrayList<IBarDataSet> lDataSets = new ArrayList<IBarDataSet>();
           lDataSets.add(iSet);

           BarData lData = new BarData(lDataSets);
           lData.setBarWidth(barWidth);
           iChart.setData(lData);
       }
   }

    public void enterHeartBeatAnomalyMode() {
        lHrvBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hrv_pulse_anomaly);
    }

    public void leavHeartBeatAnomalyMode() {
        lHrvBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hrv_connected);
    }
}
