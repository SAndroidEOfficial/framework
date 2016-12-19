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
package eu.angel.bleembedded.test;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import eu.angel.bleembedded.R;
import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.alarm.BLEAlarm;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.button.BLEOnClickListener;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIO;
import eu.angel.bleembedded.lib.item.generalIO.BLEGeneralIOEvent;
import eu.angel.bleembedded.lib.item.generalIO.BLEOnGeneralIOEventListener;


@SuppressLint({ "NewApi", "ServiceCast" })
public class MainActivityTestRaspiShort extends Activity {
	
	
	protected static final String TAG = "MainActivityTestRaspi";
	protected static final String TAGV = "MainActivityTestRaspiV";
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
	BLEGeneralIO gpioIn;
	BLEGeneralIO led2A;
	BLEGeneralIO led2B;
	BLEGeneralIO lockEnableOut;
	BLEGeneralIO ArduinolockEnable;

	BLEGeneralIO photoResistor1;
	BLEGeneralIO photoResistor2;

	int lightMode = LIGHT_MODE_PHOTO;
	private final boolean TIenabled=false;
	private final boolean arduinoLightEnabled=true;
	private final boolean arduinoLockEnabled=true;
	private final boolean arduinoAutoLightEnabled=true&arduinoLightEnabled;

	private static String ARDUINO_ITEM_NAME_SUFFIX="ra";
	private static String ARDUINO_ITEM_SHIELD_SUFFIX;

	private final boolean redBearShield=false;
	private final boolean lapisShield=false;
	private final boolean raspi=true;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintesigm);
		tvAnalog1= (TextView)findViewById(R.id.analogin1);
		tvAnalog2= (TextView)findViewById(R.id.analogin2);
		etCode=(EditText)findViewById(R.id.lockCode);
		lightModeSelector= (Button) findViewById(R.id.lightModeSelector);
		switch1= (Button)findViewById(R.id.switchRoom1);
		switch2= (Button)findViewById(R.id.switchRoom2);
		AndroidlockEnable= (Button)findViewById(R.id.lockEnable);
		checkCode= (Button)findViewById(R.id.checkCode);
		ARDUINO_ITEM_SHIELD_SUFFIX="raspi";


		BLEContext.initBLE(this);



			led1A = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
					"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_25");

		led1B = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
				"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_7");

		gpioIn = (BLEGeneralIO) BLEContext.findViewById(ARDUINO_ITEM_NAME_SUFFIX+
				"_"+ARDUINO_ITEM_SHIELD_SUFFIX+"_general_io_2");



		lightModeSelector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				led1B.setDigitalValueHigh(true);}
		});

		checkCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


				led1B.setDigitalValueHigh(false);}
		});

		switch1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

						led1A.setDigitalValueHigh(true);}

		});

		switch2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				led1A.setDigitalValueHigh(false);
			}
		});

		AndroidlockEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});




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
		});
		gpioIn.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
			@Override
			public void onBoardInitEnded() {
				gpioIn.setStatus(BLEGeneralIO.GENERAL_IO_DI);
			}

			@Override
			public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
				Log.d(TAG, "dig changed");
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
	}




	
}