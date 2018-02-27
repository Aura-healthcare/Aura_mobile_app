/**
 * @file GattCharacteristicReader.java
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
 * GattCharacteristicReader is an interface that allows to parse a GattCharacteristic
 * and convert it into a physiological data model
 *
 */

package com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader;

import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;

public interface GattCharacteristicReader {

    /**
     * @brief helper method use to parse a GattCharacteristic and convert it into a
     * physiological data
     *
     * @param event gatt characteristic
     * @return true if read succeed, false otherwise
     */
    Boolean read(PhysioEvent event);
}
