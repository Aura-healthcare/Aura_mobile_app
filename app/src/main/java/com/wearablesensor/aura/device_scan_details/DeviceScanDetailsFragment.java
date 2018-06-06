/**
 * @file DeviceScanDetailsFragment
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
 */
package com.wearablesensor.aura.device_scan_details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleDevice;
import com.wearablesensor.aura.R;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing_details.DiscoveredDeviceInfoListAdapter;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceScanDetailsFragment extends Fragment implements DeviceScanDetailsContract.View{
    private static final String TAG = DeviceScanDetailsFragment.class.getSimpleName();

    @BindView(R.id.device_pairing_step) TextView mDeviceStepTextView;
    @BindView(R.id.device_pairing_skip_step) TextView mDeviceSkipStep;
    @BindView(R.id.discovered_device_list_view_cardiac) ListView mDiscoveredDevicesListCardiac;
    @BindView(R.id.discovered_device_list_view_motion) ListView mDiscoveredDevicesListMotion;
    @BindView(R.id.discovered_device_list_view_electro_dermal) ListView mDiscoveredDevicesListElectroDermal;

    @BindView(R.id.device_scan_progress_bar) ProgressBar mProgressBar;
    @OnClick(R.id.device_pairing_skip_step)
    public void skipStep(){
            mPresenter.goToSeizureMonitoring();
    }

    @BindView(R.id.device_scan_button) Button mScanButton;
    @OnClick(R.id.device_scan_button)
    public void startScan(){
        mPresenter.startScan();
    }

    private DiscoveredDeviceInfoListAdapter mDiscoveredDevicesListAdapterCardiac;
    private DiscoveredDeviceInfoListAdapter mDiscoveredDevicesListAdapterMotion;
    private DiscoveredDeviceInfoListAdapter mDiscoveredDevicesListAdapterElectroDermal;

    private DeviceScanDetailsContract.Presenter mPresenter;

    public DeviceScanDetailsFragment(){
    }

    public static DeviceScanDetailsFragment newInstance() {
        Log.d(TAG, "newInstance");
        DeviceScanDetailsFragment fragment = new DeviceScanDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_pairing_step, container, false);
        ButterKnife.bind(this, view);

        mDiscoveredDevicesListAdapterCardiac = new DiscoveredDeviceInfoListAdapter(this.getContext(), R.layout.discovered_device_info_item);
        mDiscoveredDevicesListCardiac.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BleDevice lBleDevice = mDiscoveredDevicesListAdapterCardiac.getItem(position);
                mPresenter.connectDevice(lBleDevice);
            }
        });
        mDiscoveredDevicesListCardiac.setAdapter(mDiscoveredDevicesListAdapterCardiac);

        mDiscoveredDevicesListAdapterMotion = new DiscoveredDeviceInfoListAdapter(this.getContext(), R.layout.discovered_device_info_item);
        mDiscoveredDevicesListMotion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice lBleDevice = mDiscoveredDevicesListAdapterMotion.getItem(position);
                mPresenter.connectDevice(lBleDevice);
            }
        });
        mDiscoveredDevicesListMotion.setAdapter(mDiscoveredDevicesListAdapterMotion);

        mDiscoveredDevicesListAdapterElectroDermal = new DiscoveredDeviceInfoListAdapter(this.getContext(), R.layout.discovered_device_info_item);
        mDiscoveredDevicesListElectroDermal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BleDevice lBleDevice = mDiscoveredDevicesListAdapterElectroDermal.getItem(position);
                mPresenter.connectDevice(lBleDevice);
            }
        });
        mDiscoveredDevicesListElectroDermal.setAdapter(mDiscoveredDevicesListAdapterElectroDermal);
        return view;
    }


    public void updateScanList(LinkedList<BleDevice> iDeviceList){
        mDiscoveredDevicesListAdapterCardiac.clear();
        mDiscoveredDevicesListAdapterMotion.clear();
        mDiscoveredDevicesListAdapterElectroDermal.clear();

        for(BleDevice iDevice : iDeviceList){
            if(BluetoothDevicePairingService.isMetaWearCompatibleDevice(iDevice)){
                mDiscoveredDevicesListAdapterMotion.add(iDevice);
            }
            else if(BluetoothDevicePairingService.isHeartRateCompatibleDevice(iDevice)){
                mDiscoveredDevicesListAdapterCardiac.add(iDevice);
            }
            else if(BluetoothDevicePairingService.isGSRTemperatureCustomCompatibleDevice(iDevice)){
                mDiscoveredDevicesListAdapterElectroDermal.add(iDevice);
            }
        }
    }

    private void refreshScanDeviceList() {
        mDiscoveredDevicesListAdapterCardiac.notifyDataSetChanged();
        mDiscoveredDevicesListAdapterMotion.notifyDataSetChanged();
        mDiscoveredDevicesListAdapterElectroDermal.notifyDataSetChanged();

    }

    @Override
    public void displayStartScan(LinkedList<BleDevice> iDeviceList) {
        mProgressBar.setVisibility(View.VISIBLE);
        mScanButton.setVisibility(View.GONE);
        mDeviceSkipStep.setVisibility(View.GONE);

        updateScanList(iDeviceList);
    }

    @Override
    public void displayDeviceDiscovered(LinkedList<BleDevice> iDeviceList) {
        updateScanList(iDeviceList);

        refreshScanDeviceList();
    }

    @Override
    public void displayStartConnecting() {
        mDiscoveredDevicesListAdapterCardiac.setIsConnecting(true);
        mDiscoveredDevicesListAdapterMotion.setIsConnecting(true);
        mDiscoveredDevicesListAdapterElectroDermal.setIsConnecting(true);

        refreshScanDeviceList();
    }

    @Override
    public void displayDeviceConnected() {
        refreshScanDeviceList();

        mDiscoveredDevicesListAdapterCardiac.setIsConnecting(false);
        mDiscoveredDevicesListAdapterMotion.setIsConnecting(false);
        mDiscoveredDevicesListAdapterElectroDermal.setIsConnecting(false);
    }

    @Override
    public void displayDeviceDisconnected() {

        refreshScanDeviceList();
    }

    @Override
    public void displayEndScan(LinkedList<BleDevice> iDeviceList) {
        updateScanList(iDeviceList);

        refreshScanDeviceList();

        mDeviceSkipStep.setVisibility(View.VISIBLE);
        mScanButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void setPresenter(DeviceScanDetailsContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }
}

