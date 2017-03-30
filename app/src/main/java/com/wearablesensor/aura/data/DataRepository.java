package com.wearablesensor.aura.data;

import android.provider.ContactsContract;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by lecoucl on 29/03/17.
 */
@Singleton
public class DataRepository {
    private LocalDataRepository mLocalDataRepository;
    private RemoteDataRepository mRemoteDataRepository;

    @Inject
    public DataRepository(LocalDataRepository iLocalDataRepository,
                          RemoteDataRepository iRemoteDataRepository){
        mLocalDataRepository = iLocalDataRepository;
        mRemoteDataRepository = iRemoteDataRepository;
    }
}
