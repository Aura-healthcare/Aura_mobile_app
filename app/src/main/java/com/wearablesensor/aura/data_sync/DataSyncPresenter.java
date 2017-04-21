package com.wearablesensor.aura.data_sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wearablesensor.aura.data_repository.LocalDataRepository;
import com.wearablesensor.aura.data_repository.RemoteDataRepository;
import com.wearablesensor.aura.data_repository.SampleRRInterval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by lecoucl on 14/04/17.
 */
public class DataSyncPresenter implements DataSyncContract.Presenter {

    private final LocalDataRepository mLocalDataRepository;
    private final RemoteDataRepository mRemoteDataRepository;

    private final DataSyncContract.View mView;

    private final Context mApplicationContext;

    public DataSyncPresenter(LocalDataRepository iLocalDataRepository,
                             RemoteDataRepository iRemoteDataRepository,
                             DataSyncContract.View iView,
                             Context iApplicationContext) {
        mLocalDataRepository = iLocalDataRepository;
        mRemoteDataRepository = iRemoteDataRepository;
        mView = iView;
        mApplicationContext = iApplicationContext;
        Log.d("DataSyncPresenter","constructor" );

        mView.setPresenter(this);
    }

    @Override
    public void start() {
        Date lLastSync = getLastSync();
        mView.refreshLastSync(lLastSync);
    }

    @Override
    public void pushData() {
        new PushDataOnRemoteAsync().execute();
    }

    private void saveLastSync(Date iLastSync) throws Exception {
        try {
            mLocalDataRepository.saveLastSync(iLastSync);
            mRemoteDataRepository.saveLastSync(iLastSync);
        }catch(Exception e){
            throw e;
        }
    }

    private Date getLastSync(){
        Date lLastSync = null;
        // query last sync value from cache
        try {
            lLastSync = mLocalDataRepository.queryLastSync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // query last sync value from remote repository
        if(lLastSync == null){
            try {
                lLastSync = mRemoteDataRepository.queryLastSync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lLastSync;
    }


    // TODO: we should implement a Loader and cache/remote data sync logic
    class PushDataOnRemoteAsync extends AsyncTask<Void, Integer, Boolean> {
        final private String TAG = PushDataOnRemoteAsync.class.getSimpleName();
        private Date mCurrentSync;
        private String mFailMessage;

        protected void onPreExecute() {
            Log.d(TAG, "OnPreExecute");
            mCurrentSync = null;
            mFailMessage = "";
            mView.startPushDataOnCloud();
        }

        protected Boolean doInBackground(Void... arg0) {
            Log.d(TAG, "doInBackground");

            try {
                publishProgress(1);
                Calendar c = Calendar.getInstance();
                mCurrentSync = c.getTime();

                Date lLastSync = getLastSync();
                publishProgress(10);
                final ArrayList<SampleRRInterval> rrSamples = mLocalDataRepository.queryRRSample(lLastSync, mCurrentSync);
                publishProgress(30);
                mRemoteDataRepository.saveRRSample(rrSamples);
                publishProgress(90);

                saveLastSync(mCurrentSync);
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Fail to save RR data");
                mFailMessage = e.toString();
                return false;
            }
        }


        protected void onProgressUpdate(Integer...a){
            Log.d(TAG , "onProgressUpdate");
            mView.refreshProgressPushDataOnCloud(a[0]);
        }


        protected void onPostExecute(Boolean iSuccess) {
            Log.d(TAG , "onPostExecute");
            if(iSuccess) {
                mView.refreshLastSync(mCurrentSync);
            }
            else{
                mView.displayFailMessageOnPushData(mApplicationContext, mFailMessage);
            }

            mView.endPushDataOnCloud();
        }

    }
}