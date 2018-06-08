/**
 * @file DeviceScanDetailsContract
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
 */
package com.wearablesensor.aura.device_scan_details;

import com.idevicesinc.sweetblue.BleDevice;
import com.wearablesensor.aura.device_pairing.DeviceInfo;
import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

import java.util.LinkedList;

public class DeviceScanDetailsContract {

    interface View extends BaseView<Presenter> {
        /**
         * @brief start scan display
         *
         * @param iDeviceList device list to be displayed
         */
        void displayStartScan(LinkedList<BleDevice> iDeviceList);

        /**
         * @brief device discovered display
         *
         * @param iDeviceList device list to be displayed
         */
        void displayDeviceDiscovered(LinkedList<BleDevice> iDeviceList);

        void displayStartConnecting();

        void displayDeviceConnected();

        void displayDeviceDisconnected();

        void enableStartMonitoring();

        void disableStartMonitoring();

        /**
         * @brief end scan display
         *
         * @param iDeviceList device list to be displayed
         */
        void displayEndScan(LinkedList<BleDevice> iDeviceList);

        void setPresenter(Presenter iPresenter);
    }


    interface Presenter extends BasePresenter {
        void startScan();

        void connectDevice(BleDevice iBleDevice);

        void goToSeizureMonitoring();
    }
}
