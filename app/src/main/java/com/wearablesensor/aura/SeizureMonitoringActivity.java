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

package com.wearablesensor.aura;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wearablesensor.aura.data_sync.DataSyncFragment;
import com.wearablesensor.aura.data_sync.DataSyncPresenter;
import com.wearablesensor.aura.data_visualisation.DataVisualisationPresenter;
import com.wearablesensor.aura.data_visualisation.RRSamplesVisualisationFragment;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsPresenter;
import com.wearablesensor.aura.seizure_report.SeizureReportPresenter;
import com.wearablesensor.aura.seizure_report.SeizureStatusFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SeizureMonitoringActivity extends AppCompatActivity implements DevicePairingDetailsFragment.OnFragmentInteractionListener, DataSyncFragment.OnFragmentInteractionListener, RRSamplesVisualisationFragment.OnFragmentInteractionListener, SeizureStatusFragment.OnFragmentInteractionListener{

    private final static String TAG = SeizureMonitoringActivity.class.getSimpleName();
    private String[] mDrawerTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.left_drawer) ListView mDrawerList;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private BluetoothDevicePairingService mDevicePairingService;


    private DevicePairingDetailsPresenter mDevicePairingDetailsPresenter;
    private DevicePairingDetailsFragment mDevicePairingFragment;

    private DataSyncPresenter mDataSyncPresenter;
    private DataSyncFragment mDataSyncFragment;

    private DataVisualisationPresenter mDataVisualisationPresenter;
    private RRSamplesVisualisationFragment mRRSamplesVisualisationFragment;

    private SeizureStatusFragment mSeizureStatusFragment;
    private SeizureReportPresenter mSeizureReportPresenter;

    private static final int REQUEST_ENABLE_BT = 1;


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int position) {
            Toast.makeText(getApplicationContext(), "Youhou"+position, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seizure_monitoring);
        mDevicePairingService =  (BluetoothDevicePairingService) ((AuraApplication) getApplication()).getDevicePairingService();

        mDevicePairingFragment = (DevicePairingDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.device_pairing_details_fragment);
        mDevicePairingDetailsPresenter = new DevicePairingDetailsPresenter(mDevicePairingService, mDevicePairingFragment);

        mDataSyncFragment = (DataSyncFragment) getSupportFragmentManager().findFragmentById(R.id.data_sync_fragment);
        mDataSyncPresenter = new DataSyncPresenter( ((AuraApplication) getApplication()).getLocalDataRepository(), ((AuraApplication) getApplication()).getRemoteDataRepository(), mDataSyncFragment, this, ((AuraApplication) getApplication()).getUserSessionService());

        mRRSamplesVisualisationFragment = (RRSamplesVisualisationFragment) getSupportFragmentManager().findFragmentById(R.id.hrv_realtime_display_fragment);
        mDataVisualisationPresenter = new DataVisualisationPresenter(mDevicePairingService, mRRSamplesVisualisationFragment);

        mSeizureStatusFragment = (SeizureStatusFragment) getSupportFragmentManager().findFragmentById(R.id.seizure_status_fragment);
        mSeizureReportPresenter = new SeizureReportPresenter(mSeizureStatusFragment, this);

        ButterKnife.bind(this);

        setupDrawer();

        startAutomaticPairing();
    }

    @Override
    public void onStop(){
        try {
            ((AuraApplication) getApplication()).getLocalDataRepository().clearCache();

        }
        catch(Exception e){
            Log.d(TAG, "Fail to save cache data on exit");
        }
        super.onStop();
    }

    private void setupDrawer(){
        mDrawerTitles = getResources().getStringArray(R.array.drawer_array);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void startAutomaticPairing(){
        if(mDevicePairingService.checkBluetoothIsEnabled()){
            mDevicePairingService.automaticPairing();
            mDevicePairingFragment.progressPairing();
        }
        else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                startAutomaticPairing();
                return;
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                mDevicePairingService.endPairing();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDataSyncFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDevicePairingAttempt() {
        startAutomaticPairing();
    }

    @Override
    public void onHRVRealTimeDisplayFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSeizureStatusFragmentInteraction(Uri uri) {

    }
}
