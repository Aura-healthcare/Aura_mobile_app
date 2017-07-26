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
 *  1) connect to InfluxDB database
 * Currently secured connection to database is done using basic user/password credentials.
 *  2) 3 .. N) query or save data in database
 */


package com.wearablesensor.aura.data_repository;


import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteDataInfluxDBRepository implements RemoteDataRepository.TimeSeries {

    private final String TAG = this.getClass().getSimpleName();

    public final static String INFLUX_DB_URL = "http://192.168.1.48:8086";
    private final static String INFLUX_DB_PHYSIO_SIGNAL_NAME = "physio_signal";
    private final static String INFLUX_DB_NAME_SENSITIVE_EVENT_NAME = "sensitive_event";

    private final static String DB_HEART_MEASUREMENT = "heart";
    private final static String DB_USER_EVENT_MEASUREMENT = "user_event";

    private final static String DB_USER_TAG = "user";
    private final static String DB_UUID_TAG = "uuid";
    private final static String DB_TYPE_TAG = "type";
    private final static String DB_DEVICE_ADDRESS_TAG = "device_address";
    private final static String DB_COMMENTS_TAG = "comments";
    private final static String DB_RR_INTERVAL_TAG = "rr_interval";
    private final static String DB_SENSITIVE_EVENT_TIMESTAMP_TAG = "sensitive_event_timestamp";

    private InfluxDB mInfluxDB;

    @Override
    /**
     * @brief initialize connection between remote database and Aura application
     *
     * @param iUser username credential
     * @param iPassword password credential
     * @throws Exception
     */
    public void connect(String iUser, String iPassword) throws Exception {
        mInfluxDB = InfluxDBFactory.connect(INFLUX_DB_URL, iUser, iPassword);
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
            Point lPoint = Point.measurement(DB_HEART_MEASUREMENT)
                    .time(DateIso8601Mapper.getDate(lSample.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                    .tag(DB_USER_TAG, lSample.getUser())
                    .tag(DB_DEVICE_ADDRESS_TAG, lSample.getDeviceAdress())
                    .tag(DB_UUID_TAG, lSample.getUuid())
                    .tag(DB_TYPE_TAG, lSample.getType())
                    .addField(DB_RR_INTERVAL_TAG, lSample.getRrInterval())
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
            Point lPoint = Point.measurement(DB_USER_EVENT_MEASUREMENT)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag(DB_UUID_TAG, lSeizureEvent.getUuid())
                    .tag(DB_USER_TAG, lSeizureEvent.getUser())
                    .tag(DB_TYPE_TAG, lSeizureEvent.getType())
                    .addField(DB_COMMENTS_TAG, lSeizureEvent.getComments())
                    .addField(DB_SENSITIVE_EVENT_TIMESTAMP_TAG, DateIso8601Mapper.getDate(lSeizureEvent.getSensitiveEventTimestamp()).getTime())
                    .build();

            lBatchPoints.point(lPoint);
        }

        mInfluxDB.write(lBatchPoints);
    }
}
