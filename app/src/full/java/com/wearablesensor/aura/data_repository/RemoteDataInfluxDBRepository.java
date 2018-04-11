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


import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.MotionMagnetometerModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SeizureEventModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteDataInfluxDBRepository implements RemoteDataRepository.TimeSeries {

    private final String TAG = this.getClass().getSimpleName();

    public final static String INFLUX_DB_URL = "https://db.aura.healthcare";
    private final static String INFLUX_DB_PHYSIO_SIGNAL_NAME = "physio_signal";
    private final static String INFLUX_DB_NAME_SENSITIVE_EVENT_NAME = "sensitive_event";

    private final static String DB_HEART_MEASUREMENT = "heart";
    private final static String DB_TEMPERATURE_MEASUREMENT = "temperature";
    private final static String DB_ELECTRO_DERMAL_ACTIVITY_MEASUREMENT = "electro_dermal_activity";
    private final static String DB_ACCELEROMETER_MEASUREMENT = "accelerometer";
    private final static String DB_GYROSCOPE_MEASUREMENT = "gyroscope";


    private final static String DB_USER_EVENT_MEASUREMENT = "user_event";

    private final static String DB_USER_TAG = "user";
    private final static String DB_UUID_TAG = "uuid";
    private final static String DB_TYPE_TAG = "type";
    private final static String DB_DEVICE_ADDRESS_TAG = "device_address";
    private final static String DB_INTENSITY_TAG = "intensity";
    private final static String DB_RR_INTERVAL_TAG = "rr_interval";
    private final static String DB_SKIN_TEMPERATURE = "skin_temperature";
    private final static String DB_SENSOR_OUTPUT_FREQUENCY = "sensor_output_frequency";
    private final static String DB_ELECTRO_DERMAL_ACTIVITY = "electro_dermal_activity";
    private final static String DB_SENSITIVE_EVENT_TIMESTAMP_TAG = "sensitive_event_timestamp";
    private final static String DB_X_TAG = "x";
    private final static String DB_Y_TAG = "y";
    private final static String DB_Z_TAG = "z";

    private InfluxDB mInfluxDB;

    /**
     * @brief initialize connection between remote database and Aura application
     *
     * @param iDatabaseURL database end point
     * @param iUser username credential
     * @param iPassword password credential
     * @throws Exception
     */
    @Override
    public void connect(String iDatabaseURL, String iUser, String iPassword) throws Exception {
        mInfluxDB = InfluxDBFactory.connect(iDatabaseURL, iUser, iPassword);
    }

    /**
     * @param iPhysioSignalSamples list of physiological signal samples to be saved
     *
     * @throws Exception
     *
     * @brief save a list of physiological signal samples
     */
    @Override
    public void savePhysioSignalSamples(ArrayList<PhysioSignalModel> iPhysioSignalSamples) throws Exception {

        if(iPhysioSignalSamples.size() == 0){
            return;
        }

        BatchPoints lBatchPoints = BatchPoints
                .database(INFLUX_DB_PHYSIO_SIGNAL_NAME)
                .build();

        for (PhysioSignalModel lSample : iPhysioSignalSamples) {
            Point lPoint = buildPhysioSignalPoint(lSample);
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

        if(iSensitiveEvents.size() == 0){
            return;
        }

        BatchPoints lBatchPoints = BatchPoints
                .database(INFLUX_DB_NAME_SENSITIVE_EVENT_NAME)
                .build();

        for (SeizureEventModel lSeizureEvent : iSensitiveEvents) {
            Point lPoint = Point.measurement(DB_USER_EVENT_MEASUREMENT)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag(DB_UUID_TAG, lSeizureEvent.getUuid())
                    .tag(DB_USER_TAG, lSeizureEvent.getUser())
                    .tag(DB_TYPE_TAG, lSeizureEvent.getType())
                    .addField(DB_INTENSITY_TAG, lSeizureEvent.getIntensity())
                    .addField(DB_SENSITIVE_EVENT_TIMESTAMP_TAG, DateIso8601Mapper.getDate(lSeizureEvent.getSensitiveEventTimestamp()).getTime())
                    .build();

            lBatchPoints.point(lPoint);
        }

        mInfluxDB.write(lBatchPoints);
    }

    /**
     * @brief map a physio signal model to an influxDB point
     *
     * @param iPhysioSignalModel input physiological signal model
     * @return mapped influxDB point
     */
    private Point buildPhysioSignalPoint(PhysioSignalModel iPhysioSignalModel){
        if(iPhysioSignalModel.getType() == RRIntervalModel.RR_INTERVAL_TYPE){
            return buildRRIntervalPoint((RRIntervalModel) iPhysioSignalModel);
        }
        else if(iPhysioSignalModel.getType() == SkinTemperatureModel.SKIN_TEMPERATURE_TYPE){
            return buildSkinTemperaturePoint((SkinTemperatureModel) iPhysioSignalModel);
        }
        else if(iPhysioSignalModel.getType() == ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY){
            return buildElectroDermalActivityPoint((ElectroDermalActivityModel) iPhysioSignalModel);
        }
        else if(iPhysioSignalModel.getType() == MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL){
            return buildMotionAccelerometerModel((MotionAccelerometerModel) iPhysioSignalModel);
        }
        else if(iPhysioSignalModel.getType() == MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL){
            return buildMotionGyroscopeModel((MotionGyroscopeModel) iPhysioSignalModel);
        }

        return null;
    }

    /**
     * @brief map a RR interval model to an influxDB point
     *
     * @param iRrIntervalModel input RR interval model
     * @return mapped influxDB point
     */
    private Point buildRRIntervalPoint(RRIntervalModel iRrIntervalModel) {
        Point lPoint = Point.measurement(DB_HEART_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iRrIntervalModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iRrIntervalModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iRrIntervalModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iRrIntervalModel.getUuid())
                .tag(DB_TYPE_TAG, iRrIntervalModel.getType())
                .addField(DB_RR_INTERVAL_TAG, iRrIntervalModel.getRrInterval())
                .build();

        return lPoint;
    }

    /**
     * @brief map a skin temperature model to an influxDB point
     *
     * @param iSkinTemperatureModel input skin temperature model
     * @return mapped influxDB point
     */
    private Point buildSkinTemperaturePoint(SkinTemperatureModel iSkinTemperatureModel) {
        Point lPoint = Point.measurement(DB_TEMPERATURE_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iSkinTemperatureModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iSkinTemperatureModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iSkinTemperatureModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iSkinTemperatureModel.getUuid())
                .tag(DB_TYPE_TAG, iSkinTemperatureModel.getType())
                .addField(DB_SKIN_TEMPERATURE, iSkinTemperatureModel.getTemperature())
                .build();

        return lPoint;
    }

    /**
     * @brief map a electro dermal activity model to an influxDB point
     *
     * @param iElectroDermalActivityModel input electro dermal activity model
     * @return mapped influxDB point
     */
    private Point buildElectroDermalActivityPoint(ElectroDermalActivityModel iElectroDermalActivityModel){
        Point lPoint = Point.measurement(DB_ELECTRO_DERMAL_ACTIVITY_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iElectroDermalActivityModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iElectroDermalActivityModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iElectroDermalActivityModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iElectroDermalActivityModel.getUuid())
                .tag(DB_TYPE_TAG, iElectroDermalActivityModel.getType())
                .addField(DB_SENSOR_OUTPUT_FREQUENCY, iElectroDermalActivityModel.getSensorOutputFrequency())
                .addField(DB_ELECTRO_DERMAL_ACTIVITY, iElectroDermalActivityModel.getElectroDermalActivity())
                .build();

        return lPoint;
    }

    /**
     * @brief map a motion accelerometer model to an influxDB point
     *
     * @param iMotionAccelerometerModel input motion accelerometer model
     * @return mapped influxDB point
     */
    private Point buildMotionAccelerometerModel(MotionAccelerometerModel iMotionAccelerometerModel) {
        float[] lAccelerometer = iMotionAccelerometerModel.getAccelerometer();
        Point lPoint = Point.measurement(DB_ACCELEROMETER_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iMotionAccelerometerModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iMotionAccelerometerModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iMotionAccelerometerModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iMotionAccelerometerModel.getUuid())
                .tag(DB_TYPE_TAG, iMotionAccelerometerModel.getType())
                .addField(DB_X_TAG, lAccelerometer[0])
                .addField(DB_Y_TAG, lAccelerometer[1])
                .addField(DB_Z_TAG, lAccelerometer[2])
                .build();

        return lPoint;
    }

    /**
     * @brief map a motion gyroscope model to an influxDB point
     *
     * @param iMotionGyroscopeModel input motion gyroscope model
     * @return mapped influxDB point
     */
    private Point buildMotionGyroscopeModel(MotionGyroscopeModel iMotionGyroscopeModel) {
        float[] lGyroscope = iMotionGyroscopeModel.getGyroscope();
        Point lPoint = Point.measurement(DB_GYROSCOPE_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iMotionGyroscopeModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iMotionGyroscopeModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iMotionGyroscopeModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iMotionGyroscopeModel.getUuid())
                .tag(DB_TYPE_TAG, iMotionGyroscopeModel.getType())
                .addField(DB_X_TAG, lGyroscope[0])
                .addField(DB_Y_TAG, lGyroscope[1])
                .addField(DB_Z_TAG, lGyroscope[2])
                .build();

        return lPoint;
    }

    /**
     * @brief map a motion magnetometer model to an influxDB point
     *
     * @param iMotionMagnetometerModel input motion magnetometer model
     * @return mapped influxDB point
     */
    private Point buildMotionMagnetometerModel(MotionMagnetometerModel iMotionMagnetometerModel) {
        float[] lMagnetometer = iMotionMagnetometerModel.getMagnetometer();
        Point lPoint = Point.measurement(DB_GYROSCOPE_MEASUREMENT)
                .time(DateIso8601Mapper.getDate(iMotionMagnetometerModel.getTimestamp()).getTime(), TimeUnit.MILLISECONDS)
                .tag(DB_USER_TAG, iMotionMagnetometerModel.getUser())
                .tag(DB_DEVICE_ADDRESS_TAG, iMotionMagnetometerModel.getDeviceAdress())
                .tag(DB_UUID_TAG, iMotionMagnetometerModel.getUuid())
                .tag(DB_TYPE_TAG, iMotionMagnetometerModel.getType())
                .addField(DB_X_TAG, lMagnetometer[0])
                .addField(DB_Y_TAG, lMagnetometer[1])
                .addField(DB_Z_TAG, lMagnetometer[2])
                .build();

        return lPoint;
    }
}
