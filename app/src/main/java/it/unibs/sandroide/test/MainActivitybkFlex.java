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
package it.unibs.sandroide.test;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.alarm.BLEAlarm;
import it.unibs.sandroide.lib.item.button.BLEButton;
import it.unibs.sandroide.lib.item.button.BLEOnClickListener;
import it.unibs.sandroide.lib.item.sensor.BLESensor;
import it.unibs.sandroide.lib.item.sensor.BLESensorEvent;
import it.unibs.sandroide.lib.item.sensor.BLESensorEventListener;
import it.unibs.sandroide.lib.item.sensor.BLESensorManager;


@SuppressLint({ "NewApi", "ServiceCast" })
public class MainActivitybkFlex extends SandroideBaseActivity implements BLESensorEventListener, BLEOnClickListener {
	
	
	protected static final String TAG = "MainActivity";
	TextView tvX;
	TextView tvY;
	TextView tvZ;
	TextView tv_RMS_X;
	TextView tv_RMS_Y;
	TextView tv_RMS_Z;
	TextView tv_RMS_total;
	TextView tvBatt;
	TextView tvBattInt;
	ImageView iv;
	
	BLEButton button;
	BLEButton button2;
	BLEButton button3;
	BLEButton button4;
	BLEButton button5;
	BLEButton button6;
	BLEButton button7;
	BLEButton button8;
	Button alarmButton;
	Button batteryButton;
	BLEAlarm mbleAlarm;
	
	int indexButton1=0;
	int indexButton2=0;
	
	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private BLESensorManager mSensorManager;
    private BLESensor mAccelerometer;
	private BLESensor mThermometer;
    final private int AVERAGE_WINDOW_S = 10;
    final private int MAX_ACC_SAMPLE_RATE_US = 20000;
    final private int MAX_TIME_ACC_FILE_STORAGE_MS = 3600000;
    final private long AVERAGE_WINDOW_NS = AVERAGE_WINDOW_S*1000000000L;
    private final float NOISE = (float) 0.2;
    float[] x_buf=new float[MAX_ACC_SAMPLE_RATE_US*AVERAGE_WINDOW_S];
    float[] y_buf=new float[MAX_ACC_SAMPLE_RATE_US*AVERAGE_WINDOW_S];
    float[] z_buf=new float[MAX_ACC_SAMPLE_RATE_US*AVERAGE_WINDOW_S];
    long[] time_buf=new long[MAX_ACC_SAMPLE_RATE_US*AVERAGE_WINDOW_S];
    int write_index=0;
    private long mLastTime;
    
    String currentDateandTime;
    Context context;
    
    private final boolean accelerometerOn=true;
    private final boolean alarmOn=false;
    private final boolean buttonOn=true;
    private final boolean batteryOn=false;
	private final boolean temperatureOn=true;
	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		tvX= (TextView)findViewById(R.id.x_axis);
		tvY= (TextView)findViewById(R.id.y_axis);
		tvZ= (TextView)findViewById(R.id.z_axis);
		tv_RMS_X= (TextView)findViewById(R.id.RMS_x_axis);
		tv_RMS_Y= (TextView)findViewById(R.id.RMS_y_axis);
		tv_RMS_Z= (TextView)findViewById(R.id.RMS_z_axis);
		tv_RMS_total= (TextView)findViewById(R.id.RMS_total);
		tvBatt= (TextView)findViewById(R.id.battery_value);
		tvBattInt= (TextView)findViewById(R.id.battery_value_intent);
		iv = (ImageView)findViewById(R.id.image);
		BLEContext.initBLE(this);
        mInitialized = false;
        
        context=this;
        if (accelerometerOn)
        {
	    mSensorManager = (BLESensorManager) BLEContext.
	    		getSystemService(BLEContext.SENSOR_SERVICE);
//	    mAccelerometer = mSensorManager.getDefaultSensor
//	    		(BLESensor.TYPE_ACCELEROMETER, "des_key_accelerometer");
			mAccelerometer = mSensorManager.getDefaultSensor
					(BLESensor.TYPE_ACCELEROMETER, "tre_nrg_accelerometer");
        }
		if (accelerometerOn)
			mSensorManager.registerListener
					(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		if (temperatureOn){
			mThermometer = mSensorManager.getDefaultSensor
					(BLESensor.TYPE_TEMPERATURE, "tre_nrg_thermometer");
			mSensorManager.registerListener
					(this, mThermometer, SensorManager.SENSOR_DELAY_NORMAL);}

		if (buttonOn){
			BLEButton button1 = (BLEButton) BLEContext.findViewById("des_key_button1");
			button1.setOnClickListener(new BLEOnClickListener() {
				@Override
				public void onClick(BLEItem arg0) {
					Log.d(TAG, "clicked button 1");
				}
			});
			BLEButton button2 = (BLEButton) BLEContext.findViewById("des_key_button2");
			button2.setOnClickListener(new BLEOnClickListener() {
				@Override
				public void onClick(BLEItem arg0) {
					Log.d(TAG, "clicked button 2");
				}
			});

		}

    }

//    protected void onResume() {
//        super.onResume();
//        if (accelerometerOn)
//        	mSensorManager.registerListener
//        	(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//		if (temperatureOn)
//			mSensorManager.registerListener
//					(this, mThermometer, SensorManager.SENSOR_DELAY_NORMAL);
//
//    }

//    protected void onPause() {
//        super.onPause();
//      if(accelerometerOn)
//        mSensorManager.unregisterListener(this);
//    }

	@Override
	public void onAccuracyChanged(BLESensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(BLESensorEvent event) {

		long time;
		switch (event.sensor.getSensorType())
		{
			case BLESensor.TYPE_ACCELEROMETER:
				final float x = event.values[0];
				final float y = event.values[1];
				final float z = event.values[2];
				time= event.timestamp;
				x_buf[write_index] = x;
				y_buf[write_index] = y;
				z_buf[write_index] = z;
				time_buf[write_index] = time;


				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tvX.setText(Float.toString(x));
						tvY.setText(Float.toString(y));
						tvZ.setText(Float.toString(z));}
				});

				break;

			case BLESensor.TYPE_TEMPERATURE:
				final float temp = event.values[0];
				((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvBatt.setText(Float.toString(temp));
				}
		});
				break;

		}
	}

	@Override
	public void onClick(BLEItem arg0) {
		Log.d(TAG, "clicked button... general");
		
	}

	
}