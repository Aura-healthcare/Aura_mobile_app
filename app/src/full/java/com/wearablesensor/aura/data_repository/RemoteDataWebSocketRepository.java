/**
 * @file RemoteDataWebSocketRepository.java
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
 *  1) connect to web socket server
 *  2) send data files as messages
 */


package com.wearablesensor.aura.data_repository;


import android.content.Context;
import android.util.Log;

import com.wearablesensor.aura.data_sync.notifications.DataAckNotification;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RemoteDataWebSocketRepository extends WebSocketClient implements RemoteDataRepository.TimeSeries{

    private final String TAG = this.getClass().getSimpleName();

    public final static String PREPROD_SERVER_URL = "wss://data.preprod.aura.healthcare";

    public RemoteDataWebSocketRepository(String iDatabaseUrl, Context iApplicationContext) throws URISyntaxException {
        super(new URI(iDatabaseUrl));
    }

    @Override
    public void connectToServer() {

        SSLContext sslContext;

        try {
            sslContext = SSLContext.getInstance("TLS");

            // Custom trust manager used to accept self-signed certificate
            //TODO: switch to standard SSLContext to once we plug the official Let's encrypt on server
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }
        catch ( NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }


        SSLSocketFactory factory = sslContext.getSocketFactory();

        try {
            this.setSocket( factory.createSocket() );

            // force web-socket to stay open
            this.getSocket().setSoTimeout(0);
        } catch (IOException e) {
            Log.d(TAG, "Socket IO exception");
            e.printStackTrace();
        }

        try {
            this.connectBlocking();
        } catch (InterruptedException e) {
            Log.d(TAG, "Fail to connect");
            e.printStackTrace();
        }
    }

    @Override
    public void save(String iData) {
        Log.d("SendAll", "Send");
        this.send(iData);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen");

    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage " + message);
        DataAckNotification lAckNotification = new DataAckNotification(message);
        if(lAckNotification.getStatus().equals("OK") ){
            Log.d(TAG, "sendDeleteFile - " + lAckNotification.getFileName() + " - " + lAckNotification.getStatus());
            EventBus.getDefault().post(lAckNotification);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose " + reason);

    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "onError");
        ex.printStackTrace();
    }


}
