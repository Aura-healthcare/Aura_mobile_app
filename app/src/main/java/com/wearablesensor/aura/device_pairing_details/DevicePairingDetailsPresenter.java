package com.wearablesensor.aura.device_pairing_details;

import android.util.Log;

import com.wearablesensor.aura.device_pairing.notifications.DevicePairingConnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingServiceObserver;
import com.wearablesensor.aura.device_pairing.BluetoothDevicePairingService;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

/**
 * Created by lecoucl on 07/04/17.
 */
public class DevicePairingDetailsPresenter extends DevicePairingServiceObserver implements DevicePairingDetailsContract.Presenter {

    private final String TAG = this.getClass().getSimpleName();

    private final BluetoothDevicePairingService mBluetoothDevicePairingService;

    private final DevicePairingDetailsContract.View mView;

    public DevicePairingDetailsPresenter(BluetoothDevicePairingService iBluetoothDevicePairingService, DevicePairingDetailsContract.View iView){
        mBluetoothDevicePairingService = iBluetoothDevicePairingService;
        mView = iView;

        mView.setPresenter(this);
    }

    @Override
    public void start() {
        listenDevicePairingService();
    }

    @Override
    public void listenDevicePairingService() {
        mBluetoothDevicePairingService.addObserver(this);
    }

    @Override
    public void onDevicePairingServiceNotification(DevicePairingNotification iDevicePairingNotification) {
        DevicePairingStatus lStatus = iDevicePairingNotification.getStatus();
        Log.d(TAG, "DevicePairing onNotificationReceived " + lStatus);

        if(lStatus == DevicePairingStatus.CONNECTED){
            DevicePairingConnectedNotification lDevicePairingNotification = (DevicePairingConnectedNotification) iDevicePairingNotification;
            mView.successPairing(lDevicePairingNotification.getDeviceName(), lDevicePairingNotification.getDeviceAdress());
        }
        else if(lStatus == DevicePairingStatus.DISCONNECTED){
            mView.failParing();
        }
    }
}
