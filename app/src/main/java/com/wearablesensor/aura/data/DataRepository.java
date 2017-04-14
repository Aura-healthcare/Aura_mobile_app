package com.wearablesensor.aura.data;

import com.wearablesensor.aura.utils.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by lecoucl on 29/03/17.
 */
@ApplicationScoped
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
