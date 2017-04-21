package com.wearablesensor.aura.data_repository;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lecoucl on 29/03/17.
 */

public interface LocalDataRepository {

    ArrayList<SampleRRInterval> queryRRSample(Date iStartDate, Date iEndDate) throws Exception;
    void saveRRSample(final SampleRRInterval iSampleRR) throws Exception;

    Date queryLastSync() throws Exception;
    void saveLastSync(final Date iLastSync) throws Exception;

}
