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
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyalbira.loadingdots.LoadingDots;
import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.device_pairing.AuraDevicePairingCompatibility;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.DeviceInfo;

public class DiscoveredDeviceInfoListAdapter extends ArrayAdapter<BleDevice>{

    private boolean mIsConnecting;

    public DiscoveredDeviceInfoListAdapter(Context context, int resource) {
        super(context, resource);
        mIsConnecting = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BleDevice lBleDevice = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discovered_device_info_item, parent, false);
        }
        // Lookup view for data population
        TextView lDeviceNameText = (TextView) convertView.findViewById(R.id.device_pairing_name);
        TextView lDeviceAdressText = (TextView) convertView.findViewById(R.id.device_pairing_adress);
        ImageView lDeviceStatus = (ImageView) convertView.findViewById(R.id.device_scan_status);
        ImageView lDeviceType = (ImageView) convertView.findViewById(R.id.device_scan_type);

        LoadingDots lDeviceLoadingInProgress = (LoadingDots) convertView.findViewById(R.id.device_scan_inProgress);


        // Populate the data into the template view using the data object
        lDeviceNameText.setText(lBleDevice.getName_native());
        lDeviceAdressText.setText(lBleDevice.getMacAddress());

        if(mIsConnecting){
            if (lBleDevice.is(BleDeviceState.CONNECTING)) {
                lDeviceStatus.setVisibility(View.GONE);
                lDeviceLoadingInProgress.setVisibility(View.VISIBLE);
                lDeviceNameText.setTextColor(Color.BLACK);
                lDeviceAdressText.setTextColor(Color.BLACK);
            }
            else{
                lDeviceStatus.setVisibility(View.VISIBLE);
                lDeviceNameText.setTextColor(Color.LTGRAY);
                lDeviceAdressText.setTextColor(Color.LTGRAY);
                lDeviceLoadingInProgress.setVisibility(View.GONE);
            }
        }
        else {
            if (lBleDevice.is(BleDeviceState.CONNECTED)) {
                enableDeviceType(lDeviceType, lBleDevice);
                lDeviceStatus.setImageResource(R.drawable.icon_pairing_symbol_connected);
                lDeviceStatus.setVisibility(View.VISIBLE);
                lDeviceLoadingInProgress.setVisibility(View.GONE);
                lDeviceNameText.setTextColor(Color.BLACK);
                lDeviceAdressText.setTextColor(Color.BLACK);
            } else if (lBleDevice.is(BleDeviceState.CONNECTING)) {
                lDeviceStatus.setVisibility(View.GONE);
                lDeviceLoadingInProgress.setVisibility(View.VISIBLE);
                lDeviceNameText.setTextColor(Color.BLACK);
                lDeviceAdressText.setTextColor(Color.BLACK);
            } else {
                disableDeviceType(lDeviceType, lBleDevice);
                lDeviceStatus.setImageResource(R.drawable.icon_pairing_symbol_neutral);
                lDeviceStatus.setVisibility(View.VISIBLE);
                lDeviceLoadingInProgress.setVisibility(View.GONE);
                lDeviceNameText.setTextColor(Color.BLACK);
                lDeviceAdressText.setTextColor(Color.BLACK);
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }

    private void disableDeviceType(ImageView iDeviceType, BleDevice iDevice){
        if(AuraDevicePairingCompatibility.isHeartRateCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.hrv_pulse_disable);
        }
        else if(AuraDevicePairingCompatibility.isMetaWearCompatibleDevice(iDevice.getName_native()) || AuraDevicePairingCompatibility.isMotionMovuinoCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.accelerometer_picture_disable);
        }
        else if(AuraDevicePairingCompatibility.isGSRTemperatureCustomCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.electro_dermal_activity_picture_disable);
        }
    }

    private void enableDeviceType(ImageView iDeviceType, BleDevice iDevice){
        if(AuraDevicePairingCompatibility.isHeartRateCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.hrv_connected);
        }
        else if(AuraDevicePairingCompatibility.isMetaWearCompatibleDevice(iDevice.getName_native()) || AuraDevicePairingCompatibility.isMotionMovuinoCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.accelerometer_picture_connected);
        }
        else if(AuraDevicePairingCompatibility.isGSRTemperatureCustomCompatibleDevice(iDevice.getName_native())){
            iDeviceType.setImageResource(R.drawable.electro_dermal_activity_picture_connected);
        }
    }
    public void setIsConnecting(boolean iIsConnecting){
        mIsConnecting = iIsConnecting;
    }
}
