package com.wearablesensor.aura.data_repository;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.wearablesensor.aura.SeizureMonitoringActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by lecoucl on 05/01/17.
 */
public class DataManager {

    private RemoteDataManager mRemoteDataManager;
    private LocalDataManager mLocalDataManager;
    private SeizureMonitoringActivity mActivity;

    private static DataManager sInstance = new DataManager();

    public void init(Context iApplicationContext, Activity iActivity, String iUser) {
        mRemoteDataManager.init(iApplicationContext, iUser);
        mLocalDataManager.init(iApplicationContext);

        mActivity = (SeizureMonitoringActivity) iActivity;
    }

    public void initializeUserPrefs() {
        new InitUserPrefsAsync().execute();
        new InitHRVRealTimeDataAsync().execute();
    }

   /* public void pushDataOnRemote(){
        new PushDataOnRemoteAsync().execute();
    }*/

    public void saveRRSample(SampleRRInterval iSampleRR){
        // filter empty values
        if (iSampleRR.getTimestamp() == "" && iSampleRR.getRR() == 0) {
            return;
        }

        mLocalDataManager.saveRRData(iSampleRR);
    }

    public static synchronized DataManager getInstance() {
        return sInstance;
    }

    private DataManager() {
        mRemoteDataManager = new RemoteDataManager();
        mLocalDataManager = new LocalDataManager();
    }

    protected Date getLastSync() {
        Date lLastSync = mLocalDataManager.getLastSync();

        if(lLastSync == null){
            lLastSync = mRemoteDataManager.getLastSync();
        }

        return lLastSync;
    }

    protected void saveLastSync(Date iDate) throws CouchbaseLiteException {
        try {
            mLocalDataManager.saveLastSync(iDate);
            mRemoteDataManager.saveLastSync(iDate);
        }catch(Exception e){
            throw e;
        }
    }

    public void cleanLocalCache() {
        mLocalDataManager.clean();
    }


    /*class PushDataOnRemoteAsync extends AsyncTask<Void, Integer, Boolean>
    {
        final private String TAG = PushDataOnRemoteAsync.class.getSimpleName();
        private Date mCurrentSync;
        private String mFailMessage;

        protected void onPreExecute (){
            Log.d(TAG,"OnPreExecute");
            mCurrentSync = null;
            mFailMessage = "";
            mView.startPushDataOnRemote();
        }

        protected Boolean doInBackground(Void...arg0) {
            Log.d(TAG,"doInBackground");

            try {
                publishProgress(1);

                Calendar c = Calendar.getInstance();
                mCurrentSync = c.getTime();
                Date lLastSync = getLastSync();
                publishProgress(10);
                final ArrayList<SampleRRInterval> rrSamples = mLocalDataManager.getRRData(lLastSync, mCurrentSync);
                publishProgress(30);
                mRemoteDataManager.saveRRBatch(rrSamples);
                publishProgress(90);

                saveLastSync(mCurrentSync);
                return true;
            }
            catch(Exception e){
                Log.d(TAG,"Fail to save RR data");
                mFailMessage = e.toString();
                return false;
            }
        }

        protected void onProgressUpdate(Integer...a){
            Log.d(TAG , "onProgressUpdate");
            mActivity.progressPushDataOnRemote(a[0]);
        }


        protected void onPostExecute(Boolean iSuccess) {
            Log.d(TAG , "onPostExecute");
            if(iSuccess) {
                mActivity.successPushDataOnRemote(mCurrentSync);
            }
            else{
                mActivity.failPushDataOnRemote(mFailMessage);
            }
        }
    }*/

    class InitUserPrefsAsync extends AsyncTask<Void, Integer, Date>
    {
        final private String TAG = InitUserPrefsAsync.class.getSimpleName();
        //TODO replace by full extend of UserPrefs

        protected void onPreExecute (){
             Log.d(TAG,"OnPreExecute");
        }

        protected Date doInBackground(Void...arg0) {
            Log.d(TAG, "doInBackground");

            //TODO replace by full extend of UserPrefs
            return getLastSync();
        }

        protected void onProgressUpdate(Integer...a){
            Log.d(TAG , "onProgressUpdate");

        }

        protected void onPostExecute(Date iUserPrefs) {
            Log.d(TAG , "onPostExecute");
            mActivity.displayUserPrefs(iUserPrefs);
        }
    }

    class InitHRVRealTimeDataAsync extends AsyncTask<Void, Void, Boolean>
    {
        final private String TAG = InitHRVRealTimeDataAsync.class.getSimpleName();
        private Date mWindowStart;
        private Date mWindowEnd;

        private ArrayList<SampleRRInterval> mRrSamples;
        final private int HRV_WINDOW_WIDTH_IN_MIN = 60;
        final private int HRV_WINDOW_SHIFT_IN_MIN = 5;

        protected void onPreExecute (){
            Log.d(TAG,"OnPreExecute");
            Calendar c = Calendar.getInstance();
            Date lCurrentTime = c.getTime();
            c.setTime(lCurrentTime);
            c.add(Calendar.MINUTE, HRV_WINDOW_SHIFT_IN_MIN);
            mWindowEnd = c.getTime();

            c.add(Calendar.MINUTE, - HRV_WINDOW_SHIFT_IN_MIN - HRV_WINDOW_WIDTH_IN_MIN );
            mWindowStart = c.getTime();
            Log.d(TAG, "DateWindow - " + DateIso8601Mapper.getString(mWindowStart) + " - " + DateIso8601Mapper.getString(mWindowEnd) );
        }

        protected Boolean doInBackground(Void...arg0) {
            Log.d(TAG, "doInBackground");

            try{
                mRrSamples = mLocalDataManager.getRRData(mWindowStart, mWindowEnd);
                return true;
            }
            catch (Exception e){
                return false;
            }
        }

        protected void onProgressUpdate(Integer...a){
            Log.d(TAG , "onProgressUpdate");

        }

        protected void onPostExecute(Boolean iSuccess) {
            Log.d(TAG , "onPostExecute");
            if(iSuccess){
              //mActivity.enableHRVRealTimeDisplay(mRrSamples, mWindowStart, mWindowEnd);
              mRrSamples.clear();
            }
        }
    }
}
