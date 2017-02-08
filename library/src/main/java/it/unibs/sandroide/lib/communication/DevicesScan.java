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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.BLEEmbeddedEvent;

/**
 * Class for scanning available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DevicesScan {
    private static BluetoothAdapter mBluetoothAdapter;
    private static boolean mScanning;
    //private Handler mHandler;
    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //invalidateOptionsMenu();
        }
    };
    
    
    private static List<BluetoothDevice> mBluetoothDevices=new ArrayList<BluetoothDevice>();
    
    private String searchedDevice="";
    private static String deviceName="";
    private static String deviceAddress="";

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;

    static Timer stopScanTimer;
    
    static BLEOnScanListener mBleOnScanListener;
    

    public DevicesScan() {}
    
    public String[] getDevice(String nameDevice)
    {
    	searchedDevice=nameDevice;

        if (!BLEContext.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BLEContext.triggerBleEmbeddedEventListener
                    (BLEEmbeddedEvent.BLE_NOT_SUPPORTED);
            return null;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) BLEContext.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            BLEContext.triggerBleEmbeddedEventListener
                    (BLEEmbeddedEvent.BLE_NOT_SUPPORTED);
            return null;
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
            	mBluetoothAdapter.enable();
            }
        }

        scanLeDevice(true);
        if (nameDevice.isEmpty()) return null;
        
        return new String[]{deviceName, deviceAddress};
    	
    }
    
    public static List<BluetoothDevice> getDevices()
    {
    	setCallback();
        if (!BLEContext.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BLEContext.triggerBleEmbeddedEventListener
                    (BLEEmbeddedEvent.BLE_NOT_SUPPORTED);
            return null;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) BLEContext.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            BLEContext.triggerBleEmbeddedEventListener
                    (BLEEmbeddedEvent.BLE_NOT_SUPPORTED);
            return null;
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
            	mBluetoothAdapter.enable();
            }
        }

        scanLeDevice(true);
        
        return mBluetoothDevices;
    }


    public static void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
        	mBluetoothDevices= new ArrayList<BluetoothDevice>();
            stopScanTimer = new Timer("stopScanTimer");
            stopScanTimer.schedule(
                    new TimerTask() {

						@Override
						public void run() {
				            mScanning = false;
				            mBluetoothAdapter.stopLeScan(mLeScanCallback);
				            //
				            if (mBluetoothDevices.size()==0)
				            {
                                BLEContext.triggerBleEmbeddedEventListener
                                        (BLEEmbeddedEvent.NO_BLE_DEVICES_FOUND);
				            }
				            
				            DevicesScan.mBleOnScanListener.onScanStop(mBluetoothDevices);
						}
                            
                    	
                    },
                    SCAN_PERIOD);
        	
    		deviceName="";
    		deviceAddress="";
            mScanning = true;

            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    
    public static void scanLeDevice(final boolean enable, List<String> devicesAddress)
    {
    	scanLeDevice(enable);
    }

    
    // Device scan callback.
    //private static ScanCallback mLeScanCallbackUp21;
    
    
    // Device scan callback.
    private static BluetoothAdapter.LeScanCallback mLeScanCallback;
    
    public static void setOnScanListener(BLEOnScanListener mBleOnScanStopListener)
    {
    	DevicesScan.mBleOnScanListener=mBleOnScanStopListener;
    }
    
    public List<BluetoothDevice> getDevicesList()
    {
    	return mBluetoothDevices;
    }
    
    public void resetDevicesList()
    {
    	mBluetoothDevices=new ArrayList<BluetoothDevice>();
    }
    
    public static void setCallback() {
    	
        final BluetoothManager bluetoothManager =
                (BluetoothManager) BLEContext.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    	
		mLeScanCallback =
	            new BluetoothAdapter.LeScanCallback() {

	        @Override
	        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	        	
	        	mBluetoothDevices.add(device);
	        	mBleOnScanListener.onDeviceFound(device);
	        }
	    };
    	

	}
    
}