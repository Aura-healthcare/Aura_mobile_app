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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.wearablesensor.aura.data_sync.DataSyncFragment;
import com.wearablesensor.aura.data_sync.DataSyncPresenter;
import com.wearablesensor.aura.data_visualisation.DataVisualisationPresenter;
import com.wearablesensor.aura.data_visualisation.PhysioSignalVisualisationFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsFragment;
import com.wearablesensor.aura.device_pairing_details.DevicePairingDetailsPresenter;
import com.wearablesensor.aura.navigation.NavigationConstants;
import com.wearablesensor.aura.navigation.NavigationNotification;
import com.wearablesensor.aura.navigation.NavigationWithIndexNotification;
import com.wearablesensor.aura.seizure_report.AdditionalInformationConstants;
import com.wearablesensor.aura.seizure_report.SeizureReportFragment;
import com.wearablesensor.aura.seizure_report.SeizureReportPresenter;
import com.wearablesensor.aura.seizure_report.SeizureStatusFragment;
import com.wearablesensor.aura.seizure_report.SeizureStatusPresenter;
import com.wearablesensor.aura.seizure_report.SingleChoice;
import com.wearablesensor.aura.seizure_report.SingleChoiceList;
import com.wearablesensor.aura.seizure_report.SingleChoiceTaskFragment;
import com.wearablesensor.aura.seizure_report.YesNoTaskFragment;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserSessionService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SeizureMonitoringActivity extends AppCompatActivity implements DevicePairingDetailsFragment.OnFragmentInteractionListener, DataSyncFragment.OnFragmentInteractionListener, PhysioSignalVisualisationFragment.OnFragmentInteractionListener, SeizureStatusFragment.OnFragmentInteractionListener, SeizureReportFragment.OnFragmentInteractionListener{

    private final static String TAG = SeizureMonitoringActivity.class.getSimpleName();
    private String[] mDrawerTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.drawer_menu_button) ImageButton mDrawerImageButton;
    @OnClick(R.id.drawer_menu_button)
    public void openDrawerMenu(){
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

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

    private ArrayList<Fragment> mAdditionalInformationFragments;

    private static final int REQUEST_ENABLE_BT = 1;

    private DataCollectorService mDataCollectorService;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataCollectorService = ((DataCollectorService.LocalBinder)service).getService();

            mDevicePairingDetailsPresenter.setDevicePairingService(mDataCollectorService.getDevicePairingService());
            mDevicePairingDetailsPresenter.start();

            mDataSyncPresenter.setDataSyncService(mDataCollectorService.getDataSyncService());
            mDataSyncPresenter.start();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seizure_monitoring);

        loadUser();

        Crashlytics.setUserIdentifier(((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());

        mDevicePairingFragment = new DevicePairingDetailsFragment();
        mDevicePairingDetailsPresenter = new DevicePairingDetailsPresenter(( (mDataCollectorService != null) ? mDataCollectorService.getDevicePairingService():null), mDevicePairingFragment);

        mDataSyncFragment = new DataSyncFragment();
        mDataSyncPresenter = new DataSyncPresenter(getApplicationContext(),( (mDataCollectorService != null) ?  mDataCollectorService.getDataSyncService():null), mDataSyncFragment);

        mPhysioSignalVisualisationFragment = new PhysioSignalVisualisationFragment();
        mDataVisualisationPresenter = new DataVisualisationPresenter(mPhysioSignalVisualisationFragment);

        mSeizureReportFragment = new SeizureReportFragment();
        mSeizureReportPresenter = new SeizureReportPresenter(mSeizureReportFragment, ((AuraApplication) getApplication()).getLocalDataRepository(), ((AuraApplication) getApplication()).getUserSessionService());

        mSeizureStatusFragment = new SeizureStatusFragment();
        mSeizureStatusPresenter = new SeizureStatusPresenter(mSeizureStatusFragment);

        createAdditionalInformationFragments();
        displayFragments();

        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {

        /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
           }

            /** Called when a drawer has settled in a completely open state. */

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView.setCheckedItem(R.id.nav_SuiviContinu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_SuiviContinu:
                        break;
                    case R.id.nav_LeaveApp:
                        quitApplication();
                        break;
                }
                return true;
            }

        });
        //wait the fragment to be fully displayed before starting automatic pairing
        startDataCollector();
    }

    /**
     * @brief create a list of fragment that will map a succession of questions for the patient
     */
    private void createAdditionalInformationFragments() {
        mAdditionalInformationFragments = new ArrayList<>();

        SingleChoiceList lTreatmentObservanceOptions = new SingleChoiceList();
        lTreatmentObservanceOptions.addChoice(new SingleChoice(getString(R.string.additional_question_treatment_observance_answer_yes), "", R.drawable.treatment_ok_selector, AdditionalInformationConstants.TreatmentObservanceOkOption));
        lTreatmentObservanceOptions.addChoice(new SingleChoice(getString(R.string.additional_question_treatment_observance_answer_low),"", R.drawable.treatment_low_selector, AdditionalInformationConstants.TreatmentObservanceMissingSomeOption));
        lTreatmentObservanceOptions.addChoice(new SingleChoice(getString(R.string.additional_question_treatment_observance_answer_no), "", R.drawable.treatment_none_selector, AdditionalInformationConstants.TreatmentObservanceNeverOption));

        mAdditionalInformationFragments.add(SingleChoiceTaskFragment.newInstance(getString(R.string.additional_question_treatment_observance), AdditionalInformationConstants.TreatmentObservanceValue, lTreatmentObservanceOptions, 1));

        SingleChoiceList lSleepQualityOptions = new SingleChoiceList();
        lSleepQualityOptions.addChoice(new SingleChoice(getString(R.string.additional_question_sleep_night_answer_well), "", R.drawable.sleep_well_selector, AdditionalInformationConstants.QualityOfSleepWellOption));
        lSleepQualityOptions.addChoice(new SingleChoice(getString(R.string.additional_question_sleep_night_answer_low),"", R.drawable.wake_up_selector, AdditionalInformationConstants.QualityOfSleepFewWakeUpsOption));
        lSleepQualityOptions.addChoice(new SingleChoice(getString(R.string.additional_question_sleep_night_answer_no), "", R.drawable.insomnia_selector, AdditionalInformationConstants.QualityOfSleepInsomniaOption));
        mAdditionalInformationFragments.add(SingleChoiceTaskFragment.newInstance(getString(R.string.additional_question_sleep_night), AdditionalInformationConstants.QualityOfSleepValue,lSleepQualityOptions, 2));

        mAdditionalInformationFragments.add(YesNoTaskFragment.newInstance(getString(R.string.additional_question_high_stress_episode), AdditionalInformationConstants.HighStressEpisodeValue, 3));
        mAdditionalInformationFragments.add(YesNoTaskFragment.newInstance(getString(R.string.additional_question_fever), AdditionalInformationConstants.HavingFeverValue, 4));

        SingleChoiceList lAlcoholComsumptionOptions = new SingleChoiceList();
        lAlcoholComsumptionOptions.addChoice(new SingleChoice(getString(R.string.additional_question_alcohol_answer_none), "", R.drawable.no_drink_selector, AdditionalInformationConstants.AlcoholConsumptionNoOption));
        lAlcoholComsumptionOptions.addChoice(new SingleChoice(getString(R.string.additional_question_alcohol_answer_low),"", R.drawable.drink_selector, AdditionalInformationConstants.AlcoholConsumptionFewDrinksOption));
        lAlcoholComsumptionOptions.addChoice(new SingleChoice(getString(R.string.additional_question_alcohol_answer_high), "", R.drawable.drink_high_selector, AdditionalInformationConstants.AlcoholConsumptionHighOption));

        mAdditionalInformationFragments.add(SingleChoiceTaskFragment.newInstance(getString(R.string.additional_question_alcohol_consumption), AdditionalInformationConstants.AlcoholConsumptionValue,lAlcoholComsumptionOptions, 5));
        mAdditionalInformationFragments.add(YesNoTaskFragment.newInstance(getString(R.string.additional_question_new_treatment), AdditionalInformationConstants.NewTreatmentValue, 6));
        mAdditionalInformationFragments.add(YesNoTaskFragment.newInstance(getString(R.string.additional_question_late_sleep), AdditionalInformationConstants.LateSleepValue, 7));
    }

    private void quitApplication() {
        stopDataCollector();

        finish();
    }

    private void stopDataCollector() {
        doUnbindService();

        Intent stopIntent = new Intent(SeizureMonitoringActivity.this, DataCollectorService.class);
        stopIntent.setAction(DataCollectorServiceConstants.ACTION.STOPFOREGROUND_ACTION);
        stopService(stopIntent);
    }

    private void startDataCollector(){
        // no running Aura Data Collector service
        if(!isMyServiceRunning(DataCollectorService.class)){
            Intent startIntent = new Intent(SeizureMonitoringActivity.this, DataCollectorService.class);
            startIntent.putExtra("UserUUID", ((AuraApplication) getApplication()).getUserSessionService().getUser().getUuid());
            startIntent.setAction(DataCollectorServiceConstants.ACTION.STARTFOREGROUND_ACTION);
            startService(startIntent);

            doBindService();
        }
        // running Aura Data Collector service but not binded to Activity
        else if(isMyServiceRunning(DataCollectorService.class) && mDataCollectorService == null){
            doBindService();
        }
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

    }

    @Override
    public void onStop(){
        try {
            ((AuraApplication) getApplication()).getLocalDataRepository().forceSavingPhysioSignalSamples();

        }
        catch(Exception e){
            Log.d(TAG, "Fail to save cache data on exit");
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //disable leaving activity on back button pressed
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
        lTransaction.add(R.id.content_frame, mPhysioSignalVisualisationFragment, PhysioSignalVisualisationFragment.class.getSimpleName() );
        lTransaction.add(R.id.content_frame, mDataSyncFragment , DataSyncFragment.class.getSimpleName());
        lTransaction.add(R.id.content_frame, mSeizureStatusFragment, SeizureStatusFragment.class.getSimpleName());
        lTransaction.addToBackStack(null);

        // Commit the transaction
        lTransaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    //TODO: replace by an independent Navigation component
    /**
     * @brief method to handle navigation between additional question fragments
     * @param iQuestionIndex
     */
    public void goToAdditionnalQuestions(int iQuestionIndex){
        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();
        Fragment lFragment;

        if(iQuestionIndex >= mAdditionalInformationFragments.size()){
           lFragment = mSeizureReportFragment;
           Toast.makeText(this, getString(R.string.additional_questions_completed), Toast.LENGTH_SHORT).show();
        }
        else{
            lFragment = mAdditionalInformationFragments.get(iQuestionIndex);
        }

        lTransaction.replace(R.id.content_frame, lFragment);

        lTransaction.addToBackStack(null);

        // Commit the transaction
        lTransaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNavigationEvent(NavigationNotification iNavigationEvent){

        switch (iNavigationEvent.getNavigationFlag()){
            case NavigationConstants.NAVIGATION_SEIZURE_MONITORING:
                goToSeizureMonitoring();
                break;
            case NavigationConstants.NAVIGATION_SEIZURE_REPORTING:
                goToSeizureReporting();
                break;
            case NavigationConstants.NAVIGATION_SEIZURE_NEXT_QUESTION:
                NavigationWithIndexNotification iNavigationEventWithIndex = (NavigationWithIndexNotification) iNavigationEvent;
                goToAdditionnalQuestions(iNavigationEventWithIndex.getIndex());
                break;
            case NavigationConstants.NAVIGATION_DEVICE_SCANNING:
                goToDeviceScanning();
                break;
            default:
        }
    }

    private void goToDeviceScanning() {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);

        this.finish();
    }

    @SuppressLint("RestrictedApi")
    private void goToSeizureMonitoring() {
        android.support.v4.app.FragmentManager lFragmentManager = getSupportFragmentManager();
        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();

        for(Fragment lFragment: lFragmentManager.getFragments()){
            if (lFragment != null) {
                lTransaction.remove(lFragment);
            }
        }

        lTransaction.add(R.id.content_frame, lFragmentManager.findFragmentByTag(DevicePairingDetailsFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, lFragmentManager.findFragmentByTag(PhysioSignalVisualisationFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, lFragmentManager.findFragmentByTag(DataSyncFragment.class.getSimpleName()));
        lTransaction.add(R.id.content_frame, lFragmentManager.findFragmentByTag(SeizureStatusFragment.class.getSimpleName()));
        lTransaction.addToBackStack(null);

        lTransaction.commit();
    }

    @SuppressLint("RestrictedApi")
    private void goToSeizureReporting() {
        android.support.v4.app.FragmentManager lFragmentManager = getSupportFragmentManager();
        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();

        for(Fragment lFragment: lFragmentManager.getFragments()){
            if (lFragment != null) {
                lTransaction.remove(lFragment);
            }
        }

        lTransaction.add(R.id.content_frame, mSeizureReportFragment, SeizureReportFragment.class.getSimpleName());
        lTransaction.addToBackStack(null);

        lTransaction.commit();
    }
}
