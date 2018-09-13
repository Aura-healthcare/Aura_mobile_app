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

package com.wearablesensor.aura.device_pairing_details;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.device_pairing.DeviceInfo;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DevicePairingDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DevicePairingDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DevicePairingDetailsFragment extends Fragment implements DevicePairingDetailsContract.View {

    private final String TAG = this.getClass().getSimpleName();

    private DevicePairingDetailsContract.Presenter mPresenter;

    private ArrayAdapter<DeviceInfo> mDeviceListAdapter;

    @BindView(R.id.device_pairing_layout) GridView mDeviceGridView;
    @BindView(R.id.device_pairing_button) Button mDevicePairingButton;
    @OnClick(R.id.device_pairing_button)
    public void OnClickDevicePairingButton(View v){
        mPresenter.startScanning();
    }
    @BindView(R.id.session_duration) TextView mSessionDuration;

    public DevicePairingDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment DevicePairingFragment.
     */

    public static DevicePairingDetailsFragment newInstance() {
        DevicePairingDetailsFragment fragment = new DevicePairingDetailsFragment();
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
        View view = inflater.inflate(R.layout.fragment_device_pairing, container, false);
        ButterKnife.bind(this, view);

        mDeviceListAdapter = new DeviceInfoListAdapter(this.getContext(), R.layout.device_info_item);
        mDeviceGridView.setAdapter(mDeviceListAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void successPairing(LinkedList<DeviceInfo> iDeviceList) {
        mDeviceListAdapter.clear();
        mDeviceListAdapter.addAll(iDeviceList);

        mDeviceListAdapter.notifyDataSetChanged();

        mDeviceGridView.setVisibility(View.VISIBLE);
        mDevicePairingButton.setVisibility(View.GONE);
        mSessionDuration.setVisibility(View.VISIBLE);
    }

    @Override
    public void failParing() {
        mDeviceListAdapter.clear();

        mDeviceListAdapter.notifyDataSetChanged();

        mDeviceGridView.setVisibility(View.GONE);
        mDevicePairingButton.setVisibility(View.VISIBLE);
        mSessionDuration.setVisibility(View.GONE);
    }

    @Override
    public void refreshDeviceBatteryLevel(DeviceInfo iDeviceInfo) {
        for(int i=0 ; i<mDeviceListAdapter.getCount() ; i++){
            DeviceInfo lDevice = mDeviceListAdapter.getItem(i);
            if( lDevice.getId().equals(iDeviceInfo.getId()) ){
                lDevice.setBatteryLevel(iDeviceInfo.getBatteryLevel());
            }
        }

        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void progressPairing(){

    }

    @Override
    public void setPresenter(DevicePairingDetailsContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public void refreshSessionDuration(long lSessionDuration) {
        String lDuration = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(lSessionDuration),
                TimeUnit.MILLISECONDS.toMinutes(lSessionDuration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(lSessionDuration)));
        mSessionDuration.setText(lDuration);
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
        void onDevicePairingAttempt();
    }
}
