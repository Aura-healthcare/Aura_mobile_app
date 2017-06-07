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

import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingDetailsContract {

    interface View extends BaseView<Presenter> {

        void progressPairing();

        void successPairing(String iDeviceName, String iDeviceAdress);

        void failParing();

        void setPresenter(Presenter iPresenter);
    }


    interface Presenter extends BasePresenter {

        void listenDevicePairingService();

    }
}
