/**
 * @file GattManager.java
 * @author  NordicSemiconductor
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * @section DESCRIPTION
 *
 */

package com.wearablesensor.aura.device_pairing.bluetooth.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.AsyncTask;
import android.util.Log;

import com.wearablesensor.aura.device_pairing.bluetooth.BluetoothLeService;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattCharacteristicReadOperation;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattDescriptorReadOperation;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattOperation;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.operations.GattSetNotificationOperation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GattManager {

    private final String TAG = this.getClass().getSimpleName();
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private ConcurrentHashMap<String, BluetoothGatt> mGatts;
    private GattOperation mCurrentOperation;
    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;

    private BluetoothLeService mBleService;

    public GattManager(BluetoothLeService iBleService) {
        mQueue = new ConcurrentLinkedQueue<>();
        mGatts = new ConcurrentHashMap<>();
        mCurrentOperation = null;
        mBleService = iBleService;
    }

    public synchronized void cancelCurrentOperationBundle() {
        Log.d(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        if(mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for(GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        Log.d(TAG, "Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        Log.d(TAG, "Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if(mCurrentOperation != null) {
            Log.d(TAG, "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        if( mQueue.size() == 0) {
            Log.d(TAG, "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }

        final GattOperation operation = mQueue.poll();
        Log.d(TAG, "Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);


        if(mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    Log.d(TAG, "Starting to do a background timeout");
                    wait(operation.getTimoutInMillis());
                } catch (InterruptedException e) {
                    Log.d(TAG, "was interrupted out of the timeout");
                }
                if(isCancelled()) {
                    Log.d(TAG, "The timeout was cancelled, so we do nothing.");
                    return null;
                }
                Log.d(TAG, "Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();

        mBleService.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final BluetoothDevice device = operation.getDevice();
                if (mGatts.containsKey(device.getAddress())) {
                    execute(mGatts.get(device.getAddress()), operation);
                } else {
                    device.connectGatt(mBleService, false, new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            super.onConnectionStateChange(gatt, status, newState);

                            if (status == 133) {
                                Log.d(TAG, "Got the status 133 bug, closing gatt");
                                gatt.close();
                                mGatts.remove(device.getAddress());
                                mBleService.deviceDisconnected(device);
                                return;
                            }

                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Log.d(TAG, "Gatt connected to device " + device.getAddress());
                                mGatts.put(device.getAddress(), gatt);
                                gatt.discoverServices();
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                Log.d(TAG, "Disconnected from gatt server " + device.getAddress() + ", newState: " + newState);
                                mGatts.remove(device.getAddress());
                                setCurrentOperation(null);
                                gatt.close();
                                drive();
                                mBleService.deviceDisconnected(device);
                            }
                        }

                        @Override
                        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorRead(gatt, descriptor, status);
                            ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
                            setCurrentOperation(null);
                            drive();
                        }

                        @Override
                        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorWrite(gatt, descriptor, status);
                            setCurrentOperation(null);
                            drive();
                        }

                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicRead(gatt, characteristic, status);
                            ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
                            setCurrentOperation(null);
                            drive();
                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            super.onServicesDiscovered(gatt, status);
                            Log.d(TAG, "services discovered, status: " + status);
                            execute(gatt, operation);
                            mBleService.deviceConnected(device);
                        }

                        @Override
                        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicWrite(gatt, characteristic, status);
                            Log.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on device " + device.getAddress());
                            setCurrentOperation(null);
                            drive();
                        }

                        @Override
                        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                            super.onCharacteristicChanged(gatt, characteristic);
                            Log.d(TAG, "Characteristic " + characteristic.getUuid() + "was changed, device: " + device.getAddress());
                            mBleService.receiveCharacteristicNotification(characteristic, device);
                        }
                    });
                }
            }
        });
    }

    public void execute(BluetoothGatt gatt, GattOperation operation) {
       if(operation != mCurrentOperation && operation.getClass().getSimpleName() == GattSetNotificationOperation.class.getSimpleName()) {
            return;
        }

        operation.execute(gatt);
        if(!operation.hasAvailableCompletionCallback()) {
            setCurrentOperation(null);
            drive();
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public BluetoothGatt getGatt(BluetoothDevice device) {
        return mGatts.get(device);
    }

    public void queue(GattOperationBundle bundle) {
        for(GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

}
