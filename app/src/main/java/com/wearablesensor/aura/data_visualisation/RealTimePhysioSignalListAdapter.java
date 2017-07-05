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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;


public class RealTimePhysioSignalListAdapter extends ArrayAdapter<RRIntervalModel> {

    public RealTimePhysioSignalListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RRIntervalModel lRrInterval = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.realtime_physio_signal_item, parent, false);
        }
        // Lookup view for data population
        TextView lRrValueView = (TextView) convertView.findViewById(R.id.hrv_realtime_value);
        TextView lDeviceAdressView = (TextView) convertView.findViewById(R.id.hrv_device_adress);
        // Populate the data into the template view using the data object
        lRrValueView.setText( Integer.toString(lRrInterval.getRrInterval()) );
        lDeviceAdressView.setText(lRrInterval.getDeviceAdress());

        // Return the completed view to render on screen
        return convertView;
    }
}
