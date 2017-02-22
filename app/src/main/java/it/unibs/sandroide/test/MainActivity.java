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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.generalIO.BLEGeneralIO;
import it.unibs.sandroide.lib.item.generalIO.BLEGeneralIOEvent;
import it.unibs.sandroide.lib.item.generalIO.BLEOnGeneralIOEventListener;
import it.unibs.sandroide.lib.item.sensor.BLESensorManager;
import it.unibs.sandroide.lib.item.alarm.BLEAlarm;
import it.unibs.sandroide.lib.item.button.BLEButton;
import it.unibs.sandroide.lib.item.button.BLEOnClickListener;
import it.unibs.sandroide.lib.item.sensor.BLESensor;
import it.unibs.sandroide.lib.item.sensor.BLESensorEvent;
import it.unibs.sandroide.lib.item.sensor.BLESensorEventListener;
import it.unibs.sandroide.lib.BLEContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import it.unibs.sandroide.R;


@SuppressLint({ "NewApi", "ServiceCast" })
public class MainActivity extends SandroideBaseActivity implements BLESensorEventListener, BLEOnClickListener {
	
	
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

	BLEGeneralIO pin1;
	BLEGeneralIO pin2;
	BLEGeneralIO pin3;
	BLEGeneralIO pin4;
	BLEGeneralIO pin5;
	BLEGeneralIO pin6;

	BLEGeneralIO pin7;

	BLEGeneralIO pin13;

	BLEGeneralIO pin14;
	
	int indexButton1=0;
	int indexButton2=0;
	
	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private BLESensorManager mSensorManager;
    private BLESensor mAccelerometer;
	private BLESensor mThermometer;
	private BLESensor mBattery;
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
    
    private final boolean accelerometerOn=false;
    private final boolean alarmOn=false;
    private final boolean buttonOn=false;
    private final boolean batteryOn=false;
	private final boolean temperatureOn=false;
	private final boolean pin1on=false;
	private final boolean pin2on=false;
	private final boolean pin3on=false;
	private final boolean pin4on=false;
	private final boolean pin5on=false;
	private final boolean pin6on=false;

	private final boolean pin2inout=false;

	private final boolean pin14analog=false;


	private final boolean pin13bisquitLed=true;

	private final boolean pin7bisquitLed=false;

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
	    mAccelerometer = mSensorManager.getDefaultSensor
	    		(BLESensor.TYPE_ACCELEROMETER, "al_nrg_accelerometer");
        }
        if (buttonOn)
        {
        	addListenerOnButton();
	        addListenerOnButton2();
//
//	        addListenerOnButton3();
//	        addListenerOnButton4();
//	        addListenerOnButton5();
//	        addListenerOnButton6();
//	        addListenerOnButton7();
//	        addListenerOnButton8();
        }

		if (pin2on)
		{
			addListenerTopin2();
		}
		if (pin3on)
		{
			addListenerTopin3();
		}
		if (pin4on)
		{
			addListenerTopin4();
		}
		if (pin6on)
		{
			addListenerTopin6();
		}

		if (pin2inout)
		{
			addListenerTopin2();
			//pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);

		}

		if (pin14analog)
		{
			addListenerTopin14();
			//pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);

		}

		if (pin13bisquitLed)
		{
			addListenerTopin13();
			//pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);

		}

		if (pin7bisquitLed)
		{
			addListenerTopin7();
			//pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);

		}



		Timer changeFiletimer = new Timer("changeFiletimer");
        changeFiletimer.scheduleAtFixedRate(
                new TimerTask() {
                        
                        public void run() {
                        	changeFileDate();
                        }
                },
                0, MAX_TIME_ACC_FILE_STORAGE_MS);
        
        if(alarmOn)
        	mbleAlarm=(BLEAlarm)BLEContext.getSystemService
        	(BLEContext.ALARM_SERVICE, "al2_key_alarm");
		if(temperatureOn)
		{
			if (mSensorManager==null)
				mSensorManager = (BLESensorManager) BLEContext.
						getSystemService(BLEContext.SENSOR_SERVICE);
			mThermometer = mSensorManager.getDefaultSensor
					(BLESensor.TYPE_TEMPERATURE, "al_nrg_thermometer");
		}


        alarmButton=(Button) findViewById(R.id.buttonAlarm);
        alarmButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 if (alarmOn)
					 mbleAlarm.alarm(3000);


				if (temperatureOn)
					mSensorManager.unregisterListener(MainActivity.this);

				if (pin2inout)
					pin2.setDigitalValueHigh(true);


				if (pin13bisquitLed)
					pin13.setDigitalValueHigh(true);

				if (pin7bisquitLed)
					pin7.setDigitalValueHigh(true);

//				final MyTask myTask=new MyTask();
//				((Activity) context).runOnUiThread(new Runnable() {
//					@Override
//					public void run()
//					{
//						myTask.execute();
//
//					}
//				});


				
			}
		});
        
        batteryButton=(Button) findViewById(R.id.buttonBattery);
        batteryButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (batteryOn)
				{

					mSensorManager = (BLESensorManager) BLEContext.
							getSystemService(BLEContext.SENSOR_SERVICE);
					mBattery = mSensorManager.getDefaultSensor
							(BLESensor.TYPE_GENERIC, "al_key_battery");

					mSensorManager.registerListener
							(new BLESensorEventListener() {
								@Override
								public void onSensorChanged(BLESensorEvent event) {
									Log.d(TAG, "batteryyyyyyyyyy");
								}

								@Override
								public void onAccuracyChanged(BLESensor sensor, int accuracy) {

								}
							}, mBattery, SensorManager.SENSOR_DELAY_NORMAL);

					mBattery.getValue();
//					BLEIntentFilter ifilter = new BLEIntentFilter
//							(BLEIntent.ACTION_SINGLE_BATTERY_CHANGED, "al_key_battery");
//				    BLEContext.registerReceiver
//				    		(bleBroadcastReceiver, ifilter);
				    /*
				   int level = batteryStatus.getIntExtra(BLEBatteryManager.EXTRA_LEVEL, -1);
				    int scale = batteryStatus.getIntExtra(BLEBatteryManager.EXTRA_SCALE, -1);
				    final float batteryPct = level / (float)scale;
		            ((Activity) context).runOnUiThread(new Runnable() {
		                @Override
		                public void run() {
		                	tvBattInt.setText(Float.toString(batteryPct));}
		            });*/
				}

				if (temperatureOn)
					mSensorManager.registerListener
							(MainActivity.this, mThermometer, 3000);

				if (pin2inout)
					pin2.setDigitalValueHigh(false);

				if (pin13bisquitLed)
					pin13.setDigitalValueHigh(false);

				if (pin7bisquitLed)
					pin7.setDigitalValueHigh(false);


			}
		});
        
        ((Button) findViewById(R.id.buttonBatteryBroadcast)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (batteryOn)
				{
//					BLEIntentFilter ifilter = new BLEIntentFilter
//							(BLEIntent.ACTION_BATTERY_CHANGED, "al_key_battery");
//				    BLEIntent batteryStatus = BLEContext.registerReceiver
//				    		(bleBroadcastReceiver, ifilter);
//				   int level = batteryStatus.getIntExtra(BLEBatteryManager.EXTRA_LEVEL, -1);
//				    int scale = batteryStatus.getIntExtra(BLEBatteryManager.EXTRA_SCALE, -1);
//				    final float batteryPct = level / (float)scale;
//		            ((Activity) context).runOnUiThread(new Runnable() {
//		                @Override
//		                public void run() {
//		                	tvBattInt.setText(Float.toString(batteryPct));}
//		            });

				}
				if (temperatureOn)
					mThermometer.remove();
				if (pin2inout)
				{
					if (pin2.getStatus()==BLEGeneralIO.GENERAL_IO_DO)
						pin2.setStatus(BLEGeneralIO.GENERAL_IO_DI);
					else
						pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);
				}

				if (pin14analog)
				{
					if (pin14.getStatus()==BLEGeneralIO.GENERAL_IO_DI)
						pin14.setStatus(BLEGeneralIO.GENERAL_IO_AI);
					else
						pin14.setStatus(BLEGeneralIO.GENERAL_IO_DI);
				}
				
			}
		});
        
//	    Handler mHandler = new Handler();
//	    mHandler.postDelayed(new Runnable(){
//
//			@Override
//			public void run() {
//		        addListenerOnButton3();
//	        addListenerOnButton4();
//		        addListenerOnButton5();
//		        addListenerOnButton6();
//				((Activity) context).runOnUiThread(new Runnable() {
//					@Override
//					public void run() 
//					{	
//						Toast.makeText(
//								context,
//								"button2 handler ran", Toast.LENGTH_SHORT)
//								.show();			
//					}
//				});			
//				
//			}}, 30000);
    }
    
//    BLEBroadcastReceiver bleBroadcastReceiver=new BLEBroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, BLEIntent intent) {
//		    int level = intent.getIntExtra(BLEBatteryManager.EXTRA_LEVEL, -1);
//		    int scale = intent.getIntExtra(BLEBatteryManager.EXTRA_SCALE, -1);
//		    final float batteryPct = level / (float)scale;
//            ((Activity) context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                	tvBatt.setText(Float.toString(batteryPct));}
//            });
//
//		}
//	};
    
    
    void changeFileDate()
    {
    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	 currentDateandTime = sdf.format(new Date());    	
    }

    protected void onResume() {
        super.onResume();
        if (accelerometerOn)
        	mSensorManager.registerListener
        	(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		if (temperatureOn)
			mSensorManager.registerListener
					(this, mThermometer, 3000);

    }

    protected void onPause() {
        super.onPause();
      if(accelerometerOn)
        mSensorManager.unregisterListener(this);
    }

	@Override
	public void onAccuracyChanged(BLESensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(BLESensorEvent event) {
		Log.d(TAG, "sensor changed...");

		long time;
		switch (event.sensor.getSensorType())
		{
			case BLESensor.TYPE_ACCELEROMETER:
				final float x = event.values[0];
				final float y = event.values[1];
				final float z = event.values[2];

				Log.d(TAG, "x: "+ x);
				Log.d(TAG, "y: "+ y);
				Log.d(TAG, "z: "+ z);
				time= event.timestamp;
				x_buf[write_index] = x;
				y_buf[write_index] = y;
				z_buf[write_index] = z;
				time_buf[write_index] = time;

				if (write_index==0)
				{
					mLastTime=time;
					//mInitialized = true; viene settato a true nella gestione refresh layout
					write_index++;
				}
				else
				{
					write_index++;
					int l=write_index+1;
					if ((time-mLastTime)>AVERAGE_WINDOW_NS)
					{
						double RMSX2=0, RMSY2=0, RMSZ2=0, tot, RMStot2=0;
						for (int i=0; i<l-1;i++)
						{
							double dTimeS= ((((double) time_buf[i+1])-((double)time_buf[i]))/1000000000);
							RMSX2+=((x_buf[i]*x_buf[i])*dTimeS);
							RMSY2+=((y_buf[i]*y_buf[i])*dTimeS);
							RMSZ2+=((z_buf[i]*z_buf[i])*dTimeS);
							tot=(x_buf[i]+y_buf[i]+z_buf[i]);
							RMStot2+=(tot*tot)*dTimeS;
						}

						double tTime =  ((((double)time_buf[l-1])-((double)time_buf[0]))/1000000000);
						final double normRMSX2=RMSX2/tTime;
						final double normRMSY2=RMSY2/tTime;
						final double normRMSZ2=RMSZ2/tTime;
						final double normRMStot2=RMStot2/tTime;

						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								tv_RMS_X.setText(Double.toString(normRMSX2));
								tv_RMS_Y.setText(Double.toString(normRMSY2));
								tv_RMS_Z.setText(Double.toString(normRMSZ2));
								tv_RMS_total.setText(Double.toString(normRMStot2));
							}
						});


						//final File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/acc_save_"+currentDateandTime);

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						String currentDT = sdf.format(new Date());

						final String	toWrite="\r\n\r\n--RECORD: ended at "+currentDT+" recorded for "+Integer.toString(AVERAGE_WINDOW_S)+" seconds--\r\n"+"RMS X axes:"+" "+ Double.toString(normRMSX2)+"\r\n"+
								"RMS Y axes:"+" "+ Double.toString(normRMSY2)+"\r\n"+"RMS Z axes:"+" "+ Double.toString(normRMSY2)+"\r\n"+
								"RMS total:"+" "+ Double.toString(normRMStot2)+"\r\n"+"\r\n";
	           /* ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                	writeFile(f, toWrite);}
	            });*/

						write_index=0;
					}
				}



				//gestione refresh layout
				if (!mInitialized) {
					mLastX = x;
					mLastY = y;
					mLastZ = z;
					((Activity) context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvX.setText(Float.toString(x));
							tvY.setText(Float.toString(y));
							tvZ.setText(Float.toString(z));
							}
	            	});

					mInitialized = true;
				} else {
					final float deltaX = Math.abs(mLastX - x);
					final float deltaY = Math.abs(mLastY - y);
					final float deltaZ = Math.abs(mLastZ - z);
					mLastX = x;
					mLastY = y;
					mLastZ = z;

					Log.d(TAG, "dx: "+ deltaX);
					Log.d(TAG, "dy: "+ deltaY);
					Log.d(TAG, "dz: "+ deltaZ);

					((Activity) context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if ((deltaX > NOISE)){
										tvX.setText(Float.toString(x));
									}

							if ((deltaY > NOISE)){
										tvY.setText(Float.toString(y));}

							if ((deltaZ > NOISE)){
										tvZ.setText(Float.toString(z));}

							iv.setVisibility(View.VISIBLE);

							if ((deltaX > deltaY)&&((deltaX > NOISE)||(deltaY > NOISE))) {
										iv.setImageResource(R.drawable.horizontal);

							} else if ((deltaY > deltaX)&&((deltaX > NOISE)||(deltaY > NOISE))) {
										iv.setImageResource(R.drawable.vertical);

							} else {
										iv.setVisibility(View.INVISIBLE);

							}


						}
					});


					if ((deltaX > deltaY)&&((deltaX > NOISE)||(deltaY > NOISE))) {
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								iv.setImageResource(R.drawable.horizontal);
							}
						});

					} else if ((deltaY > deltaX)&&((deltaX > NOISE)||(deltaY > NOISE))) {
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								iv.setImageResource(R.drawable.vertical);
							}
						});

					} else {
						((Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								iv.setVisibility(View.INVISIBLE);
							}
						});

					}
				}
				break;

			case BLESensor.TYPE_TEMPERATURE:
				final float temp = event.values[0];
				time= event.timestamp;

				(MainActivity.this).runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  tvBattInt.setText(Float.toString(temp));}
				  }
				);


				break;
		}
	}
	
    public void writeFile(File f,String evento)
    {
	   	  
	   	  FileWriter fw = null;
	   	  BufferedWriter bw = null;
	   	  try{
	   	    fw = new FileWriter(f, true);
	   	    bw = new BufferedWriter(fw);
	   	    bw.write(evento);
	   	    bw.close();
	   	    fw.close();
	   	    
	   	  } catch (IOException e) {
	   	    e.printStackTrace(); 
	   	  }
	   	 
	   	  
	}

	public void addListenerTopin2()
	{
		pin2 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_2");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		Log.d(TAG, "set pin 2...");
		pin2.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {
				pin2.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 2 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin2.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}

	public void addListenerTopin3()
	{
		pin3 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_3");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		Log.d(TAG, "set pin 3...");
		pin3.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {
				pin3.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 3 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin3.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}

	public void addListenerTopin4()
	{
		pin4 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_4");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		pin4.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {

			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 4 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin4.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}

	public void addListenerTopin6()
	{
		pin6 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_6");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		pin6.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {

			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 6 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin6.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}

	public void addListenerTopin14()
	{
		pin14 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_14");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		pin14.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {
				pin14.setStatus(BLEGeneralIO.GENERAL_IO_AI);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 14 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "analog input 14 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin14.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}


	public void addListenerTopin13()
	{
		pin13 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_19");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		Log.d(TAG, "set pin 13...");
		pin13.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {
				pin13.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 3 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin3.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}


	public void addListenerTopin7()
	{
		pin7 = (BLEGeneralIO) BLEContext.findViewById("bbh_rbs_general_io_7");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		Log.d(TAG, "set pin 7...");
		pin7.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {

			@Override
			public void onBoardInitEnded() {
				pin7.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input 7 changed: "+ bleGeneralIOEvent.values[1]);
			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

			}
		});
		//pin3.setStatus(BLEGeneralIO.GENERAL_IO_DI);
	}


	///////parte BLEButton//////////////
	public void addListenerOnButton() {

		button = (BLEButton) BLEContext.findViewById("al_key_button1");
		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);
		
		button.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 1");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
    ////////////////////////////////////
	
    ///////parte BLEButton//////////////
	public void addListenerOnButton2() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button2 = (BLEButton) BLEContext.findViewById("al_key_button2");
		
		button2.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
						if (pin14analog)
						{
							if (pin14.getStatus()==BLEGeneralIO.GENERAL_IO_DI)
								pin14.setStatus(BLEGeneralIO.GENERAL_IO_AI);
							else
								pin14.setStatus(BLEGeneralIO.GENERAL_IO_DI);
						}
					}
	            });				
			}

		});

	}
	
	
	public void addListenerOnButton3() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button3 = (BLEButton) BLEContext.findViewById("blebut1dev1");
		
		button3.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
	
	public void addListenerOnButton4() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button4 = (BLEButton) BLEContext.findViewById("blebut2dev1");
		
		button4.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
	
	
	public void addListenerOnButton6() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button5 = (BLEButton) BLEContext.findViewById("blebut1dev2");
		
		button5.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
	
	public void addListenerOnButton5() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button6 = (BLEButton) BLEContext.findViewById("blebut2dev2");
		
		button6.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
	
	public void addListenerOnButton7() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button7 = (BLEButton) BLEContext.findViewById("blebut1dev3");
		
		button7.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}
	
	public void addListenerOnButton8() {

		//button = (BLEButton) BLEContext.findViewById(R.array.dev0_button1);

		button8 = (BLEButton) BLEContext.findViewById("blebut2dev3");
		
		button8.setOnClickListener(new BLEOnClickListener() {

			@Override
			public void onClick(BLEItem arg0) {
				Log.d(TAG, "clicked button 2");
	            ((Activity) context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	    				if (alarmButton.isEnabled())
	    					alarmButton.setEnabled(false);
	    				else
	    					alarmButton.setEnabled(true);}
	            });				
			}

		});

	}


    ////////////////////////////////////

	@Override
	public void onClick(BLEItem arg0) {
		Log.d(TAG, "clicked button... general");
		
	}

	
//	
//	BLEAlarm alarm;
//	BLEButton button;
//	
//    ///////Button//////////////
//	public void addListenerOnButton() {
//
//		alarm = (BLEAlarm) BLEContext.getSystemService
//				(BLEContext.ALARM, "bleAlarmDev2");
//    	
//		button = (BLEButton) BLEContext.findViewById("bleButton1Dev0");
//		
//		button.setOnClickListener(new BLEOnClickListener() {
//
//			@Override
//			public void onClick(BLEItem v) {
//				Log.d(TAG, "clicked button");
//	            ((Activity) context).runOnUiThread(new Runnable() {
//	                @Override
//	                public void run() {
//	                	alarm.alarm(3000);}
//	            });				
//			}
//
//		});
//
//	}
//    ////////////////////////////////////

//	private class MyTask extends AsyncTask<String, Integer, String> {
//
//		@Override
//		protected String doInBackground(String... params) {
//
//			Uri uri = Uri.parse("content://it.unibs.sandroide.flasher.fileprovider/bleresources.xml");
//			InputStream is = null;
//			StringBuilder result = new StringBuilder();
//			try {
//				is = getApplicationContext().getContentResolver().openInputStream(uri);
//				BufferedReader r = new BufferedReader(new InputStreamReader(is));
//				String line;
//				while ((line = r.readLine()) != null) {
//					result.append(line);
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				try { if (is != null) is.close(); } catch (IOException e) { }
//			}
//
//			return result.toString();
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//			super.onPostExecute(result);
//		}
//	}
	
}