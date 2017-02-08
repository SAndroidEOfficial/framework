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

import java.util.ArrayList;
import java.util.List;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.BLEEmbeddedEvent;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.Bleresource;

/**
 * <p>
 * BLESensorManager lets you access the device's {@link BLESensor}. Get an instance of this class by calling
 * {@link BLEContext#getSystemService(java.lang.String)
 * Context.getSystemService()} with the argument
 * {@link android.content.Context#SENSOR_SERVICE}.
 * </p>
 * <p>
 * Always make sure to disable sensors you don't need, especially when your
 * activity is paused. Failing to do so can drain the battery in just a few
 * hours. Note that the system will <i>not</i> disable sensors automatically when
 * the screen turns off.
 * </p>
 * <pre class="prettyprint">
 * public class SensorActivity extends Activity, implements SensorEventListener {
 *     private final BLESensorManager mSensorManager;
 *     private final BLESensor mAccelerometer;
 *
 *     public SensorActivity() {
 *         mSensorManager = (BLESensorManager)getSystemService(BLEContext.SENSOR_SERVICE);
 *         mAccelerometer = mSensorManager.getDefaultSensor(BLESensor.TYPE_ACCELEROMETER);
 *     }
 *
 *     protected void onResume() {
 *         super.onResume();
 *         mSensorManager.registerListener(this, mAccelerometer, BLESensorManager.SENSOR_DELAY_NORMAL);
 *     }
 *
 *     protected void onPause() {
 *         super.onPause();
 *         mSensorManager.unregisterListener(this);
 *     }
 *
 *     public void onAccuracyChanged(BLESensor sensor, int accuracy) {
 *     }
 *
 *     public void onSensorChanged(BLESensorEvent event) {
 *     }
 * }
 * </pre>
 *
 *
 */
public class BLESensorManager {

	private static final String TAG = "BLESensorManager";
	static List<BLESensor> mSensors = new ArrayList<BLESensor>();
	
	public BLESensorManager() {
	}

	
	public BLESensor getDefaultSensor(int sensorType, String resStr)
	{
		Bleresource bleresource = BLEContext.getBleResource(resStr);
		if (bleresource!=null)
		{
			return getDefaultSensor(sensorType, bleresource);
		}
		else
		{
			BLEContext.triggerBleEmbeddedEventListener(BLEEmbeddedEvent.RESOURCE_DESCRIPTION_NOT_FOUND);
			return null;
		}
	}
	
	public BLESensor getDefaultSensor(int sensorType, Bleresource bleresource)
	{
		BLESensor mbleSensor=null;
		if (bleresource!=null)
		{
			switch (sensorType)
			{
				case BLESensor.TYPE_ACCELEROMETER:

					mbleSensor=(BLESensor) BLEContext.
							getItem(BLEItem.TYPE_SENSOR_ACCELEROMETER
									, bleresource.getDevtype(), bleresource);
					mSensors.add(mbleSensor);
					break;

				case BLESensor.TYPE_TEMPERATURE:

					mbleSensor=(BLESensor) BLEContext.
							getItem(BLEItem.TYPE_SENSOR_TEMPERATURE
									, bleresource.getDevtype(), bleresource);
					mSensors.add(mbleSensor);
					break;

				case BLESensor.TYPE_GENERIC:

					mbleSensor=(BLESensor) BLEContext.
							getItem(BLEItem.TYPE_SENSOR_GENERIC
									, bleresource.getDevtype(), bleresource);
					mSensors.add(mbleSensor);
					break;


				default:
					break;
			}
		}

		return mbleSensor;		
	}
	/**
	 * Registers a {@link BLESensorEventListener BLESensorEventListener} for the given
	 * sensor at the given sampling frequency.
	 * <p>
	 * The events will be delivered to the provided {@code BLESensorEventListener} as soon as they are
	 * available.
	 * </p>
	 * <p>
	 * Applications must unregister their {@code SensorEventListener}s in their activity's
	 * {@code onPause()} method to avoid consuming power
	 * while the device is inactive.
	 * </p>
	 *
	 * @param listener A {@link BLESensorEventListener BLESensorEventListener} object.
	 * @param sensor The {@link BLESensor Sensor} to register to.
	 * @param samplingPeriodUs The rate {@link BLESensorEvent sensor events} are
	 *            delivered at. This is only a hint to the system. Events may be received faster or
	 *            slower than the specified rate.
	 * @return <code>true</code> if the sensor is supported and successfully enabled.
	 * @see #unregisterListener(BLESensorEventListener)
	 */
	public boolean registerListener
	(BLESensorEventListener listener, BLESensor sensor, int samplingPeriodUs)
	{
		if (sensor!=null)
		{
			boolean ret=false;
			if (sensor.setBLESensorEventListener(listener)){
				if (sensor.setSampleRate(samplingPeriodUs))
					ret=true;
				else
					sensor.resetBLESensorEventListener();
				}
			else
				sensor.resetBLESensorEventListener();
			return ret;
		}
		else
			return false;
	}

	/**
	 * Unregisters a listener for all sensors.
	 *
	 * @param listener
	 *        a BLESensorListener object
	 *
	 * @see #registerListener(BLESensorEventListener, BLESensor, int)
	 *
	 */
	//TODO:sistemare l'unregister
	public void unregisterListener(BLESensorEventListener listener)
	{
		Log.d(TAG, "unregisterListener");
		for (BLESensor s : mSensors)
		{
			if (s.getBLEsel()==listener)
				s.resetBLESensorEventListener();
		}
		//mSensors=new ArrayList<BLESensor>();
	}

	
}
