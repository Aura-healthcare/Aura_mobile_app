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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
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

import com.crashlytics.android.Crashlytics;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.wearablesensor.aura.data_sync.DataSyncFragment;
import com.wearablesensor.aura.data_sync.DataSyncPresenter;
import com.wearablesensor.aura.data_visualisation.DataVisualisationPresenter;
import com.wearablesensor.aura.data_visualisation.PhysioSignalVisualisationFragment;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDisconnectedNotification;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsPresenter;
import com.wearablesensor.aura.seizure_report.SeizureReportFragment;
import com.wearablesensor.aura.seizure_report.SeizureReportPresenter;
import com.wearablesensor.aura.seizure_report.SeizureStatusFragment;
import com.wearablesensor.aura.seizure_report.SeizureStatusPresenter;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SeizureMonitoringActivity extends AppCompatActivity implements DevicePairingDetailsFragment.OnFragmentInteractionListener, DataSyncFragment.OnFragmentInteractionListener, PhysioSignalVisualisationFragment.OnFragmentInteractionListener, SeizureStatusFragment.OnFragmentInteractionListener, SeizureReportFragment.OnFragmentInteractionListener{

    private final static String TAG = SeizureMonitoringActivity.class.getSimpleName();
    private String[] mDrawerTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.left_drawer) ListView mDrawerList;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    private DevicePairingDetailsPresenter mDevicePairingDetailsPresenter;
    private DevicePairingDetailsFragment mDevicePairingFragment;

    private DataSyncPresenter mDataSyncPresenter;
    private DataSyncFragment mDataSyncFragment;

    private DataVisualisationPresenter mDataVisualisationPresenter;
    private PhysioSignalVisualisationFragment mPhysioSignalVisualisationFragment;

    private SeizureStatusFragment mSeizureStatusFragment;
    private SeizureStatusPresenter mSeizureStatusPresenter;

    private SeizureReportFragment mSeizureReportFragment;
    private SeizureReportPresenter mSeizureReportPresenter;

    private static final int REQUEST_ENABLE_BT = 1;

    private DataCollectorService mDataCollectorService;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataCollectorService = ((DataCollectorService.LocalBinder)service).getService();
            mDevicePairingDetailsPresenter.setDevicePairingService(mDataCollectorService.getDevicePairingService());
            mDevicePairingDetailsPresenter.start();
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataCollectorService = null;
        }
    };
    private Boolean mIsDataCollectorBound = false;

    void doBindService() {

        bindService(new Intent(getApplicationContext(), DataCollectorService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        mIsDataCollectorBound = true;
    }

    void doUnbindService() {
        if (mIsDataCollectorBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsDataCollectorBound = false;
        }
    }

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
        try{
            ((AuraApplication) getApplication()).getRemoteDataTimeSeriesRepository().connect("lecoued", "lecoued");
        }catch(Exception e){
            Log.d(TAG, "Fail initialization InfluxDB");
            e.printStackTrace();
        }

        loadUser();

        Crashlytics.setUserIdentifier(((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());

        mDevicePairingFragment = new DevicePairingDetailsFragment();
        mDevicePairingDetailsPresenter = new DevicePairingDetailsPresenter(( (mDataCollectorService != null) ? mDataCollectorService.getDevicePairingService():null), mDevicePairingFragment);

        mDataSyncFragment = new DataSyncFragment();
        mDataSyncPresenter = new DataSyncPresenter(getApplicationContext(), ((AuraApplication) getApplication()).getDataSyncService(), mDataSyncFragment);

        mPhysioSignalVisualisationFragment = new PhysioSignalVisualisationFragment();
        mDataVisualisationPresenter = new DataVisualisationPresenter(mPhysioSignalVisualisationFragment);

        mSeizureReportFragment = new SeizureReportFragment();
        mSeizureReportPresenter = new SeizureReportPresenter(mSeizureReportFragment, this, ((AuraApplication) getApplication()).getLocalDataRepository(), ((AuraApplication) getApplication()).getUserSessionService());

        mSeizureStatusFragment = new SeizureStatusFragment();
        mSeizureStatusPresenter = new SeizureStatusPresenter(mSeizureStatusFragment, mSeizureReportFragment, this);

        displayFragments();

        ButterKnife.bind(this);

        setupDrawer();
    }

    private void loadUser() {
        SharedPreferences lSharedPref = getSharedPreferences(UserSessionService.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String lUserUUID = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_UUID, null);
        String lUserAmazonId = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_AMAZON_ID, "");
        String lUserAlias = lSharedPref.getString(UserSessionService.SHARED_PREFS_USER_ALIAS,"");

        if(lUserUUID != null) {
            ((AuraApplication) getApplication()).getUserSessionService().setUser(new UserModel(lUserUUID, lUserAmazonId, lUserAlias));
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        //wait the fragment to be fully displayed before starting automatic pairing
       startAutomaticPairing();

        ((AuraApplication) getApplication()).getDataSyncService().initialize();
    }

    @Override
    public void onStop(){
        try {
            ((AuraApplication) getApplication()).getLocalDataRepository().clearCache();

        }
        catch(Exception e){
            Log.d(TAG, "Fail to save cache data on exit");
        }

        ((AuraApplication) getApplication()).getDataSyncService().close();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //disable leaving activity on back button pressed
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
        // no running Aura Data Collector service
        if(!isMyServiceRunning(DataCollectorService.class)){
            BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {

                private Boolean mHasBeenCanceled = false;

                @Override
                public Please onEvent(BluetoothEnablerEvent e) {
                    Log.d(TAG, "Bluetooth Enabler Event - " + e);

                    if(e.status().isCancelled()){
                        mDevicePairingDetailsPresenter.onDevicePairingEvent(new DevicePairingDisconnectedNotification());
                        mHasBeenCanceled = true;
                    }
                    if (e.isDone()) {

                        if(mHasBeenCanceled){
                            mHasBeenCanceled = false;
                        }
                        else{
                            Log.d(TAG, "Bluetooth Enabler Event - " + e.status());

                            Intent startIntent = new Intent(SeizureMonitoringActivity.this, DataCollectorService.class);
                            startIntent.putExtra("UserUUID", ((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());
                            startIntent.setAction(DataCollectorServiceConstants.ACTION.STARTFOREGROUND_ACTION);
                            startService(startIntent);

                            doBindService();
                        }
                    }

                    return super.onEvent(e);
                }
            });
        }
        // running Aura Data Collector service but not binded to Activity
        else if(isMyServiceRunning(DataCollectorService.class) && mDataCollectorService == null){
            doBindService();
        }
        // binded Aura Data Collector service but not paired with device -> restart service
        else if(mDataCollectorService != null && !mDataCollectorService.getDevicePairingService().isPairing() && !mDataCollectorService.getDevicePairingService().isPaired() ) {
            BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {
                @Override
                public Please onEvent(BluetoothEnablerEvent e) {
                    Log.d(TAG, "Bluetooth Enabler Event - " + e);
                    if (e.isDone()) {
                        Intent startIntent = new Intent(SeizureMonitoringActivity.this, DataCollectorService.class);
                        startIntent.putExtra("UserUUID", ((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());
                        startIntent.setAction(DataCollectorServiceConstants.ACTION.STARTFOREGROUND_ACTION);
                        startService(startIntent);

                        doBindService();
                    } else if (e.status().isCancelled()) {

                    }

                    return super.onEvent(e);
                }
            });
        }
    }

    private boolean isMyServiceRunning(Class<?> iServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (iServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void displayFragments(){
        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();

        lTransaction.add(R.id.content_frame, mDevicePairingFragment, DevicePairingDetailsFragment.class.getSimpleName());
        lTransaction.add(R.id.content_frame, mDataSyncFragment , DataSyncFragment.class.getSimpleName());
        lTransaction.add(R.id.content_frame, mSeizureStatusFragment, SeizureStatusFragment.class.getSimpleName());
        lTransaction.add(R.id.content_frame, mPhysioSignalVisualisationFragment, PhysioSignalVisualisationFragment.class.getSimpleName() );
        lTransaction.addToBackStack(null);

        // Commit the transaction
        lTransaction.commit();
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


    @Override
    public void onSeizureReportFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDestroy(){
        doUnbindService();
        super.onDestroy();
    }

}
