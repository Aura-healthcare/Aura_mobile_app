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

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.MotionMagnetometerModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;


public class RealTimePhysioSignalListAdapter extends ArrayAdapter<PhysioSignalModel> {

    public RealTimePhysioSignalListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PhysioSignalModel lPhysioSignal = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.realtime_physio_signal_item, parent, false);
        }
        // Lookup view for data population

        ImageView lImageView = (ImageView) convertView.findViewById(R.id.physio_signal_item_picture);
        TextView lValueView = (TextView) convertView.findViewById(R.id.physio_signal_item_value);
        TextView lDeviceAdressView = (TextView) convertView.findViewById(R.id.physio_signal_item_device_adress);

        // Populate the data into the template view using the data object
        if(lPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE)) {
            RRIntervalModel lRrInterval = ((RRIntervalModel) lPhysioSignal);

            Bitmap lHrvBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hrv_picture);
            lImageView.setImageBitmap(lHrvBitmap);
            lValueView.setText(String.valueOf(Math.round(60000.0 / lRrInterval.getRrInterval() * 1.0)) + " bpm");
            lDeviceAdressView.setText(lRrInterval.getDeviceAdress());
        }
        else if(lPhysioSignal.getType().equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE)){
            SkinTemperatureModel lSkinTemperatureModel = ((SkinTemperatureModel) lPhysioSignal);

            Bitmap lSkinTemperatureBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.skin_temperature_picture);
            lImageView.setImageBitmap(lSkinTemperatureBitmap);
            lValueView.setText(String.valueOf(lSkinTemperatureModel.getTemperature()) + " Celsius");
            lDeviceAdressView.setText(lSkinTemperatureModel.getDeviceAdress());
        }
        else if(lPhysioSignal.getType().equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            ElectroDermalActivityModel lElectroDermalActivityModel = ((ElectroDermalActivityModel) lPhysioSignal);

            Bitmap lElectroDermalActivityBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.electro_dermal_activity_picture);
            lImageView.setImageBitmap(lElectroDermalActivityBitmap);
            lValueView.setText(String.valueOf((float) (lElectroDermalActivityModel.getElectroDermalActivity() * 1.0/ 1000)) + " kOhm" );
            lDeviceAdressView.setText(lElectroDermalActivityModel.getDeviceAdress());
        }
        else if(lPhysioSignal.getType().equals(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL)){
            MotionAccelerometerModel lMotionAccelerometerModel = ((MotionAccelerometerModel) lPhysioSignal);

            Bitmap lMotionAccelerometerBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.accelerometer_picture);
            lImageView.setImageBitmap(lMotionAccelerometerBitmap);
            float[] lAccelerometerValues = lMotionAccelerometerModel.getAccelerometer();
            lValueView.setText(String.format("%.2f", lAccelerometerValues[0]) + " " + String.format("%.2f", lAccelerometerValues[1]) + " " + String.format("%.2f", lAccelerometerValues[2]));
            lDeviceAdressView.setText(lMotionAccelerometerModel.getDeviceAdress());
        }
        else if(lPhysioSignal.getType().equals(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL)){
            MotionGyroscopeModel lMotionGyroscopeModel = ((MotionGyroscopeModel) lPhysioSignal);

            Bitmap lMotionGyroscopeBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gyroscope_picture);
            lImageView.setImageBitmap(lMotionGyroscopeBitmap);
            float[] lGyroscopeValues = lMotionGyroscopeModel.getGyroscope();
            lValueView.setText(String.format("%.2f", lGyroscopeValues[0]) + " " + String.format("%.2f", lGyroscopeValues[1]) + " " + String.format("%.2f", lGyroscopeValues[2]));
            lDeviceAdressView.setText(lMotionGyroscopeModel.getDeviceAdress());
        }
        else if(lPhysioSignal.getType().equals(MotionMagnetometerModel.MOTION_MAGNETOMETER_MODEL)){
            MotionMagnetometerModel lMotionMagnetometerModel = ((MotionMagnetometerModel) lPhysioSignal);

            Bitmap lMotionMagnetometerBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.magnetometer_picture);
            lImageView.setImageBitmap(lMotionMagnetometerBitmap);
            float[] lMagnetometerValues = lMotionMagnetometerModel.getMagnetometer();
            lValueView.setText(String.format("%.2f", lMagnetometerValues[0]/1000000) + " " + String.format("%.2f", lMagnetometerValues[1]/1000000) + " " + String.format("%.2f", lMagnetometerValues[2]/1000000));
            lDeviceAdressView.setText(lMotionMagnetometerModel.getDeviceAdress());
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
