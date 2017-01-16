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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.activities.SandroideBaseActivity;
import eu.angel.bleembedded.lib.data.BLEBeaconData;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.alarm.BLEAlarm;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconMonitorNotifier;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconRangeNotifier;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.button.BLEOnClickListener;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIO;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIOEvent;
import eu.angel.bleembedded.lib.item.generalIO.BLEOnGeneralIOEventListener;


@SuppressLint({ "NewApi", "ServiceCast" })
public class MainActivityResearcherNight extends SandroideBaseActivity {

    // TODO: 19/10/16 delete unusued
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
	private final int LED_INCOMING_PERSON_DETECTED_MS = 2000;
	private final int LED_OUTCOMING_PERSON_DETECTED_MS = 5000;
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
	BLEGeneralIO led1;
	BLEGeneralIO led2;
	BLEGeneralIO lampRelay;
	BLEGeneralIO lockRelay;


	int lightMode = LIGHT_MODE_PHOTO;
	private final boolean TIenabled=false;




	private static String ARDUINO_ITEM_NAME_SUFFIX="hac";
	private static String ARDUINO_ITEM_SHIELD_SUFFIX;

	private final boolean redBearShield=true;
	private final boolean lapisShield=false;


	private ListView mListMonitor;
	private ListView mListRange;

	private int personIn =0;

	private List<AccessHelper> accessHelpers = new ArrayList<>();
	private int RSSIThreshold=-70;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintesigmandbeacon);
		// TODO: 19/10/16 delete
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
// TODO: 19/10/16 delete
//		if(lightMode==LIGHT_MODE_PHOTO)
//		{
//			switch1.setEnabled(false);
//			switch2.setEnabled(false);
//		}
//		else
//		{
//			tvAnalog1.setText("MAN MODE");
//			tvAnalog2.setText("MAN MODE");
//		}

        //context=this;


		if(TIenabled) {
// TODO: 19/10/16 delete
//			remoteLockEnable = (BLEButton) BLEContext.findViewById("lock_enable_key_button_1");
//			wrongCodeAlarm = (BLEAlarm) BLEContext.getSystemService
//					(BLEContext.ALARM_SERVICE, "alarm_wrong_code_key_alarm");
			remoteLockEnable = (BLEButton) BLEContext.findViewById("remote_key_button1");
			wrongCodeAlarm = (BLEAlarm) BLEContext.getSystemService
					(BLEContext.ALARM_SERVICE, "alarm_key_alarm");

			remoteLockEnable.setOnClickListener(new BLEOnClickListener() {
				@Override
				public void onClick(BLEItem arg0) {

					lockRelay.setDigitalValueHigh(true);
					Timer timer=new Timer("stopLockEnable");
					timer.schedule(
							new TimerTask() {
								@Override
								public void run() {
									lockRelay.setDigitalValueHigh(false);
								}
							},
							LOCK_ENABLE_ON_TIME_MS);

				}
			});
		}



		led1 = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
				"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_2");
		led2 = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
				"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_3");
		lampRelay = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
				"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_4");
		lockRelay = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX
		+"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_5");


// TODO: 19/10/16 delete
//		//region buttonClick
//		lightModeSelector.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(lightMode==LIGHT_MODE_PHOTO)
//				{
//					tvAnalog1.setText("MAN MODE");
//					tvAnalog2.setText("MAN MODE");
//					switch1.setEnabled(true);
//					switch2.setEnabled(true);
//					lightMode=LIGHT_MODE_MANUAL;
//					if (arduinoAutoLightEnabled){
//					photoResistor1.setStatus(BLEGeneralIO.GENERAL_IO_DI);
//					photoResistor2.setStatus(BLEGeneralIO.GENERAL_IO_DI);}
//				}
//				else
//				{
//					switch1.setEnabled(false);
//					switch2.setEnabled(false);
//					if (arduinoAutoLightEnabled){
//					photoResistor1.setStatus(BLEGeneralIO.GENERAL_IO_AI);
//					photoResistor2.setStatus(BLEGeneralIO.GENERAL_IO_AI);}
//					lightMode=LIGHT_MODE_PHOTO;
//				}
//			}
//		});
//
//		switch1.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (lightMode == LIGHT_MODE_MANUAL)
//				{
//					if (switch1status==OFF)
//					{
//						switch1status=ON;
//						if (arduinoLightEnabled){
//						led1A.setDigitalValueHigh(true);
//						led1B.setDigitalValueHigh(true);}
//					}
//					else
//					{
//						switch1status=OFF;
//						if (arduinoLightEnabled){
//						led1A.setDigitalValueHigh(false);
//						led1B.setDigitalValueHigh(false);}
//					}
//				}
//			}
//		});
//
//		switch2.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (lightMode == LIGHT_MODE_MANUAL)
//				{
//					if (switch2status == OFF) {
//						switch2status = ON;
//						if (arduinoLightEnabled){
//						led2A.setDigitalValueHigh(true);
//						led2B.setDigitalValueHigh(true);}
//					} else {
//						switch2status = OFF;
//						if (arduinoLightEnabled){
//						led2A.setDigitalValueHigh(false);
//						led2B.setDigitalValueHigh(false);}
//					}
//				}
//			}
//		});
//
//		AndroidlockEnable.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (arduinoLockEnabled)
//				{
//					lockEnableOut.setDigitalValueHigh(true);
//					Timer timer=new Timer("stopLockEnable");
//					timer.schedule(
//							new TimerTask() {
//								@Override
//								public void run() {
//									lockEnableOut.setDigitalValueHigh(false);
//								}
//							},
//							LOCK_ENABLE_ON_TIME_MS);
//				}
//			}
//		});
//
//		checkCode.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String insertedCode = etCode.getText().toString();
//				if (insertedCode.equals(lockCode))
//				{
//					if (arduinoLockEnabled)
//					{
//						lockEnableOut.setDigitalValueHigh(true);
//						Timer timer=new Timer("stopLockEnable");
//						timer.schedule(
//								new TimerTask() {
//									@Override
//									public void run() {
//										lockEnableOut.setDigitalValueHigh(false);
//									}
//								},
//								LOCK_ENABLE_ON_TIME_MS);
//					}
//				}
//				else
//				{
//					if (TIenabled)
//					{
//						wrongCodeAlarm.alarm(5000);
//					}
//				}
//
//			}
//		});
//		//endregion



		led1.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led1.setStatus(BLEGeneralIO.GENERAL_IO_DO);
				led1.setDigitalValueHigh(false);
				Timer timer=new Timer("connectionLed");
				timer.schedule(
						new TimerTask() {
							@Override
							public void run() {
								led1.setDigitalValueHigh(true);
							}
						},
						1000);


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


		led2.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				led2.setStatus(BLEGeneralIO.GENERAL_IO_DO);
				led2.setDigitalValueHigh(false);
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


		lockRelay.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
			//	mi resta il mode_model_6 anche se sul file ho segnato mode model 7........
				lockRelay.setStatus(BLEGeneralIO.GENERAL_IO_DO);
				lockRelay.setDigitalValueHigh(false);
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

			lampRelay.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
				@Override
				public void onBoardInitEnded() {
					lampRelay.setStatus(BLEGeneralIO.GENERAL_IO_DO);
					lampRelay.setDigitalValueHigh(false);
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

		BLEBeaconManager.startBeaconRangeNotifier(new BLEBeaconRangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<BLEBeacon> bleBeacons,
												BLEBeaconRegion region,
												BLEBeaconCluster bleBeaconCluster) {
				List<String> strings=new ArrayList<>();
				for (BLEBeacon bleBeacon:bleBeacons){
					for (AccessHelper accessHelper:accessHelpers){
						if (accessHelper.bleBeaconRegion.getUidBleRegion().equals(bleBeacon.getUniqueId())){
							if (accessHelper.beaconIn){
								if (bleBeacon.getRssi()<RSSIThreshold){
									accessHelper.beaconIn=false;
									beaconReallyExited(bleBeaconCluster, region);
								}
							} else{
								if (bleBeacon.getRssi()>=RSSIThreshold){
									accessHelper.beaconIn=true;
									beaconReallyEntered(bleBeaconCluster, region);
								}
							}
							break;
						}
					}

					if (bleBeacon.getUniqueId()!=null)
						strings.add(bleBeacon.getUniqueId()+", RSSI: "+bleBeacon.getRssi());
					else
						strings.add("region: "+region.getUidBleRegion());


					for (BLEBeaconData bleBeaconData:bleBeacon.getBLEBeaconData())
						Log.d(TAG, "DATA: "+bleBeacon.getUniqueId()+"; "+
								bleBeaconData.getData_type());
				}

				final List<String> finalstring = new ArrayList<>(strings);
				(MainActivityResearcherNight.this).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateAdapterRange(finalstring);}
				});
			}
		});



		BLEBeaconManager.startBeaconMonitorNotifier(new BLEBeaconMonitorNotifier() {
			@Override
			public void didEnterCluster(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion) {
				Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" entered monitoring");
				accessHelpers.add(new AccessHelper(bleBeaconRegion,bleBeaconCluster));

			}

			@Override
			public void didExitCluster(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion) {
				Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" exited monitoring");
				for (int i=0;i<accessHelpers.size();i++){
					if (accessHelpers.get(i).bleBeaconRegion.getUidBleRegion()
							.equals(bleBeaconRegion.getUidBleRegion())){
						if(accessHelpers.get(i).beaconIn)
							beaconReallyExited(bleBeaconCluster, bleBeaconRegion);
						accessHelpers.remove(i);
						break;
					}
				}

			}

			@Override
			public void didDetermineStateForCluster(int i, BLEBeaconCluster bleBeaconCluster) {

			}
		});

		mListMonitor=(ListView) findViewById(R.id.listMonitor);
		updateAdapterMonitor(new ArrayList<String>());

		mListRange=(ListView) findViewById(R.id.listRange);
		updateAdapterRange(new ArrayList<String>());

        }

	public void updateAdapterMonitor(List<String> stringList)
	{
		final ArrayAdapter arrayAdapter =
				new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList);
		mListMonitor.setAdapter(arrayAdapter);
	}

	public void updateAdapterRange(List<String> stringList)
	{
		final ArrayAdapter arrayAdapter =
				new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList);
		mListRange.setAdapter(arrayAdapter);
	}

	private void beaconReallyEntered(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion){
		Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" entered");
		if (bleBeaconCluster.getUniqueId().equals("home")){
			personIn++;

			lockRelay.setDigitalValueHigh(true);
			Timer timer=new Timer("stopLockEnable");
			timer.schedule(
					new TimerTask() {
						@Override
						public void run() {
							lockRelay.setDigitalValueHigh(false);
						}
					},
					LOCK_ENABLE_ON_TIME_MS);

			lampRelay.setDigitalValueHigh(true);
		}

		led2.setDigitalValueHigh(true);
		Timer timer2=new Timer("incoming_person");
		timer2.schedule(
				new TimerTask() {
					@Override
					public void run() {
						led2.setDigitalValueHigh(false);
					}
				},
				LED_INCOMING_PERSON_DETECTED_MS);

		String s=bleBeaconRegion.getBleBeacon().getUniqueId()+" of group "+bleBeaconCluster.getUniqueId()+" entered";
		List<String> string = new ArrayList<>();
		string.add(s);
		final List<String> finalstring = new ArrayList<>(string);
		(MainActivityResearcherNight.this).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateAdapterMonitor(finalstring);}
		});
	}

	private void beaconReallyExited(BLEBeaconCluster bleBeaconCluster, BLEBeaconRegion bleBeaconRegion){
		Log.d(TAG, "beacon: "+bleBeaconRegion.getUidBleRegion()+" exited");
		if (bleBeaconCluster.getUniqueId().equals("home")){
			personIn--;
			if(personIn ==0)
				lampRelay.setDigitalValueHigh(false);
		}
		led2.setDigitalValueHigh(true);
		Timer timer=new Timer("outcoming_person");
		timer.schedule(
				new TimerTask() {
					@Override
					public void run() {
						led2.setDigitalValueHigh(false);
					}
				},
				LED_OUTCOMING_PERSON_DETECTED_MS);

		String s=bleBeaconRegion.getBleBeacon().getUniqueId()+
				" of group "+bleBeaconCluster.getUniqueId()+" exited";
		List<String> string = new ArrayList<>();
		string.add(s);
		final List<String> finalstring = new ArrayList<>(string);
		(MainActivityResearcherNight.this).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateAdapterMonitor(finalstring);}
		});
	}



}