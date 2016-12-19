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
package eu.angel.bleembedded.lib;

import android.annotation.SuppressLint;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;


import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.device.DevicesManager;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.BLEItemDescriptor;
import eu.angel.bleembedded.lib.item.BleResourcesHandler;
import eu.angel.bleembedded.lib.item.Bleresource;
import eu.angel.bleembedded.lib.item.alarm.BLEAlarm;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIO;
import eu.angel.bleembedded.lib.item.sensor.BLESensor;
import eu.angel.bleembedded.lib.item.sensor.BLESensorManager;

/**
 * Class which exposes resources of the library.
 */
@SuppressLint("ServiceCast")
public class BLEContext {




	private static List<BLEItem> mbleItems=new ArrayList<>();


	public static List<BLEItem> cloneMbleItems() {
		return new ArrayList<BLEItem>(mbleItems);
	}

	static BLESensorManager mBLESensorManager;
	
    
    private static final String DEFAULT_RESOURCE_FILE= "res.txt";
    public static final String DEFAULT_RESOURCES_FILE_PATH =
			Environment.getExternalStorageDirectory().toString()+"/"+DEFAULT_RESOURCE_FILE;
	
	
	public static final int BLE_RESOURCE_FIELD = 0;
	public static final int BLE_DEV_ADDRESS_FIELD = 1;
	public static final int BLE_DEV_NAME = 2;


	public final static String SENSOR_SERVICE = "sensor";
	public final static String ALARM_SERVICE = "alarm";
	public final static int NONE = 0;
	public final static int BUTTON_1 = 1;


	private static final String TAG = "BLEContext";
	public static Context context;
	private static BLEEmbeddedEventListener bleEmbeddedEventListener;


	/**
	 * Return the Object representing the resource pointed by the id. It is used for {@link BLEButton}
	 * and {@link BLEGeneralIO}.
	 *
	 * @param resId the id of the resource. The String has to be exactly the same reported on the xml file
	 *
	 */
	public static Object findViewById(String resId)
	{
		Bleresource bleresource =getBleResource(resId);
		return findViewById(bleresource);
	}

	/**
	 * Return the Object representing the resource pointed by the id. It is used for {@link BLEButton}
	 * and {@link BLEGeneralIO}.
	 *
	 * @param bleresource the {@link Bleresource}.
	 *
	 */
	public static Object findViewById(Bleresource bleresource)
	{
		if (bleresource!=null)
		{
			switch (bleresource.getType())
			{
				case BLEItemDescriptor.ble_button:
					return getItem(BLEItem.TYPE_BUTTON,
							bleresource.getDevtype(), bleresource.getCardinality(), bleresource);

				case BLEItemDescriptor.ble_generalIO:
					return getItem(BLEItem.TYPE_GENERALIO,
							bleresource.getDevtype(), bleresource.getCardinality(), bleresource);

				default:
					return null;
			}
		}
		else
			return null;

	}

	/**
	 * Return the handle to a SAndroidE library system-level service by name.
	 * The class of the returned object varies by the requested name.
	 * Currently available names are:
	 * <dl>
	 *  <dt> {@link #SENSOR_SERVICE} ("sensor")
	 *  <dd> The {@link BLESensorManager} for handling the sensor resources of the remote devices
	 *  windows.  The returned object is a {@link android.view.WindowManager}.
	 *  <dt> {@link #ALARM_SERVICE} ("alarm")
	 *  <dd> A {@link BLEAlarm} for interacting with the alarm hardware of the remote device.
	 *  <dd> {@link #ALARM_SERVICE} requires a second parameter to point to the selected resources
	 * </dl>
	 *
	 * @param service
	 * @param bleresource
	 * @return
	 */
	public static Object getSystemService(String service, Bleresource bleresource)
	{
		Object ret=null;
		switch(service)
		{
			case SENSOR_SERVICE:
				if (mBLESensorManager==null)
				{
					mBLESensorManager=new BLESensorManager();
					ret=mBLESensorManager;
				}
				else
					ret=mBLESensorManager;
				break;

			case ALARM_SERVICE:
				if (bleresource!=null)
				return getItem(BLEItem.TYPE_ALARM,
						bleresource.getDevtype(), bleresource);
				else
				return null;

			default:
				ret= null;
		}
		return ret;
	}

	/**
	 * Return the handle to a SAndroidE library system-level service by name.
	 * The class of the returned object varies by the requested name.
	 * Currently available names are:
	 * <dl>
	 *  <dt> {@link #SENSOR_SERVICE} ("sensor")
	 *  <dd> The {@link BLESensorManager} for handling the sensor resources of the remote devices
	 *  windows.  The returned object is a {@link android.view.WindowManager}.
	 *  <dt> {@link #ALARM_SERVICE} ("alarm")
	 *  <dd> A {@link BLEAlarm} for interacting with the alarm hardware of the remote device.
	 *  <dd> {@link #ALARM_SERVICE} requires a second parameter to point to the selected resources
	 * </dl>
	 *
	 * @param service
	 * @param resourceName
	 * @return
	 */
	public static Object getSystemService(String service, String resourceName)
	{
		Bleresource bleresource =getBleResource(resourceName);
		return getSystemService(service, bleresource);
	}


	/**
	 * Return the handle to a SAndroidE library system-level service by name.
	 * The class of the returned object varies by the requested name.
	 * Currently available names are:
	 * <dl>
	 *  <dt> {@link #SENSOR_SERVICE} ("sensor")
	 *  <dd> The {@link BLESensorManager} for handling the sensor resources of the remote devices
	 *  windows.  The returned object is a {@link android.view.WindowManager}.
	 *  <dt> {@link #ALARM_SERVICE} ("alarm")
	 *  <dd> A {@link BLEAlarm} for interacting with the alarm hardware of the remote device.
	 *  <dd> {@link #ALARM_SERVICE} requires a second parameter to point to the selected resources
	 * </dl>
	 *
	 * @param service
	 * @return
	 */
	public static Object getSystemService(String service)
	{
		return getSystemService(service, (Bleresource) null);
	}


	/**
	 * Initialization of the library.
	 *
	 * @param context needed to handle the Android resources such as Bluetooth, storage ...
	 */
	public static void initBLE(Context context)
	{
		BLEContext.context=context;
	}

	public static void releaseBLE()
	{
		DevicesManager.removeAllDevices();
		BLEContext.context=null;
	}


	/**
	 * Get the item related to the required resource.
	 * @param type define the type of the {@link BLEItem}
	 * @param deviceName the name of the device
	 * @param whichOne the cardinality of the resource
	 * @param bleresource the {@link Bleresource} required
	 */
	public static BLEItem getItem(int type, String deviceName,
								  int whichOne, Bleresource bleresource)
	{
		BLEItem mBleItem=null;
		switch(type)
		{
			case BLEItem.TYPE_SENSOR_ACCELEROMETER:
				Log.d(TAG, "il num del type acc  : " + type);
				mBleItem= new BLESensor(BLESensor.TYPE_ACCELEROMETER,
						deviceName, bleresource);
				break;

			case BLEItem.TYPE_SENSOR_TEMPERATURE:
			Log.d(TAG, "il num del type acc  : " + type);
			mBleItem= new BLESensor(BLESensor.TYPE_TEMPERATURE,
					deviceName, bleresource);
			break;

			case BLEItem.TYPE_SENSOR_GENERIC:
				Log.d(TAG, "il num del type gen  : " + type);
				mBleItem= new BLESensor(BLESensor.TYPE_GENERIC,
						deviceName, bleresource);
				break;

			case BLEItem.TYPE_BUTTON:
				Log.d(TAG, "il num del type button  : " + type);
				mBleItem= new BLEButton(deviceName,
						whichOne, bleresource);
				break;

			case BLEItem.TYPE_ALARM:
				Log.d(TAG, "il num del type alarm  : " + type);
				mBleItem= new BLEAlarm(deviceName, bleresource);
				break;

			case BLEItem.TYPE_GENERALIO:
				Log.d(TAG, "il num del type generalIO  : " + type);
				mBleItem = new BLEGeneralIO(deviceName, bleresource);
				break;
		}

		addBLEItem(mBleItem);
		DevicesManager.connectDevice(mBleItem, type);

		return mBleItem;
	}

	/**
	 * Get the item related to the required resource.
	 * @param type define the type of the {@link BLEItem}
	 * @param deviceName the name of the device
	 * @param bleresource the {@link Bleresource} required
	 */
	public static BLEItem getItem(int type, String deviceName, Bleresource bleresource)
	{
		return getItem(type, deviceName, 1, bleresource);
	}

	/**
	 * Get the resource by the required Item.
	 * @param ItemName the of the item
	 */
	public static Bleresource getBleResource(String ItemName)
	{
		return BleResourcesHandler.getItemDescriptorFromBleresources(context, ItemName);
	}


//	public void unregisterReceiver(BLEBroadcastReceiver bleBroadcastReceiver)
//	{
//		for (int i=0; i<mBLERegisteredReceivers.size();i++)
//		{
//			if (bleBroadcastReceiver==mBLERegisteredReceivers.get(i))
//			{
//				for (BLEIntentFilter filter:mBLERegisteredReceivers.get(i).bleIntentFilters)
//				{
//					switch(filter.getAction())
//					{
//						case BLEIntent.ACTION_BATTERY_CHANGED:
//							BLEBatteryManager.deleteBattery(filter);
//							break;
//					}
//				}
//				mBLERegisteredReceivers.remove(i);
//			}
//		}
//	}


	//region BLEEmbeddedEvent listener section
	/**
	 * Sets the bleEmbeddedEventListener used for point out events of the library.
	 * @param bleEmbeddedEventListener {@link BLEEmbeddedEventListener}
	 */
	public static void setBleEmbeddedEventListener
			(BLEEmbeddedEventListener bleEmbeddedEventListener)
	{
		BLEContext.bleEmbeddedEventListener=bleEmbeddedEventListener;
	}

	/**
	 * triggers the bleEmbeddedEventListener onBLEEmbeddedEvent callback.
	 * @param event number which identifies the event type
	 */
	public static void triggerBleEmbeddedEventListener(int event)
	{
		if (bleEmbeddedEventListener!=null)
			bleEmbeddedEventListener.onBLEEmbeddedEvent(event);
	}

	/**
	 * triggers the bleEmbeddedEventListener onBLEItemDisconnected callback.
	 * @param item the {@link BLEItem} disconnected
	 */
	public static void triggerBleEmbeddedEventListenerDisconnection(BLEItem item)
	{
		if (bleEmbeddedEventListener!=null)
			bleEmbeddedEventListener.onBLEItemDisconnected(item);
	}

	/**
	 * triggers the bleEmbeddedEventListener onBLEItemConnected callback.
	 * @param item the {@link BLEItem} disconnected
	 */
	public static void triggerBleEmbeddedEventListenerConnection(BLEItem item)
	{
		if (bleEmbeddedEventListener!=null)
			bleEmbeddedEventListener.onBLEItemConnected(item);
	}
	//endregion

	//region mbleItems handling
	//----------------------------------------------------------------------------------------------
	private static void addBLEItem(BLEItem bleItem)
	{
		mbleItems.add(bleItem);
	}

	public static void removeBLEItem(BLEItem bleItem)
	{
		DevicesManager.stopDeviceControl(mbleItems, bleItem);
		for (int i=0; i<mbleItems.size();i++)
		{
			if (mbleItems.get(i)==bleItem)
				mbleItems.remove(i);
		}
	}
	//----------------------------------------------------------------------------------------------
	//endregion


	//region Activity life related Callbacks.
	// These methods shall be placed in the relative Activity life callbacks
	/**
	 * This method shall to be inserted in the onResume() callback of the Activity
	 * to allow the proper handling of the resources
	 * @return
	 */
	public static void onResume()
	{
	}

	/**
	 * This method shall to be inserted in the onDestroy() callback of the Activity
	 * to allow the  proper handling of the resources
	 */
	public static void onDestroy()
	{
		BLEBeaconManager.onDestroy();
	}
	//endregion
}
