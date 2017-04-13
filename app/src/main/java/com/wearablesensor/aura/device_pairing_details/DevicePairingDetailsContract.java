package com.wearablesensor.aura.device_pairing_details;

import com.wearablesensor.aura.utils.BasePresenter;
import com.wearablesensor.aura.utils.BaseView;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingDetailsContract {

    interface View extends BaseView<Presenter> {

        void successPairing(String iDeviceName, String iDeviceAdress);

        void failParing();

        void setPresenter(Presenter iPresenter);
    }


    interface Presenter extends BasePresenter {

        void listenDevicePairingService();

    }
}
