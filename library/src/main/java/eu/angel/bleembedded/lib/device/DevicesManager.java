/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini and Angelo Vezzoli, All rights reserved.
 *
 * @author  Angelo Vezzoli
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
package eu.angel.bleembedded.lib.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.BLEEmbeddedEvent;
import eu.angel.bleembedded.lib.communication.BLEOnConnectionEventToDevicesManagerListener;
import eu.angel.bleembedded.lib.communication.BLEOnScanListener;
import eu.angel.bleembedded.lib.communication.BluetoothLeDevice;
import eu.angel.bleembedded.lib.communication.DevicesScan;
import eu.angel.bleembedded.lib.item.BLEItem;

/**
 * Class which handles the {@link DeviceControl}s.
 */
public class DevicesManager{

	private static final String TAG = "DevicesManager";

	private static boolean scanning=false;

	private static List<DeviceControl> mDeviceControls=new ArrayList<>();
	private static List<DeviceControl> mDeviceControlsWaitingForServicesReq=new ArrayList<>();
	private static Timer deviceControlsWaitingForServicesReqTimer;
	private static int MAX_DELAY_BETWEEN_DEVICES_SERVICES_REQUEST = 2000;

	private static List<BLEItem> mAttemptingToConnectBLEItems = new ArrayList<>();

	/**
	 * Scans the neighborhood searching for BLE devices
	 *
	 * @param mBleOnScanStopListener listener for scanning operation
	 *
	 *
	 */
    private static void scanDevice(BLEOnScanListener mBleOnScanStopListener)
    {
    	scanning=true;
    	DevicesScan.setOnScanListener(mBleOnScanStopListener);
    	DevicesScan.getDevices();
    }
/*

	private static BLEOnScanListener connectGenericItem = new BLEOnScanListener() {

		@Override
		public void onScanStop(List<BluetoothDevice> mBluetoothDevices) {
			Log.d(TAG, "onScanStop.. connectGenericItem: " + mBluetoothDevices);
			scanning=false;
			if (mBluetoothDevices!=null)
			{
				if (!mBluetoothDevices.isEmpty())
				{
					btdevLoop:
					for (BluetoothDevice mBluetoothDevice:mBluetoothDevices)
					{
						checkAndAddDeviceControl(mBluetoothDevice);
					}
				}
			}
		}

		@Override
		public void onDeviceFound(BluetoothDevice bluetoothDevice) {
			Log.d(TAG, "onDeviceFound.. connectGenericItem: " + bluetoothDevice);
			checkAndAddDeviceControl(bluetoothDevice);
		}
	};
	
	
	public static void checkAndAddDeviceControl(BluetoothDevice bluetoothDevice)
	{ 
		String BTdevAdd=bluetoothDevice.getAddress();
		String BTdevName=bluetoothDevice.getName();
		
		checkAndAddDeviceControl(BTdevAdd, BTdevName);
	}*/

	/**
	 * Check whether the bluetooth device identified by name and mac address
	 * is associated to one of the {@link BLEItem} required, if so the bluetooth is connected.
	 * @param BTdevAdd mac address of the analyzed device
	 * @param BTdevName name of the analyzed device
	 *
	 */
	public static void checkAndAddDeviceControl(String BTdevAdd, String BTdevName)
	{
		for (DeviceControl mDeviceControl:mDeviceControls)
		{
			Log.e(TAG, "device scanned whereas it should be already connected!!!!!");
			Log.d(TAG, "il mDeviceControl  : " + mDeviceControl);
			String mDevAdd=mDeviceControl.getmDeviceAddress();
			Log.d(TAG, "il getmDeviceAddress  : " + mDevAdd);
			Log.d(TAG, "il getAddress  : " + BTdevAdd);
			if (mDevAdd.equalsIgnoreCase(BTdevAdd))
				return;
		}
		if ((BTdevName!=null)&&(BTdevAdd!=null))
		{
			List<BLEItem> mBLEItemsCopy=getBLEItems();
			for (int i=0;i<mBLEItemsCopy.size();i++)
			{
				if (mBLEItemsCopy.get(i).getAddressRef().equalsIgnoreCase(BTdevAdd))
				return;
			}
			addDeviceControl(BTdevAdd, BTdevName);
			return;
		}
	}


	/**
	 * Add the {@link DeviceControl} to the list of handled {@link DeviceControl}
	 * @param deviceControl the {@link DeviceControl} to be added to the list
	 *
	 */
	public static void addNewDeviceControl(DeviceControl deviceControl)
	{ 
		for (DeviceControl mDeviceControl:mDeviceControls)
		{
			if (mDeviceControl.getmDeviceAddress().equalsIgnoreCase
					(deviceControl.getmDeviceAddress()))
				return;
		}
		mDeviceControls.add(deviceControl);
	}



    /**
     * Connects the phone to the BLE device at address 'deviceAddres'.
     * If already connected with BLEdevices do not force another Scan
     * 
     * @param mBleItem {@link BLEItem} address of the device to connect with.
     * If deviceAddress {@link null} the phone 
     * will be connected with all the devices with the required service
     * @param requestedService to connect only with the devices providing this kind of service
     *
     */
	public static void connectDevice(BLEItem mBleItem, int requestedService)
	{
		final String deviceAddress=mBleItem.getAddressRef();
		Log.d(TAG, "connectDevice");
		
		for (DeviceControl mDeviceControl: mDeviceControls)
		{
			Log.d(TAG, "provear #####storedDevMac: "+mDeviceControl.getmDeviceAddress()+"; ItemMac: "+mBleItem.getBleresource().getDevmacaddress());
			if (mDeviceControl.getmDeviceAddress().equalsIgnoreCase
					(mBleItem.getBleresource().getDevmacaddress()))
			{
				//if(mDeviceControl.isItemEmbedded(mBleItem))
				if(mDeviceControl.isDevItemEmbedded(mBleItem.getDevItem()))
				{
					if ((deviceAddress==null)||
							(mBleItem.getAddressRef().equalsIgnoreCase(mDeviceControl.getmDeviceAddress())))
					{
						mBleItem.setDeviceControl(mDeviceControl);
						Log.d(TAG, "provear connected device " + mDeviceControl + " with Item " + mBleItem);
						return;
					}
				}
			}
		}

		//warning: the position of these two function has to be held
		checkAndAddDeviceControl(deviceAddress.toUpperCase(), mBleItem.getDeviceNameRef());
		addBLEItemToBLEItems(mBleItem);
	}


	/**
	 * Removes from the list of the handled {@link DeviceControl} the non operative {@link DeviceControl}s
	 *
	 */
	//TODO: to be tested
	private static void removeNonOperativeConnectedDevices()
	{
		for (DeviceControl mDeviceControl:mDeviceControls)
		{
			if (mDeviceControl.getmOperative())
				continue;
			mDeviceControls.remove(mDeviceControl);
		}
	}

	/**
	 * Creates and initializes the {@link DeviceControl}
	 * @param deviceAddress mac address of the analyzed device
	 * @param deviceName name of the analyzed device
	 */
	private static void addDeviceControl(String deviceAddress, String deviceName)
	{
		Log.d(TAG, "adding dev: " + deviceName);

		DeviceControl mDeviceControl=new DeviceControl(BLEContext.context, deviceName,
				deviceAddress, DevicesManagerBLEOnConnectionEventListener);

		mDeviceControl.initDeviceControl();
	}

	/**
	 * Adds the {@link BLEItem} to the list of requiring the connection {@link BLEItem}s
	 * @param bleItem
	 */
	public static void addBLEItemToBLEItems(BLEItem bleItem)
	{
		Log.d(TAG, "adding Item to Items: " + bleItem);
		mAttemptingToConnectBLEItems.add(bleItem);
	}

	/**
	 * @return the list of requiring the connection {@link BLEItem}s
	 */
	private static List<BLEItem> getBLEItems()
	{
		return mAttemptingToConnectBLEItems;
	}
	
	private static void removeItemFromBLEItems(int index)
	{
		Log.d(TAG, "removing: " + index + " size: " + mAttemptingToConnectBLEItems.size());
		mAttemptingToConnectBLEItems.remove(index);
	}

	/**
	 * The implementations of {@link BLEOnConnectionEventToDevicesManagerListener} interface handles
	 * the life of the {@link DeviceControl}s connected through the library.
	 */
	private static BLEOnConnectionEventToDevicesManagerListener
			DevicesManagerBLEOnConnectionEventListener =
			new BLEOnConnectionEventToDevicesManagerListener() {
		
		@Override
		synchronized public void onConnectionEventToDevicesManager(int state,
				DeviceControl mDeviceControlReturned) {
			
			DeviceControl mDeviceControl;
			List<BLEItem> mBLEItemsCopy=getBLEItems();
			switch(state)
			{

				case BluetoothLeDevice.ACTION_GATT_CONNECTED:
					addServicesRequest(mDeviceControlReturned);
					break;

				case BluetoothLeDevice.ACTION_GATT_SERVICES_DISCOVERED:
					Log.d(TAG, "Service discovered, next device services request");
					nextServiceRequest();
					break;

				case BluetoothLeDevice.ACTION_NAME_DEVICE_AVAILABLE_FROM_GATT_SERVICE:

					if (!mDeviceControlReturned.isDeviceDescriptorSet())
					{

						if (mDeviceControlReturned.setDeviceDescriptor())
						{
							Log.d(TAG, "trying adding new deviceControl: "+mDeviceControlReturned);
							//mDeviceControls.add(mDeviceControl);
							addNewDeviceControl(mDeviceControlReturned);
							if (!mDeviceControlReturned.getmDeviceName()
									.equals(mDeviceControlReturned.getmDeviceName()))
							{
								Log.d(TAG, "the devName req and the real name are different");
								BLEContext.triggerBleEmbeddedEventListener
									(BLEEmbeddedEvent.NAME_BLERESOURCE_DIFFERENT_FROM_REAL_NAME);
							}
						}
						else
						{
							BLEContext.triggerBleEmbeddedEventListener
								(BLEEmbeddedEvent.BLE_DEVICE_NOT_FOUND);
							return;
						}

					}
					else
					{
						mDeviceControl=mDeviceControlReturned;
						if (mDeviceControl!=null)
							addNewDeviceControl(mDeviceControl);
						else
							return;
					}

					Log.d(TAG, "il dev  :1 " + mDeviceControlReturned);
					List<Integer> indexToRm= new ArrayList<>();
					Log.d(TAG, mBLEItemsCopy.size() + " Items to connect");
					List<BLEItem> connectedItems=new ArrayList<>();
					for (int i=0; i<mBLEItemsCopy.size();i++)
					{
						Log.d(TAG, "il mBLEItems " + i + "  : " + mBLEItemsCopy.get(i));
						//if(mDeviceControl.isItemEmbedded(mBLEItemsCopy.get(i)))
						if (mDeviceControlReturned.getmDeviceAddress().equalsIgnoreCase
								(mBLEItemsCopy.get(i).getBleresource().getDevmacaddress()))
						{
							if(mDeviceControlReturned.isDevItemEmbedded(mBLEItemsCopy.get(i).getDevItem()))
							{
								if ((mBLEItemsCopy.get(i).getAddressRef()==null)||
										(mBLEItemsCopy.get(i).getAddressRef().equalsIgnoreCase(mDeviceControlReturned.getmDeviceAddress())))
								{
									mBLEItemsCopy.get(i).setDeviceControl(mDeviceControlReturned);
									mBLEItemsCopy.get(i).setActualDeviceName(mDeviceControlReturned.getmDeviceName());
									Log.d(TAG, "connected device " + mDeviceControlReturned + " with Item " + mBLEItemsCopy.get(i) + " at i: " + i);
									mDeviceControlReturned.addInItems(mBLEItemsCopy.get(i));
									connectedItems.add(mBLEItemsCopy.get(i));
									indexToRm.add(i);
								}
							}
						}
					}

					for (int i=0;i<indexToRm.size();i++)
					{
						Log.d(TAG, "mBLEItems: removed Item, mBleItems length is " + getBLEItems().size() + "index i: " + indexToRm.get(i));
						removeItemFromBLEItems(indexToRm.get(i));
						for (int u=i+1;u<indexToRm.size();u++)
						{
							indexToRm.set(u, indexToRm.get(u)-1);
						}
					}

					for (int i=0; i<connectedItems.size();i++)
						BLEContext.triggerBleEmbeddedEventListenerConnection(connectedItems.get(i));

					break;

				case BluetoothLeDevice.ACTION_GATT_DISCONNECTED:
					List<BLEItem> items=mDeviceControlReturned.getItems();
					for (BLEItem item:items)
					{
						addBLEItemToBLEItems(item);
						mDeviceControlReturned.resetAfterDisconnection();
						BLEContext.triggerBleEmbeddedEventListenerDisconnection(item);
					}

					mDeviceControlReturned.releaseItems();
					break;

				case BluetoothLeDevice.ACTION_DEVICE_DETACHMENT_REQUEST:
					List<BLEItem> mbleItems=BLEContext.cloneMbleItems();
					for (BLEItem bleItem:mbleItems)
					{
						if (bleItem.isDeviceControlConnected())
						{
							if (bleItem.getDeviceAddress().equalsIgnoreCase
									(mDeviceControlReturned.getmDeviceAddress()))
								return;
						}
						mDeviceControlReturned.mBluetoothLeDevice.disconnect();
						mDeviceControlReturned.mBluetoothLeDevice.close();
						mDeviceControlReturned.mBluetoothLeDevice=null;
						mDeviceControlReturned=null;
					}
					break;
			}
			
		}
	};

	/**
	 * Adds the {@link DeviceControl} to the list in order to require the beginning of the {@link DeviceControl}
	 * services. Useful to insert delay between the service requirements.
	 * @param deviceControl
	 */
	private synchronized static void addServicesRequest(DeviceControl deviceControl)
	{
		mDeviceControlsWaitingForServicesReq.add(deviceControl);
		startServiceRequest();
	}

	/**
	 * Removes the last {@link DeviceControl} requiring service beginning and start the next one
	 */
	private synchronized static void removeAndTriggerNextServiceRequest()
	{
		if (mDeviceControlsWaitingForServicesReq.size()>0)
			mDeviceControlsWaitingForServicesReq.remove(0);
		if (mDeviceControlsWaitingForServicesReq.size()>0)
			startServiceRequest();
	}

	/**
	 * Reset the {@link Timer} used to handle the service requests
	 */
	private synchronized static void resetServiceRequestTimer()
	{
		if (deviceControlsWaitingForServicesReqTimer!=null)
		{
			deviceControlsWaitingForServicesReqTimer.purge();
			deviceControlsWaitingForServicesReqTimer.cancel();
			deviceControlsWaitingForServicesReqTimer=null;
		}
	}

	/**
	 * Handles the next service request
	 */
	private synchronized static void nextServiceRequest()
	{
		resetServiceRequestTimer();
		removeAndTriggerNextServiceRequest();
	}

	/**
	 * Starts the current service
	 */
	private synchronized static void startServiceRequest()
	{
		if (deviceControlsWaitingForServicesReqTimer==null)
		{
			if (mDeviceControlsWaitingForServicesReq.size()>0)
			{
				mDeviceControlsWaitingForServicesReq.get(0).requestServices();
				deviceControlsWaitingForServicesReqTimer=new Timer();
				deviceControlsWaitingForServicesReqTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						nextServiceRequest();
					}
				}, MAX_DELAY_BETWEEN_DEVICES_SERVICES_REQUEST);
			}
		}
	}

	/**
	 * Flushes the {@link DeviceControl} list
	 */
	public static void removeAllDevices()
	{
		for (DeviceControl deviceControl:mDeviceControls)
		{
			deviceControl.mBluetoothLeDevice.disconnect();
			deviceControl.mBluetoothLeDevice.close();
			deviceControl.mBluetoothLeDevice=null;
			deviceControl=null;
		}
		mDeviceControls=new ArrayList<>();
	}

	/**
	 * Stops the {@link DeviceControl} identified by the {@link BLEItem}
	 * @param bleItems the list of the {@link BLEItem}s connected
	 * @param bleItemTarget the {@link BLEItem} whose {@link DeviceControl} has to be stopped
	 */
	public static void stopDeviceControl(List<BLEItem> bleItems, BLEItem bleItemTarget)
	{
		for (BLEItem bleItem:bleItems)
		{
			if (bleItem.isDeviceControlConnected())
			{
				if (bleItem.getDeviceAddress().equalsIgnoreCase
						(bleItemTarget.getDeviceAddress()))
					return;
			}
		}

		for (int i=0; i<mDeviceControls.size();i++)
		{
			if (mDeviceControls.get(i).getmDeviceAddress().
					equalsIgnoreCase(bleItemTarget.getDeviceAddress()))
			{
				mDeviceControls.get(i).mBluetoothLeDevice.disconnect();
				mDeviceControls.get(i).mBluetoothLeDevice.close();
				mDeviceControls.get(i).mBluetoothLeDevice=null;
				mDeviceControls.remove(i);
			}
		}
	}


}