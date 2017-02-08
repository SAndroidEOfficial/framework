/**
 * Original Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.unibs.sandroide.lib.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeDevice{
    private final static String TAG = BluetoothLeDevice.class.getSimpleName();
    public BLEOnConnectionEventListener mBLEOnConnectionEventListener;
    
    
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

	private String mBluetoothDeviceAddress;
    private String mBluetoothDeviceName;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Context context;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    
    public final static int ACTION_GATT_NONE = 0;
    public final static int ACTION_GATT_CONNECTED = 5;
    public final static int ACTION_GATT_DISCONNECTED = 1;
    public final static int ACTION_GATT_SERVICES_DISCOVERED = 2;
    public final static int ACTION_DATA_AVAILABLE = 3;
    public final static int EXTRA_DATA = 4;
    public final static int EXTRA_SENSOR_TYPE = 6;
    public final static int ACTION_GATT_DISCONNECTED_STATE_8 = 7;
    public final static int ACTION_NAME_DEVICE_AVAILABLE_FROM_GATT_SERVICE=8;
    public final static int ACTION_DEVICE_DETACHMENT_REQUEST=9;
    
    
    public String getmBluetoothDeviceAddress() {
		return mBluetoothDeviceAddress;
	}

	public String getmBluetoothDeviceName() {
		return mBluetoothDeviceName;
	}
    public void setmBluetoothDeviceName(String mBluetoothDeviceName) {
        this.mBluetoothDeviceName=mBluetoothDeviceName;
    }

    
    public BluetoothLeDevice(Context context, String mBluetoothDeviceAddress) {
		this.context=context;
		this.mBluetoothDeviceAddress = mBluetoothDeviceAddress;
	}

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            int state=0;
            switch(newState)
            {
            case BluetoothProfile.STATE_CONNECTED:
                state = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                mBluetoothDeviceName=gatt.getDevice().getName();
                Log.d(TAG, "name at connection: " + mBluetoothDeviceName);
                
                if (mBLEOnConnectionEventListener!=null)
                	mBLEOnConnectionEventListener.onConnectionEvent(state, null);
                else
                	Log.i(TAG, "mBLEOnConnectionEventListener is null1");
                Log.i(TAG, "Connected to GATT server.");
            	break;
            	
            case BluetoothProfile.STATE_DISCONNECTED:
            		state = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                if (mBLEOnConnectionEventListener!=null)
                	mBLEOnConnectionEventListener.onConnectionEvent(state, null);
                else
                	Log.i(TAG, "mBLEOnConnectionEventListener is null2");
            	break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (mBluetoothDeviceName==null)
                mBluetoothDeviceName=gatt.getDevice().getName();
        	Log.d(TAG, "name: "+mBluetoothDeviceName);
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	if (mBLEOnConnectionEventListener!=null)
            		mBLEOnConnectionEventListener.onConnectionEvent(ACTION_GATT_SERVICES_DISCOVERED, null);
                else
                	Log.i(TAG, "mBLEOnConnectionEventListener is null3");
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	if (mBLEOnConnectionEventListener!=null)
            		mBLEOnConnectionEventListener.onConnectionEvent(ACTION_DATA_AVAILABLE, characteristic);
                else
                	Log.i(TAG, "mBLEOnConnectionEventListener is null4");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	if (mBLEOnConnectionEventListener!=null)
        		mBLEOnConnectionEventListener.onConnectionEvent(ACTION_DATA_AVAILABLE, characteristic);
            else
            	Log.i(TAG, "mBLEOnConnectionEventListener is null5");
        }
        
        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
        	Log.d(TAG, "onDescriptorStatus is " + status);
        	Log.d(TAG, "BluetoothGattDescriptor is " + descriptor);
        }
    };

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        //mBluetoothDeviceAddress = address;
        
        mConnectionState = STATE_CONNECTING;
        return true;
    }
    
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * 
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect() {
        if (mBluetoothAdapter == null || mBluetoothDeviceAddress == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
/*
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && mBluetoothDeviceAddress.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }*/

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        //mBluetoothDeviceName=device.getName();
        //mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "read initiated: "+mBluetoothGatt.readCharacteristic(characteristic));
    }
    

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }
    
    public void setBLEOnConnectionEventListener(BLEOnConnectionEventListener mBLEOnConnectionEventListener)
    {
    	this.mBLEOnConnectionEventListener=mBLEOnConnectionEventListener;
    }

    public BluetoothGattService getService(UUID uuid)
    {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getService(uuid);
    }

    public BluetoothGattCharacteristic getCharact(UUID uuidService, UUID uuidCharact)
    {
        if (mBluetoothGatt == null) return null;
        BluetoothGattService bluetoothGattService= mBluetoothGatt.getService(uuidService);
        if (bluetoothGattService!=null)
            return bluetoothGattService.getCharacteristic(uuidCharact);
        else
            return null;
    }

    public void discoverServices()
    {
        if (mBluetoothGatt!=null)
            mBluetoothGatt.discoverServices();
    }

}
