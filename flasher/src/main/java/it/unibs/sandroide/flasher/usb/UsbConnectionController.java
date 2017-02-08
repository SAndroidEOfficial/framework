/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
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
package it.unibs.sandroide.flasher.usb;




import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

public class UsbConnectionController {
	
	
	public final static String TAG = "USBController";
	private Context context;

	private static UsbConnectionEventListener usbConnectionEventListener;
	protected static final String ACTION_USB_PERMISSION = "it.unibs.flasher.usb";
	public static List<UsbDevice> UsbDevices= new ArrayList<UsbDevice>();
	private static UsbManager usbManager;
	static UsbConnectionController sInstance;
	private boolean hasPermission=false;
	public UsbDeviceConnection getConn() {
		return conn;
	}
	UsbDeviceConnection conn;
	UsbInterface dev_interface;

	private UsbConnectionController(Context context,
									UsbConnectionEventListener usbConnectionEventListener) {
		this.context = context;
		this.usbConnectionEventListener = usbConnectionEventListener;
		usbManager = (UsbManager) this.context
				.getSystemService(Context.USB_SERVICE);
		init();
	}
	
	public static UsbConnectionController getInstance(Context context, UsbConnectionEventListener usbConnectionEventListener) {

		if (sInstance == null) {
			sInstance = new UsbConnectionController(context, usbConnectionEventListener);
		}
		else
		{
			UsbDevices= new ArrayList<>();
			UsbConnectionController.usbConnectionEventListener = usbConnectionEventListener;
			init();
		}

		return sInstance;
	}

	private static void init() {
		enumerate();
	}

	public void stop()
	{
		try{
			if (conn!=null){
			if (dev_interface!=null)
				conn.releaseInterface(dev_interface);
			conn.close();}
			context.unregisterReceiver(permissionReceiver);
		}catch(IllegalArgumentException e){};//bravo
	}

	permissionDeniedListener listener=new permissionDeniedListener() {
		
		@Override
		public void onPermissionDenied(UsbDevice d) {
			UsbManager usbman = (UsbManager) context
					.getSystemService(Context.USB_SERVICE);
			PendingIntent pi = PendingIntent.getBroadcast(
					context, 0, new Intent(
							ACTION_USB_PERMISSION), 0);
			context.registerReceiver(permissionReceiver,
					new IntentFilter(ACTION_USB_PERMISSION));
			usbman.requestPermission(d, pi);
			
		}
	};
	
	
	private static void enumerate() {
		HashMap<String, UsbDevice> devlist = usbManager.getDeviceList();

		for (String key:devlist.keySet()) {
			UsbDevice d = devlist.get(key);
			Log.d(TAG, "Found device: vendor id: "+ d.getVendorId()+"; product id: "+
					d.getProductId());
			UsbDevices.add(d);
		}
	}
	
	public UsbDeviceConnection connectToDev(UsbDevice d, USB_DeviceID deviceID)
	{
		if (!usbManager.hasPermission(d))
		{
			hasPermission=false;
			listener.onPermissionDenied(d);
			return null;
		}
		else
		{
			hasPermission=true;
			conn = usbManager.openDevice(d);
			dev_interface = d.getInterface(0);
			return conn;
			//return init_device(d, deviceID);
		}
	}
	
	public boolean hasPermission()
	{
		return hasPermission;
	}
	

	private class PermissionReceiver extends BroadcastReceiver {
		private final permissionDeniedListener mPermissionListener;

		public PermissionReceiver(permissionDeniedListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			UsbConnectionController.this.context.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (usbConnectionEventListener !=null) {
							usbConnectionEventListener.onPermissionGranted(dev);
						}
					} else {
						Log.d(TAG, "device not found");
					}
				}
			}
		}

	}

	private BroadcastReceiver permissionReceiver = new PermissionReceiver(
			new permissionDeniedListener() {
				@Override
				public void onPermissionDenied(UsbDevice d) {
					Log.d(TAG, "Permission denied on device: " + d.getDeviceId());
				}
			});

	private static interface permissionDeniedListener {
		void onPermissionDenied(UsbDevice d);
	}
	
	public UsbManager getmUsbManager(){
		return usbManager;
	}

}


