/**
 * @file RemoteDataInfluxDBRepository.java
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
 *  1) connect to InfluxDB database
 * Currently secured connection to database is done using basic user/password credentials.
 *  2) 3 .. N) query or save data in database
 */


package com.wearablesensor.aura.data_repository;


import android.content.Context;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RemoteDataWebSocketRepository extends WebSocketClient implements RemoteDataRepository.TimeSeries{

    private final String TAG = this.getClass().getSimpleName();
    private Context mApplicationContext;

    public final static String PREPROD_SERVER_URL = "wss://db.preprod.aura.healthcare";

    public RemoteDataWebSocketRepository(String iDatabaseUrl, Context iApplicationContext) throws URISyntaxException {
        super(new URI(iDatabaseUrl));
        mApplicationContext = iApplicationContext;
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


        SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            this.setSocket( factory.createSocket() );
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
        this.send(iData);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen");

    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage");

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
