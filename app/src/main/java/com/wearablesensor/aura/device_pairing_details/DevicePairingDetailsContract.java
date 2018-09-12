/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

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

package com.wearablesensor.aura.device_pairing_details;

import com.wearablesensor.aura.device_pairing.DeviceInfo;
import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

import java.util.LinkedList;

public class DevicePairingDetailsContract {

    interface View extends BaseView<Presenter> {

        void progressPairing();

        void successPairing(LinkedList<DeviceInfo> iDeviceList);

        void failParing();

        void refreshDeviceBatteryLevel(DeviceInfo iDeviceInfo);

        void setPresenter(Presenter iPresenter);

        void refreshSessionDuration(long lSessionDuration);
    }


    interface Presenter extends BasePresenter {
        void startScanning();
    }
}
