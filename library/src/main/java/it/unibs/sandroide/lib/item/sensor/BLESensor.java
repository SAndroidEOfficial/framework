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
package it.unibs.sandroide.lib.item.sensor;

import android.util.Log;

import it.unibs.sandroide.lib.data.BLEDeviceData;
import it.unibs.sandroide.lib.data.ParsingComplements;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.BLEOnItemUpdateListener;
import it.unibs.sandroide.lib.item.Bleresource;

/**
 * The class which implements the BLESensor resources.
 */
public class BLESensor extends BLEItem {

	private static final String TAG="BLESensor";
	public static final int TYPE_NONE=0;
	//type of the sensor
	public static final int TYPE_ACCELEROMETER=1;
	public static final int TYPE_TEMPERATURE=2;
	public static final int TYPE_GENERIC=3;

	public static final int ACCELEROMETER_AXIS_X=0;
	public static final int ACCELEROMETER_AXIS_Y=1;
	public static final int ACCELEROMETER_AXIS_Z=2;


	private BLESensorEventListener BLEsel;

	public BLESensorEventListener getBLEsel() {
		return BLEsel;
	}

	private int sampleRate;
	private int sensorType;
	private String mSensorAddress;
	
	private boolean bLESensorEventListenerSet=false;



	public String getAddress() {
			return mSensorAddress;
		}


	public void setAddress(String address) {
		this.mSensorAddress = address;
	}


	/**
	 * Constructor of the {@link BLESensor} class.
	 * @param sensorType type of the sensor.
	 * @param devName the name of the device to connect with (the name identifies exactly the
	 *                   remote device anf the firmware version).
	 * @param bleresource {@link Bleresource} which defines the Item.
	 */
	//TODO: se type è diverso da quello definito dal descrittore non ritornare nulla
	public BLESensor(final int sensorType, String devName, Bleresource bleresource) {
		super(BLEItem.TYPE_NONE, devName, bleresource);
		//FIXME: il sensortype non dovrebbe essere un attributo dell'Item, ma del Sensor, vedere se è possibile eliminare la parte Item
		switch (sensorType)
		{
			case TYPE_ACCELEROMETER:
				setType(BLEItem.TYPE_SENSOR_ACCELEROMETER);
				break;

			case TYPE_TEMPERATURE:
				setType(BLEItem.TYPE_SENSOR_TEMPERATURE);
				break;

			case TYPE_GENERIC:
				setType(BLEItem.TYPE_SENSOR_GENERIC);
				break;
			
			default:
				setType(TYPE_NONE);
				break;
		}
		this.sensorType=sensorType;
	}
	
	public int getSensorType()
	{
		return sensorType;
	}

	/**
	 * SEts the listener for the event of the sensor returned through the callbacks
	 * {@link BLESensorEventListener#onSensorChanged} and {@link BLESensorEventListener#onAccuracyChanged}
	 * of the interface {@link BLESensorEventListener} to the Application.
	 * @param bleSensorEventListener of the Item.
	 * @return ret
	 */
	public boolean setBLESensorEventListener(BLESensorEventListener bleSensorEventListener)
	{
		Log.d(TAG, "setBLESensorEventListener");
		boolean ret;
		bLESensorEventListenerSet=true;
		try {
			BLEsel = bleSensorEventListener;
			ret=true;
		} catch (Exception e) {
			ret=false;
			e.printStackTrace();
		}
		if (mDeviceControl!=null)
			initDeviceControl();
			//mDeviceControl.addBLEEvent(new BLEEvent
			//	(this.getType(), true, (byte) 0, EVENT_START_ACCELEROMETER_NOTIFICATION));
			//mDeviceControl.startBLEService(SampleGattAttributes.EMULATED_ACCELAROMETER_STRING, true);
		return ret;
	}

	/**
	 * Resets the listener for the event of the sensor.
	 */
	public void resetBLESensorEventListener()
	{
		Log.d(TAG, "resetBLESensorEventListener");
		if (mDeviceControl!=null)
			stopDeviceControlNotification();
		bLESensorEventListenerSet=false;
			//mDeviceControl.startBLEService(SampleGattAttributes.EMULATED_ACCELAROMETER_STRING, false);
		//bLESensorEventListenerSet=false;
		//BLEsel=null;
	}

	/**
	 * SEts the samplerate of the sensor (it does not work).
	 */
	//TODO: make it real
	public boolean setSampleRate(int sampleRate)
	{
		boolean ret;
		switch (sensorType)
		{
			case TYPE_GENERIC:
			case TYPE_TEMPERATURE:
			case TYPE_ACCELEROMETER:
				this.sampleRate=sampleRate;
				ret=true;
				break;
			
			default:
				ret=false;
				break;
		}
		
		return ret;
	}
	
	public int getSampleRate()
	{
		return sampleRate;
	}

	/**
	 * Overrides the initialization of the {@link it.unibs.sandroide.lib.device.DeviceControl}.
	 * The initialization is tailored based on the type of the {@link BLESensor}. Up to now the sensor
	 * available are accelerometer, temperature and generic. The main difference is the number of the
	 * data (3 axes for the accelerometer, 1 for the temperature and generic)
	 */
	@Override
	public void initDeviceControl() {

		super.initDeviceControl();
		Log.d(TAG, "initDeviceControl type: "+sensorType);

		switch (sensorType)
		{
			case TYPE_ACCELEROMETER:

				initItem();
				setBleItemListeners(new BLEOnItemUpdateListener() {
					@Override
					public void onItemUpdate(BLEDeviceData[] data) {
						boolean hasDeviceTimestamp=false;

						BLESensorEvent se=new BLESensorEvent(3);
						for (int i=0;i<data.length;i++){
							//TODO: fixed configuration of axes... better for performance... use different type of data (X, Y, z)
							//might be more flexible
							if (data[i].getData_type()==ParsingComplements.DT_SENSOR){
								se.values[ACCELEROMETER_AXIS_X]=data[i+0].getValue();
								se.values[ACCELEROMETER_AXIS_Y]=data[i+1].getValue();
								se.values[ACCELEROMETER_AXIS_Z]=data[i+2].getValue();
								//TODO: solo per testare... trovare una soluzione
								break;
							} else if (data[i].getData_type()==ParsingComplements.DT_TIME_STAMP){
								se.timestamp=(long)data[i].getValue();
								hasDeviceTimestamp=true;
							}
						}
						if (!hasDeviceTimestamp){
							Long tsLong = System.currentTimeMillis()*1000000;
							se.timestamp=tsLong;
						}

						se.sensor=BLESensor.this;
						final BLESensorEvent sefin=se;
						BLEsel.onSensorChanged(sefin);
					}
				}, null);
				break;

			case TYPE_GENERIC:
				setBleItemListeners(new BLEOnItemUpdateListener() {
					@Override
					public void onItemUpdate(BLEDeviceData[] data) {
						boolean hasDeviceTimestamp=false;

						BLESensorEvent se=new BLESensorEvent(data.length);
						for (int i=0;i<data.length;i++){
							//TODO: fixed configuration of axes... better for performance... use different type of data (X, Y, z)
							//might be more flexible
							se.values[i] = data[i].getValue();
						}
						Long tsLong = System.currentTimeMillis()*1000000;
						se.timestamp=System.currentTimeMillis()*1000000;

						se.sensor=BLESensor.this;
						final BLESensorEvent sefin=se;
						BLEsel.onSensorChanged(sefin);
					}
				}, null);
				initItem();
				break;

			case TYPE_TEMPERATURE:
				setBleItemListeners(new BLEOnItemUpdateListener() {
					@Override
					public void onItemUpdate(BLEDeviceData[] data) {
						//TODO: this should be a characteristic of the Item decribed in the xml file
						boolean hasDeviceTimestamp=false;
						for (BLEDeviceData bleDeviceData:data){
							BLESensorEvent se=new BLESensorEvent(1);
							if (bleDeviceData.getData_type()== ParsingComplements.DT_SENSOR){
								se.values[0]=bleDeviceData.getValue();
							} else if (bleDeviceData.getData_type()== ParsingComplements.DT_TIME_STAMP){
								hasDeviceTimestamp=true;
								se.timestamp=(long)bleDeviceData.getValue();
							}
							if (!hasDeviceTimestamp) {
								Long tsLong = System.currentTimeMillis() * 1000000;
								se.timestamp = tsLong;
							}
							se.sensor=BLESensor.this;
							final BLESensorEvent sefin=se;
							BLEsel.onSensorChanged(sefin);
						}
					}
				}, null);

				initItem();
				break;
		}

		
	}

	public void getValue(){
		runAction("single_read");
	}


	//TODO
	private void stopDeviceControlNotification()
	{
		Log.d(TAG, "stopDeviceControl "+sensorType+"; bLESensorEventListenerSet "+bLESensorEventListenerSet);
		switch (sensorType)
		{
			case TYPE_ACCELEROMETER:
//				if (bLESensorEventListenerSet)
//					mDeviceControl.addBLEEvent(new BLEEvent
//							(this.getType(), true, (byte) 0, EVENT_STOP_ACCELEROMETER_NOTIFICATION));
//				mDeviceControl.addBLEEvent(new BLEEvent
//						(this.getType(), true, (byte) 0, EVENT_DISABLE_ACCELEROMETER));
//				mDeviceControl.setbleOnAccelerometerUpdateListener(null);
				break;

			case TYPE_TEMPERATURE:
//				if (bLESensorEventListenerSet)
//					mDeviceControl.addBLEEvent(new BLEEvent
//							(this.getType(), true, (byte) 0, EVENT_STOP_TEMPERATURE_NOTIFICATION));
//				mDeviceControl.setbleOnTemperatureUpdateListener(null);
				break;
		}
	}

	
/////////////////////////////////////
	
}
