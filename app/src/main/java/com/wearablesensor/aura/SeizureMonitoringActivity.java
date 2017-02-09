package com.wearablesensor.aura;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.wearablesensor.aura.bluetooth.BluetoothLeService;
import com.wearablesensor.aura.data.DataManager;
import com.wearablesensor.aura.data.SampleRRInterval;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SeizureMonitoringActivity extends AppCompatActivity implements DevicePairingFragment.OnFragmentInteractionListener, DataSyncFragment.OnFragmentInteractionListener, HRVRealTimeDisplayFragment.OnFragmentInteractionListener{

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

    private DevicePairingFragment mDevicePairingFragment;
    private DataSyncFragment mDataSyncFragment;
    private HRVRealTimeDisplayFragment mHrvRealTimeDisplayFragment;

    private SeizureMonitoringActivity.LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                failAutomaticPairing();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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
        mDevicePairingFragment = (DevicePairingFragment) getSupportFragmentManager().findFragmentById(R.id.device_pairing_fragment);
        mDataSyncFragment = (DataSyncFragment) getSupportFragmentManager().findFragmentById(R.id.data_sync_fragment);
        mHrvRealTimeDisplayFragment = (HRVRealTimeDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.hrv_realtime_display_fragment);
        ButterKnife.bind(this);

        setupDrawer();
        startAutomaticPairing();

        // initialize DataManager
        DataManager.getInstance().init(getApplicationContext(), this, "me");

        initializeUserPrefs();

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

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            failAutomaticPairing();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            failAutomaticPairing();
            return;
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new SeizureMonitoringActivity.LeDeviceListAdapter();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                scanLeDevice(true);
            }
        }
        else{
            scanLeDevice(true);
        }
    }

    private void successAutomaticPairing(String iDeviceName, String iDeviceAdress){
        mDevicePairingFragment.displaySuccessPairing(iDeviceName, iDeviceAdress);
        mHrvRealTimeDisplayFragment.enableHRVRealTime();
    }

    private void failAutomaticPairing(){
        final String failMessage = getString(R.string.automatic_pairing_fail);
        Toast.makeText(getApplicationContext(), failMessage, Toast.LENGTH_LONG).show();
        mDevicePairingFragment.displayFailPairing();
        mHrvRealTimeDisplayFragment.disableHRVRealTime();
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
                scanLeDevice(true);
                return;
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                failAutomaticPairing();
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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    if(mLeDeviceListAdapter.getCount()> 0) {
                        BluetoothDevice lDevice = mLeDeviceListAdapter.getDevice(0);
                        mDeviceName = lDevice.getName();
                        mDeviceAddress = lDevice.getAddress();

                        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                        Intent gattServiceIntent = new Intent(SeizureMonitoringActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    }
                    else{
                        failAutomaticPairing();
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                successAutomaticPairing(mDeviceName, mDeviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                failAutomaticPairing();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                BluetoothGattCharacteristic lBleHeartRateCharacteristic = mBluetoothLeService.getBluetoothGattHeartRateCharacteristic();
                mBluetoothLeService.setCharacteristicNotification(lBleHeartRateCharacteristic, true);
                // Show all the supported services and characteristics on the user interface.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "Bluetooth Service - Data Available");
                String uuid = intent.getStringExtra(BluetoothLeService.ID_EXTRA_DATA);
                String user = intent.getStringExtra(BluetoothLeService.USER_EXTRA_DATA);
                String timestamp = intent.getStringExtra(BluetoothLeService.TIMESTAMP_EXTRA_DATA);
                Integer rr = intent.getIntExtra(BluetoothLeService.RR_EXTRA_DATA, 0);

                SampleRRInterval lSampleRRInterval = new SampleRRInterval(user, mDeviceAddress, timestamp, rr);
                DataManager.getInstance().saveRRSample(lSampleRRInterval);
                mHrvRealTimeDisplayFragment.addNewHRVData(lSampleRRInterval);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            //mInflator = SeizureMonitoringActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            /*DeviceScanActivity.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new DeviceScanActivity.ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (DeviceScanActivity.ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());*/

            return view;
        }
    }
}
