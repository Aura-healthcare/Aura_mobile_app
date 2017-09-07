/**
 * @file DeviceInfo.java
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
 *
 *
 */

package com.wearablesensor.aura.device_pairing;

import com.wearablesensor.aura.device_pairing_details.BatteryLevel;

public class DeviceInfo {
    private String mId;
    private String mName;

    private BatteryLevel mBatteryLevel;

    public DeviceInfo(String iId, String iName){
        mId = iId;
        mName = iName;
        mBatteryLevel = BatteryLevel.UNKNOWN;
    }

    public DeviceInfo(String iId, String iName, int iBatteryLevelPercentage){
        mId = iId;
        mName = iName;
        setBatteryLevel(iBatteryLevelPercentage);
    }

    public String getId(){
        return mId;
    }

    public String getName(){
        return mName;
    }

    /**
     * @brief convert battery level percentage to battery level and set it to device
     *
     * @param iBatteryLevelPercentage input battery level percentage
     */
    public void setBatteryLevel(int iBatteryLevelPercentage) {
        if (iBatteryLevelPercentage <= 100 && iBatteryLevelPercentage > 70) {
            mBatteryLevel = BatteryLevel.HIGH;
        } else if (iBatteryLevelPercentage <= 70 && iBatteryLevelPercentage > 30) {
            mBatteryLevel = BatteryLevel.MEDIUM;
        } else if (iBatteryLevelPercentage <= 30 && iBatteryLevelPercentage > 10) {
            mBatteryLevel = BatteryLevel.LOW;
        } else if (iBatteryLevelPercentage <= 10) {
            mBatteryLevel = BatteryLevel.VERY_LOW;
        } else {
            mBatteryLevel = BatteryLevel.UNKNOWN;
        }
    }

    /**
     * @brief getter
     *
     * @return battery level
     */
    public BatteryLevel getBatteryLevel(){
        return mBatteryLevel;
    }

    /**
     * @brief setter
     *
     * @param iBatteryLevel input battery level
     */
    public void setBatteryLevel(BatteryLevel iBatteryLevel) {
        mBatteryLevel = iBatteryLevel;
    }
}
