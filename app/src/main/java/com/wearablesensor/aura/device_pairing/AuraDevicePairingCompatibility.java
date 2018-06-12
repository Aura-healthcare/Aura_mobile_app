/*
Aura Mobile Application
Copyright (C) 2018 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.device_pairing;


public class AuraDevicePairingCompatibility {
    /**
     * @brief check if available bluetooth devices are compatibles with Aura prototype
     *
     * @param iDeviceName device name for available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    public static boolean isCompatibleDevice(String iDeviceName) {

        if(isHeartRateCompatibleDevice(iDeviceName) || isGSRTemperatureCustomCompatibleDevice(iDeviceName) || isMotionMovuinoCompatibleDevice(iDeviceName) || isMetaWearCompatibleDevice(iDeviceName)){
            return true;
        }

        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for heart rate data streaming with Aura prototype
     *
     * @param iDeviceName device name for available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    public static boolean isHeartRateCompatibleDevice(String iDeviceName) {

        if(iDeviceName != null) {
            String lDeviceUpperName = iDeviceName.toUpperCase();

            if ((lDeviceUpperName.contains("RHYTHM") || lDeviceUpperName.contains("POLAR") || lDeviceUpperName.contains("MIO"))) {
                return true;
            }

            return false;
        }
        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for temperature and electro dermal activity
     * data streaming with Aura prototype
     *
     * @param iDeviceName device name for available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    public static boolean isGSRTemperatureCustomCompatibleDevice(String iDeviceName) {

        if(iDeviceName != null) {
            String lDeviceUpperName = iDeviceName.toUpperCase();

            if( lDeviceUpperName.contains("MAXREFDES73")) {
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for wrist motion
     * data streaming with Aura prototype
     *
     * @param iDeviceName device name for available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    public static boolean isMotionMovuinoCompatibleDevice(String iDeviceName){
        if(iDeviceName != null){
            String lDeviceUpperName = iDeviceName.toUpperCase();

            if( lDeviceUpperName.contains("MOVUINO")){
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for wrist motion
     * data streaming with Aura prototype
     *
     * @param iDeviceName device name for available bluetooth device
     * @return true if device is compatible, false otherwise
     */
    public static boolean isMetaWearCompatibleDevice(String iDeviceName){

        if(iDeviceName != null) {
            String lDeviceUpperName = iDeviceName.toUpperCase();

            if (lDeviceUpperName.contains("METAWEAR")) {
                return true;
            }

            return false;
        }
        return false;
    }
}
