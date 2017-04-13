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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.github.clans.fab.FloatingActionButton;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.data.DataManager;
import com.wearablesensor.aura.data.SampleRRInterval;
import com.wearablesensor.aura.device_pairing_details.DaggerDevicePairingDetailsComponent;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsPresenter;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsPresenterModule;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SeizureMonitoringActivity extends AppCompatActivity implements DevicePairingDetailsFragment.OnFragmentInteractionListener, DataSyncFragment.OnFragmentInteractionListener, HRVRealTimeDisplayFragment.OnFragmentInteractionListener{

    private final static String TAG = SeizureMonitoringActivity.class.getSimpleName();
    private String[] mDrawerTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.left_drawer) ListView mDrawerList;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    @BindView(R.id.action_menu_manual_pairing) FloatingActionButton mManualPairingButton;
    @OnClick (R.id.action_menu_manual_pairing)
    public void actionMenuManualPairingCallback(View v){ DataManager.getInstance().cleanLocalCache();}
    @BindView(R.id.action_menu_push_data) FloatingActionButton mPushDataButton;
    @OnClick (R.id.action_menu_push_data)
    public void actionMenuPushBackCallback(View v) {
        DataManager.getInstance().pushDataOnRemote();
    }

    private BluetoothDevicePairingService mDevicePairingService;

    @Inject
    DevicePairingDetailsPresenter mDevicePairingDetailsPresenter;
    private DevicePairingDetailsFragment mDevicePairingFragment;
    private DataSyncFragment mDataSyncFragment;
    private HRVRealTimeDisplayFragment mHrvRealTimeDisplayFragment;

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
        mDevicePairingFragment = (DevicePairingDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.device_pairing_details_fragment);

        // Create the presenter
        DaggerDevicePairingDetailsComponent.builder()
                .devicePairingComponent( ((AuraApplication) getApplication()).getDevicePairingComponent() )
                .devicePairingDetailsPresenterModule(new DevicePairingDetailsPresenterModule(mDevicePairingFragment))
                .build()
                .inject(this);


        mDataSyncFragment = (DataSyncFragment) getSupportFragmentManager().findFragmentById(R.id.data_sync_fragment);
        mHrvRealTimeDisplayFragment = (HRVRealTimeDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.hrv_realtime_display_fragment);
        ButterKnife.bind(this);

        setupDrawer();

        startAutomaticPairing();

        //initializeUserProfile();

        //initializeUserPrefs();

    }

    private ProfileTracker mProfileTracker;
    private AccessTokenTracker mAccesTokenTracker;
    private void initializeUserProfileFromFacebook(){


        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                    //TODO update profile display
                    mProfileTracker.stopTracking();
                }
            };
        }
        else {
            Profile lProfile = Profile.getCurrentProfile();
            //TODO update profile display
        }

        if(AccessToken.getCurrentAccessToken() == null){
            mAccesTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    // initialize DataManager only after we receive credentials from Identity provider
                    initializeDataManager();
                    mAccesTokenTracker.stopTracking();
                }
            };

        }
        else{
            initializeDataManager();
        }

    }

    private void initializeUserProfile() {
        initializeUserProfileFromFacebook();
    }

    private void initializeDataManager(){
        DataManager.getInstance().init(getApplicationContext(), this, "me");
    }

    private void initializeUserPrefs() {
        DataManager.getInstance().initializeUserPrefs();
    }

    //TODO extend to all user Prefs
    public void displayUserPrefs(Date iLastSync) {
        mDataSyncFragment.updateLastSyncDisplay(iLastSync);
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
        mDevicePairingService =  ( (AuraApplication) getApplication()).getDevicePairingComponent().devicePairingService();
        if(mDevicePairingService.checkBluetoothIsEnabled()){
            mDevicePairingService.automaticPairing();
        }
        else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void startPushDataOnRemote(){
        mDataSyncFragment.displayStartPushData();
    }

    public void progressPushDataOnRemote(Integer iProgress){
        mDataSyncFragment.displayProgressPushData(iProgress);
    }

    public void successPushDataOnRemote(Date iCurrentSync){
        mDataSyncFragment.displaySuccessPushData(iCurrentSync);
    }

    public void failPushDataOnRemote(String iFailMessage){
        String lFailMessage = getString(R.string.push_data_fail) + " : " + iFailMessage;
        Toast.makeText(getApplicationContext(), lFailMessage, Toast.LENGTH_LONG).show();

        mDataSyncFragment.displayFailPushData();
    }

    public void enableHRVRealTimeDisplay(ArrayList<SampleRRInterval> mRrSamples, Date iWindowStart, Date iWindowEnd){
        mHrvRealTimeDisplayFragment.initHRVRealTimeData(mRrSamples);
        mHrvRealTimeDisplayFragment.displayHRVRealTimeData(iWindowStart, iWindowEnd);
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
                //scanLeDevice(true);
                mDevicePairingService.automaticPairing();
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
    public void onDevicePairingFragmentInteraction(Uri uri) {

    }

    @Override
    public void onHRVRealTimeDisplayFragmentInteraction(Uri uri) {

    }
}
