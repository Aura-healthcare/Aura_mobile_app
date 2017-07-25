/**
 * @file RemoteDataInfluxDBRepository.java
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 * RemoteDataInfluxDBRepository is a remote data storage specialized in time series data storage
 *
 * We consider a two-step initialization:
 *  1) connect to DynamoDB database
 * Currently secured connection to database is done using basic user/password credentials.
 *  2) 3 .. N) query or save data in database
 */


package com.wearablesensor.aura.data_repository;

import android.util.Log;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.user_session.UserModel;
import com.wearablesensor.aura.user_session.UserPreferencesModel;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteDataInfluxDBRepository implements RemoteDataRepository {

    private final String TAG = this.getClass().getSimpleName();

    public final static String INFLUX_DB_URL = "http://192.168.8.100:8086";
    private final static String INFLUX_DB_PHYSIO_SIGNAL_NAME = "physio_signal";
    private final static String INFLUX_DB_NAME_SENSITIVE_EVENT_NAME = "sensitive_event";

    private InfluxDB mInfluxDB;

    @Override
    /**
     * @brief initialize connection between remote database and Aura application
     *
     * @param lAuthToken no needed
     * @throws Exception
     */
    public void connect(String lAuthToken) throws Exception {
        mInfluxDB = InfluxDBFactory.connect(INFLUX_DB_URL,"lecoued", "lecoued");
    }

    /**
     * @brief save a list of R-R interval samples
     *
     * @param iRrSamples list of R-R interval samples to be saved
     *
     * @throws Exception
     */
    @Override
    public void saveRRSample(ArrayList<RRIntervalModel> iRrSamples) throws Exception {
        BatchPoints lBatchPoints = BatchPoints
                .database(INFLUX_DB_PHYSIO_SIGNAL_NAME)
                .build();

        for (RRIntervalModel lSample : iRrSamples) {
            Point lPoint = Point.measurement("physio")
                    .time(DateIso8601Mapper.getDate(lSample.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                    .tag("user", lSample.getUser())
                    .tag("device_address", lSample.getDeviceAdress())
                    .tag("uuid", lSample.getUuid())
                    .tag("type", lSample.getType())
                    .addField("rr_interval", lSample.getRrInterval())
                    .build();

            lBatchPoints.point(lPoint);
        }

        mInfluxDB.write(lBatchPoints);
    }

    /**
     * @brief save a list of seizure event samples
     *
     * @param iSensitiveEvents list of seizure event samples
     *
     * @throws Exception
     */

    @Override
    public void saveSeizures(ArrayList<SeizureEventModel> iSensitiveEvents) throws Exception {

        BatchPoints lBatchPoints = BatchPoints
                .database(INFLUX_DB_NAME_SENSITIVE_EVENT_NAME)
                .build();

        for (SeizureEventModel lSeizureEvent : iSensitiveEvents) {
            Point lPoint = Point.measurement("event")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("uuid", lSeizureEvent.getUuid())
                    .tag("user", lSeizureEvent.getUser())
                    .tag("type", lSeizureEvent.getType())
                    .addField("comments", lSeizureEvent.getComments())
                    .addField("sensitive_timestamp", DateIso8601Mapper.getDate(lSeizureEvent.getSensitiveEventTimestamp()).getTime())
                    .build();

            lBatchPoints.point(lPoint);
        }

        mInfluxDB.write(lBatchPoints);
    }

    //TODO: to be implemented
    @Override
    public UserModel queryUser(String iAmazonId) throws Exception {
        return null;
    }

    //TODO: to be implemented
    @Override
    public void saveUser(UserModel iUserModel) throws Exception {

    }

    //TODO: to be implemented
    @Override
    public UserPreferencesModel queryUserPreferences(String iUserId) throws Exception {
        return null;
    }

    //TODO: to be implemented
    @Override
    public void saveUserPreferences(UserPreferencesModel iUserPreferencesModel) throws Exception {

    }
}
