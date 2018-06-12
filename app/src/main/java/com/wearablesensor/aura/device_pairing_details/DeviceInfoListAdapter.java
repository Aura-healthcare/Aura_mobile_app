/**
 * @file DeviceInfoListAdapter.java
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
package com.wearablesensor.aura.device_pairing_details;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.device_pairing.AuraDevicePairingCompatibility;
import com.wearablesensor.aura.device_pairing.DeviceInfo;

public class DeviceInfoListAdapter extends ArrayAdapter<DeviceInfo>{
    public DeviceInfoListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DeviceInfo lDeviceInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_info_item, parent, false);
        }
        // Lookup view for data population

        ImageView lDeviceSymbol = (ImageView) convertView.findViewById(R.id.device_symbol);
        ImageView lDeviceBatteryLevelImage = (ImageView) convertView.findViewById(R.id.device_battery_level);
        lDeviceBatteryLevelImage.setImageResource(getBatteryLevelImage(lDeviceInfo.getBatteryLevel()));
        lDeviceSymbol.setImageResource(getDeviceSymbol(lDeviceInfo));
        // Return the completed view to render on screen
        return convertView;
    }

    private int getDeviceSymbol(DeviceInfo lDeviceInfo) {
        if( AuraDevicePairingCompatibility.isHeartRateCompatibleDevice(lDeviceInfo.getName()) ){
            return R.drawable.hrv_connected;
        }
        else if( AuraDevicePairingCompatibility.isMetaWearCompatibleDevice(lDeviceInfo.getName()) ){
            return R.drawable.accelerometer_picture_connected;
        }
        else {
            return R.drawable.electro_dermal_activity_picture_connected;
        }
    }

    private int getBatteryLevelImage(BatteryLevel iBatteryLevel){
        switch (iBatteryLevel){
            case UNKNOWN:
                return R.drawable.battery_unknown;
            case VERY_LOW:
                return R.drawable.battery_very_low;
            case LOW:
                return R.drawable.battery_low;
            case MEDIUM:
                return R.drawable.battery_medium;
            case HIGH:
                return R.drawable.battery_high;
        }

        return 0;
    }
}
