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
package eu.angel.bleembedded.beacontest;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.alarm.BLEAlarm;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconMonitorNotifier;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.button.BLEOnClickListener;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIO;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIOEvent;
import eu.angel.bleembedded.lib.item.generalIO.BLEOnGeneralIOEventListener;


@SuppressLint({ "NewApi", "ServiceCast" })
public class MainActivityTesiGMAndBeacon extends Activity {
	
	
	protected static final String TAG = "MainActivityTesiGM";
	protected static final String TAGV = "MainActivityTGMValue";
	protected final String lockCode = "12345";



	final static int LIGHT_MODE_PHOTO = 0;
	final static int LIGHT_MODE_MANUAL = 1;

	final static int VERY_DARK = 600; //sopra 700 entrambi accesi
	final static int VERY_SHINE = 100; //sotto i 100 entrambi spenti

	final static int ON=1;
	final static int OFF=0;


	int switch1status = OFF;
	int switch2status = OFF;
	int lockAbilitationStatus= OFF;


	private int photoRcycles1 = 0;
	private int photoRcycles2 = 0;
	private final int PHOTO_CYCLES = 30;
	private final int LOCK_ENABLE_ON_TIME_MS = 2000;
	//Android Embedded
	TextView tvAnalog1;
	TextView tvAnalog2;
	EditText etCode;
	Button lightModeSelector;
	Button switch1;
	Button switch2;
	Button AndroidlockEnable;
	Button checkCode;

	//TI board
	BLEButton remoteLockEnable;
	BLEAlarm wrongCodeAlarm;

	//Arduino board
	BLEGeneralIO led1A;
	BLEGeneralIO led1B;
	BLEGeneralIO led2A;
	BLEGeneralIO led2B;
	BLEGeneralIO lockEnableOut;
	BLEGeneralIO ArduinolockEnable;
	BLEGeneralIO lampOut;

	BLEGeneralIO photoResistor1;
	BLEGeneralIO photoResistor2;

	int lightMode = LIGHT_MODE_PHOTO;
	private final boolean TIenabled=true;
	private final boolean arduinoLightEnabled=true;
	private final boolean arduinoLockEnabled=true;
	private final boolean arduinoAutoLightEnabled=true&arduinoLightEnabled;
	private final boolean ledFirstRoomEnabled=false;
	private final boolean arduinoLampEnabled=!ledFirstRoomEnabled;

	private static String ARDUINO_ITEM_NAME_SUFFIX="hac";
	private static String ARDUINO_ITEM_SHIELD_SUFFIX;

	private final boolean redBearShield=true;
	private final boolean lapisShield=false;


	private ListView mList;

	private int personIn =0;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintesigmandbeacon);
//		tvAnalog1= (TextView)findViewById(R.id.analogin1);
//		tvAnalog2= (TextView)findViewById(R.id.analogin2);
//		etCode=(EditText)findViewById(R.id.lockCode);
//		lightModeSelector= (Button) findViewById(R.id.lightModeSelector);
//		switch1= (Button)findViewById(R.id.switchRoom1);
//		switch2= (Button)findViewById(R.id.switchRoom2);
//		AndroidlockEnable= (Button)findViewById(R.id.lockEnable);
//		checkCode= (Button)findViewById(R.id.checkCode);

		if (redBearShield)
			ARDUINO_ITEM_SHIELD_SUFFIX="rbs";
		else if(lapisShield)
			ARDUINO_ITEM_SHIELD_SUFFIX="lapis";

		if(lightMode==LIGHT_MODE_PHOTO)
		{
			switch1.setEnabled(false);
			switch2.setEnabled(false);
		}
		else
		{
			tvAnalog1.setText("MAN MODE");
			tvAnalog2.setText("MAN MODE");
		}

		BLEContext.initBLE(this);
        //context=this;


		if(TIenabled) {
//			remoteLockEnable = (BLEButton) BLEContext.findViewById("lock_enable_key_button_1");
//			wrongCodeAlarm = (BLEAlarm) BLEContext.getSystemService
//					(BLEContext.ALARM_SERVICE, "alarm_wrong_code_key_alarm");
			remoteLockEnable = (BLEButton) BLEContext.findViewById("remote_key_button1");
			wrongCodeAlarm = (BLEAlarm) BLEContext.getSystemService
					(BLEContext.ALARM_SERVICE, "alarm_key_alarm");

			remoteLockEnable.setOnClickListener(new BLEOnClickListener() {
				@Override
				public void onClick(BLEItem arg0) {
					if (arduinoLockEnabled)
					{
						lockEnableOut.setDigitalValueHigh(true);
						Timer timer=new Timer("stopLockEnable");
						timer.schedule(
								new TimerTask() {
									@Override
									public void run() {
										lockEnableOut.setDigitalValueHigh(false);
									}
								},
								LOCK_ENABLE_ON_TIME_MS);
					}
				}
			});
		}


		if (arduinoLightEnabled) {
			if (ledFirstRoomEnabled){
			led1A = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_2");
			led1B = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_3");}
			led2A = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_4");
			led2B = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX
					+"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_5");
		}

		if (arduinoLockEnabled) {
			lockEnableOut = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_6");
			ArduinolockEnable = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_7");
		}
		if (arduinoAutoLightEnabled) {
			photoResistor1 = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_14");
			photoResistor2 = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_15");
		}
		if(arduinoLampEnabled){
			lampOut= (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_2");
		}


		lightModeSelector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(lightMode==LIGHT_MODE_PHOTO)
				{
					tvAnalog1.setText("MAN MODE");
					tvAnalog2.setText("MAN MODE");
					switch1.setEnabled(true);
					switch2.setEnabled(true);
					lightMode=LIGHT_MODE_MANUAL;
					if (arduinoAutoLightEnabled){
					photoResistor1.setStatus(BLEGeneralIO.GENERAL_IO_DI);
					photoResistor2.setStatus(BLEGeneralIO.GENERAL_IO_DI);}
				}
				else
				{
					switch1.setEnabled(false);
					switch2.setEnabled(false);
					if (arduinoAutoLightEnabled){
					photoResistor1.setStatus(BLEGeneralIO.GENERAL_IO_AI);
					photoResistor2.setStatus(BLEGeneralIO.GENERAL_IO_AI);}
					lightMode=LIGHT_MODE_PHOTO;
				}
			}
		});

		switch1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lightMode == LIGHT_MODE_MANUAL)
				{
					if (switch1status==OFF)
					{
						switch1status=ON;
						if (arduinoLightEnabled){
						led1A.setDigitalValueHigh(true);
						led1B.setDigitalValueHigh(true);}
					}
					else
					{
						switch1status=OFF;
						if (arduinoLightEnabled){
						led1A.setDigitalValueHigh(false);
						led1B.setDigitalValueHigh(false);}
					}
				}
			}
		});

		switch2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lightMode == LIGHT_MODE_MANUAL)
				{
					if (switch2status == OFF) {
						switch2status = ON;
						if (arduinoLightEnabled){
						led2A.setDigitalValueHigh(true);
						led2B.setDigitalValueHigh(true);}
					} else {
						switch2status = OFF;
						if (arduinoLightEnabled){
						led2A.setDigitalValueHigh(false);
						led2B.setDigitalValueHigh(false);}
					}
				}
			}
		});

		AndroidlockEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (arduinoLockEnabled)
				{
					lockEnableOut.setDigitalValueHigh(true);
					Timer timer=new Timer("stopLockEnable");
					timer.schedule(
							new TimerTask() {
								@Override
								public void run() {
									lockEnableOut.setDigitalValueHigh(false);
								}
							},
							LOCK_ENABLE_ON_TIME_MS);
				}
			}
		});

		checkCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String insertedCode = etCode.getText().toString();
				if (insertedCode.equals(lockCode))
				{
					if (arduinoLockEnabled)
					{
						lockEnableOut.setDigitalValueHigh(true);
						Timer timer=new Timer("stopLockEnable");
						timer.schedule(
								new TimerTask() {
									@Override
									public void run() {
										lockEnableOut.setDigitalValueHigh(false);
									}
								},
								LOCK_ENABLE_ON_TIME_MS);
					}
				}
				else
				{
					if (TIenabled)
					{
						wrongCodeAlarm.alarm(5000);
					}
				}

			}
		});

		if (arduinoAutoLightEnabled){
		photoResistor1.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				if (lightMode==LIGHT_MODE_PHOTO)
					photoResistor1.setStatus(BLEGeneralIO.GENERAL_IO_AI);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				float fvalue= bleGeneralIOEvent.values[1];
				int value = (int) fvalue;
				Log.d(TAGV, "Analog1 value: "+Integer.toString(value));
				photoRcycles1++;
				if (photoRcycles1>PHOTO_CYCLES){
					//final String res= String.format("%.1f",(fvalue/1024)*5);
					final String res= Float.toString(fvalue);

					if(lightMode==LIGHT_MODE_PHOTO){
					(MainActivityTesiGMAndBeacon.this).runOnUiThread(
							new Runnable() {
							  @Override
							  public void run() {
								  tvAnalog1.setText(res+" V");
								  }
							}
					);}
					photoRcycles1=0;
					if (value < VERY_SHINE)
					{
						Log.d(TAGV, "room1 very shine");
						led1A.setDigitalValueHigh(false);
						led1B.setDigitalValueHigh(false);
					}
					else if((VERY_SHINE < value) && (value < VERY_DARK))
					{
						Log.d(TAGV, "room1 shady");
						led1A.setDigitalValueHigh(true);
						led1B.setDigitalValueHigh(false);
					}
					else
					{
						Log.d(TAGV, "room1 very dark");
						led1A.setDigitalValueHigh(true);
						led1B.setDigitalValueHigh(true);
					}
				}
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
		photoResistor2.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				if (lightMode==LIGHT_MODE_PHOTO)
					photoResistor2.setStatus(BLEGeneralIO.GENERAL_IO_AI);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

			}

			@Override
			public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				float fvalue= bleGeneralIOEvent.values[1];
				int value = (int) fvalue;
				Log.d(TAGV, "Analog2 value: "+Integer.toString(value));
				photoRcycles2++;
				if (photoRcycles2>PHOTO_CYCLES) {
					//final String res= String.format("%.1f",(fvalue/1024)*5);
					final String res= Float.toString(fvalue);
					if(lightMode==LIGHT_MODE_PHOTO){
					(MainActivityTesiGMAndBeacon.this).runOnUiThread(
							new Runnable() {
								@Override
								public void run() {
									tvAnalog2.setText(res+" V");
								}
							}
					);}
					photoRcycles2=0;
					if (value < VERY_SHINE) {
						Log.d(TAGV, "room2 very shine");
						led2A.setDigitalValueHigh(false);
						led2B.setDigitalValueHigh(false);
					} else if ((VERY_SHINE < value) && (value < VERY_DARK)) {
						Log.d(TAGV, "room2 shady");
						led2A.setDigitalValueHigh(true);
						led2B.setDigitalValueHigh(false);
					} else {
						Log.d(TAGV, "room2 very dark");
						led2A.setDigitalValueHigh(true);
						led2B.setDigitalValueHigh(true);
					}
				}
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
		});}
		if (arduinoLightEnabled){
			if(ledFirstRoomEnabled){
		led1A.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led1A.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
		led1B.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led1B.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
		});}
		led2A.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led2A.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
		led2B.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led2B.setStatus(BLEGeneralIO.GENERAL_IO_DO);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
		});}
		if (arduinoLockEnabled){
		lockEnableOut.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
			//	mi resta il mode_model_6 anche se sul file ho segnato mode model 7........
				lockEnableOut.setStatus(BLEGeneralIO.GENERAL_IO_DO);
				lockEnableOut.setDigitalValueHigh(false);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
		ArduinolockEnable.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				ArduinolockEnable.setStatus(BLEGeneralIO.GENERAL_IO_DI);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "digital input lock enable changed: "+ bleGeneralIOEvent.values[1]);
				if(bleGeneralIOEvent.values[1]==1)
				{
					lockEnableOut.setDigitalValueHigh(true);
					Timer timer=new Timer("stopLockEnable");
					timer.schedule(
						new TimerTask() {
							@Override
							public void run() {
								lockEnableOut.setDigitalValueHigh(false);
							}
						},
							LOCK_ENABLE_ON_TIME_MS);
				}
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
		});}
		if (arduinoLampEnabled){
			lampOut.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
				@Override
				public void onBoardInitEnded() {
					lampOut.setStatus(BLEGeneralIO.GENERAL_IO_DO);
					lampOut.setDigitalValueHigh(false);
				}

				@Override
				public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

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
			});}

//		BLEBeaconManager.startBeaconRangeNotifier(new BLEBeaconRangeNotifier() {
//			@Override
//			public void didRangeBeaconsInRegion(Collection<BLEBeacon> bleBeacons,
//												BLEBeaconRegion region,
//												BLEBeaconCluster bleBeaconCluster) {
//				List<String> strings=new ArrayList<String>();
//				for (BLEBeacon bleBeacon:bleBeacons){
//					if (bleBeacon.getUniqueId()!=null)
//						strings.add(bleBeacon.getUniqueId());
//					else
//						strings.add("region: "+region.getUidBleRegion());
//					for (BLEBeaconData bleBeaconData:bleBeacon.getBLEBeaconData())
//						Log.d(TAG, "DATA: "+bleBeacon.getUniqueId()+"; "+
//								bleBeaconData.getData_type());
//				}
//
//				final List<String> finalstring = new ArrayList<>(strings);
//				(MainActivityTesiGMAndBeacon.this).runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						updateAdapter(finalstring);}
//				});
//			}
//		});



		BLEBeaconManager.startBeaconMonitorNotifier(new BLEBeaconMonitorNotifier() {
			@Override
			public void didEnterCluster(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion) {
				Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" entered");
				if (bleBeaconCluster.getUniqueId().equals("home")){
					personIn++;
					if (arduinoLockEnabled)
					{
						lockEnableOut.setDigitalValueHigh(true);
						Timer timer=new Timer("stopLockEnable");
						timer.schedule(
								new TimerTask() {
									@Override
									public void run() {
										lockEnableOut.setDigitalValueHigh(false);
									}
								},
								LOCK_ENABLE_ON_TIME_MS);
					}
					if(arduinoLightEnabled){
						if (ledFirstRoomEnabled) {
							led1A.setDigitalValueHigh(true);
							led1B.setDigitalValueHigh(true);
						}
						led2A.setDigitalValueHigh(true);
						led2B.setDigitalValueHigh(true);
					}
					if(arduinoLampEnabled){
						lampOut.setDigitalValueHigh(true);
					}
				}

				String s=bleBeaconRegion.getBleBeacon().getUniqueId()+" of group "+bleBeaconCluster.getUniqueId()+" entered";
				List<String> string = new ArrayList<>();
				string.add(s);
				final List<String> finalstring = new ArrayList<>(string);
				(MainActivityTesiGMAndBeacon.this).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateAdapter(finalstring);}
				});
			}

			@Override
			public void didExitCluster(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion) {
				Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" exited");
				if (bleBeaconCluster.getUniqueId().equals("home")){
					personIn--;
					if(arduinoLightEnabled){
						if (ledFirstRoomEnabled){
							led1A.setDigitalValueHigh(false);
							led1B.setDigitalValueHigh(false);
						}
						led2A.setDigitalValueHigh(false);
						led2B.setDigitalValueHigh(false);
					}
					if(arduinoLampEnabled){
						if(personIn ==0)
							lampOut.setDigitalValueHigh(false);
					}
				}

				String s=bleBeaconRegion.getBleBeacon().getUniqueId()+
						" of group "+bleBeaconCluster.getUniqueId()+" exited";
				List<String> string = new ArrayList<>();
				string.add(s);
				final List<String> finalstring = new ArrayList<>(string);
				(MainActivityTesiGMAndBeacon.this).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateAdapter(finalstring);}
				});
			}

			@Override
			public void didDetermineStateForCluster(int i, BLEBeaconCluster bleBeaconCluster) {

			}
		});

		mList=(ListView) findViewById(R.id.list);

        }

	public void updateAdapter(List<String> stringList)
	{
		final ArrayAdapter arrayAdapter =
				new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList);
		mList.setAdapter(arrayAdapter);
	}

	
}