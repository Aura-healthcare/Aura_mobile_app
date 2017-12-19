/**
 * @file ModelSerializer.java
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
 * a helper class used to serialize/deserialize the physiological signal models
 *
 */
package com.wearablesensor.aura.data_repository.models;

public class ModelSerializer{

    /**
     * @brief serialize a model to a string in order to be stored in local data file
     *
     * @param iPhysioSignalModel serialized model
     * @return export string
     */
    public static String serialize(PhysioSignalModel iPhysioSignalModel){
        return iPhysioSignalModel.toString();
    }

    /**
     * @brief deserialize a string to a model
     *
     * @param iContent string to be deserialized
     * @return export model
     */
    public static PhysioSignalModel deserialize(String iContent) {
        String[] lArgs = iContent.split(" ");
        if(lArgs[1].equals(RRIntervalModel.RR_INTERVAL_TYPE)){
            return new RRIntervalModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2], Integer.parseInt(lArgs[5]));
        }
        else if( lArgs[1].equals(SkinTemperatureModel.SKIN_TEMPERATURE_TYPE) ){
            return new SkinTemperatureModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2],  Float.parseFloat(lArgs[5])  );
        }
        else if(lArgs[1].equals(ElectroDermalActivityModel.ELECTRO_DERMAL_ACTIVITY)){
            return new ElectroDermalActivityModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2], Integer.parseInt(lArgs[6]), Integer.parseInt(lArgs[5]));
        }
        else if(lArgs[1].equals(MotionAccelerometerModel.MOTION_ACCELEROMETER_MODEL)){
            float[] lValues = new float[3];
            lValues[0] = Float.parseFloat(lArgs[5]);
            lValues[1] = Float.parseFloat(lArgs[6]);
            lValues[2] = Float.parseFloat(lArgs[7]);
            return new MotionAccelerometerModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2], lValues, lArgs[7]);
        }
        else if(lArgs[1].equals(MotionGyroscopeModel.MOTION_GYROSCOPE_MODEL)){
            float[] lValues = new float[3];
            lValues[0] = Float.parseFloat(lArgs[5]);
            lValues[1] = Float.parseFloat(lArgs[6]);
            lValues[2] = Float.parseFloat(lArgs[7]);
            return new MotionGyroscopeModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2], lValues);
        }
        else if(lArgs[1].equals(MotionMagnetometerModel.MOTION_MAGNETOMETER_MODEL)){
            float[] lValues = new float[3];
            lValues[0] = Float.parseFloat(lArgs[5]);
            lValues[1] = Float.parseFloat(lArgs[6]);
            lValues[2] = Float.parseFloat(lArgs[7]);
            return new MotionMagnetometerModel(lArgs[0], lArgs[4], lArgs[3], lArgs[2], lValues);
        }

        return null;
    }
}
