/**
 * @author  Angelo Vezzoli
 * @date    2016
 * @version 1.0
 *
 * Copyright (c) Angelo Vezzoli, University of Brescia (I guess), All Rights Reserved.
 *
 *
 * This software is the confidential and proprietary information of the authors and
 * the University of Brescia
 *
 */
package eu.angel.bleembedded.beaconconfig;


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
public class MainActivityResearcherNight extends Activity {

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

		if (redBearShield)
			ARDUINO_ITEM_SHIELD_SUFFIX="rbs";
		else if(lapisShield)
			ARDUINO_ITEM_SHIELD_SUFFIX="lapis";

		BLEContext.initBLE(this);
        //context=this;


		if(TIenabled) {
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