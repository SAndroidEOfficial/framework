package it.unibs.sandroide.test;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.json.JSONException;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.activities.SandroideApplication;
import it.unibs.sandroide.lib.beacon.BeaconTags;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgBase;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgNearable;
import it.unibs.sandroide.lib.beacon.notifier.TagRangeNotifier;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.button.BLEButton;
import it.unibs.sandroide.lib.item.button.BLEOnClickListener;
import it.unibs.sandroide.lib.item.generalIO.BLEGeneralIO;
import it.unibs.sandroide.lib.item.generalIO.BLEGeneralIOEvent;
import it.unibs.sandroide.lib.item.generalIO.BLEOnGeneralIOEventListener;

/**
 * Created by giova on 27/03/2017.
 */

public class LogService extends IntentService implements BeaconConsumer {
    protected static final String TAG = "LogService";
    BLEGeneralIO nanoButton;
    BLEGeneralIO nanoLed;
    BLEGeneralIO nanoTrimmer;
    BLEButton mButton1;
    BLEButton mButton2;
    BLEGeneralIO arduinoButton, arduinoLed, raspALed, raspAButton, raspBLed, raspBButton;

    private BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    public LogService()
    {
        super("LogService");
    }

    @Override
    protected void onHandleIntent(Intent i)
    {
        BLEContext.initBLE(this.getApplicationContext());
        int n=0;


        arduinoLed = (BLEGeneralIO) BLEContext.findViewById("arduino_rbs_general_io_5");
        arduinoButton = (BLEGeneralIO) BLEContext.findViewById("arduino_rbs_general_io_2");

        raspALed = (BLEGeneralIO) BLEContext.findViewById("raspA_raspi_general_io_5");
        raspAButton = (BLEGeneralIO) BLEContext.findViewById("raspA_raspi_general_io_2");

        raspBLed = (BLEGeneralIO) BLEContext.findViewById("raspB_raspi_general_io_5");
        raspBButton = (BLEGeneralIO) BLEContext.findViewById("raspB_raspi_general_io_2");

        try {
            arduinoLed.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    arduinoLed.setStatus(BLEGeneralIO.GENERAL_IO_DO);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

                }

                @Override
                public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                }

                @Override
                public void onDigitalOutputValueChanged(final BLEGeneralIOEvent bleGeneralIOEvent) {
                    Log.i(TAG,String.format("Arduino led %s",bleGeneralIOEvent.values[1]==1?"ON":"OFF"));
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

            arduinoButton.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    arduinoButton.setStatus(BLEGeneralIO.GENERAL_IO_DI);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    if(bleGeneralIOEvent.values[1]==1)
                    {
                        raspALed.setDigitalValueHigh(true);
                        Log.i(TAG,"Arduino button pressed");
                    } else {
                        raspALed.setDigitalValueHigh(false);
                        Log.i(TAG,"Arduino button released");
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
            });
        } catch(RuntimeException ex) {
            BLEContext.displayToastOnMainActivity(ex.toString());
            ex.printStackTrace();
        }

        try {
            raspALed.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    raspALed.setStatus(BLEGeneralIO.GENERAL_IO_DO);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

                }

                @Override
                public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                }

                @Override
                public void onDigitalOutputValueChanged(final BLEGeneralIOEvent bleGeneralIOEvent) {
                    Log.i(TAG,String.format("Rasp A led %s",bleGeneralIOEvent.values[1]==1?"ON":"OFF"));
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

            raspAButton.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    raspAButton.setStatus(BLEGeneralIO.GENERAL_IO_DI);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    if(bleGeneralIOEvent.values[1]==0)
                    {
                        raspBLed.setDigitalValueHigh(true);
                        Log.i(TAG,"Rasp A button pressed");
                    } else {
                        raspBLed.setDigitalValueHigh(false);
                        Log.i(TAG,"Rasp A button released");
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
            });
        } catch(RuntimeException ex) {
            BLEContext.displayToastOnMainActivity(ex.toString());
            ex.printStackTrace();
        }


        try {
            raspBLed.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    raspBLed.setStatus(BLEGeneralIO.GENERAL_IO_DO);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

                }

                @Override
                public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                }

                @Override
                public void onDigitalOutputValueChanged(final BLEGeneralIOEvent bleGeneralIOEvent) {
                    Log.i(TAG,String.format("Rasp B led %s",bleGeneralIOEvent.values[1]==1?"ON":"OFF"));
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

            raspBButton.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    raspBButton.setStatus(BLEGeneralIO.GENERAL_IO_DI);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    if (bleGeneralIOEvent.values[1] == 0) {
                        arduinoLed.setDigitalValueHigh(true);
                        Log.i(TAG,"Rasp B button pressed");
                    } else {
                        arduinoLed.setDigitalValueHigh(false);
                        Log.i(TAG,"Rasp B button released");
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
            });
        } catch(RuntimeException ex) {
            BLEContext.displayToastOnMainActivity(ex.toString());
            ex.printStackTrace();
        }

        /*nanoLed = (BLEGeneralIO) BLEContext.findViewById("nano_rbs_general_io_28");
        nanoButton = (BLEGeneralIO) BLEContext.findViewById("nano_rbs_general_io_15");
        nanoTrimmer = (BLEGeneralIO) BLEContext.findViewById("nano_rbs_general_io_4");*/

        if (nanoTrimmer!=null) {
            nanoTrimmer.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    nanoTrimmer.setStatus(BLEGeneralIO.GENERAL_IO_AI);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {

                }

                @Override
                public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    Log.d(TAG, "analog value changing: " + bleGeneralIOEvent.values[1]);
                    final float val = bleGeneralIOEvent.values[1];
                    Log.i(TAG,"Trimmer value" + Math.round(val * 100) / (float) 100);
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

        if (nanoLed!=null) {
            nanoLed.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    nanoLed.setStatus(BLEGeneralIO.GENERAL_IO_DO);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                }

                @Override
                public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                }

                @Override
                public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    final float val = bleGeneralIOEvent.values[1];
                    Log.i(TAG,val == 1 ? "Led ON" : "Led OFF");
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

        if (nanoButton!=null) {
            nanoButton.setOnGeneralIOEventListener(new BLEOnGeneralIOEventListener() {
                @Override
                public void onBoardInitEnded() {
                    nanoButton.setStatus(BLEGeneralIO.GENERAL_IO_DI);
                }

                @Override
                public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
                    Log.d(TAG, "button pressing: " + bleGeneralIOEvent.values[1]);
                    if (bleGeneralIOEvent.values[1] == 1) {
                        Log.i(TAG, "BUTTON PRESSED");
                        nanoLed.setDigitalValueHigh(true);
                    } else {
                        Log.i(TAG, "BUTTON RELEASED");
                        nanoLed.setDigitalValueHigh(false);
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
            });
        }


        mButton2 = (BLEButton) BLEContext.findViewById("cyan_flicbutton");
        if (mButton2 != null) {
            mButton2.setOnClickListener(new BLEOnClickListener() {
                @Override
                public void onClick(BLEItem bleItem) {
                    Log.i(TAG,"Clicked CYAN");
                }
            });
        }

        mButton1 = (BLEButton) BLEContext.findViewById("black_flicbutton");
        if (mButton1 != null) {
            mButton1.setOnClickListener(new BLEOnClickListener() {
                @Override
                public void onClick(BLEItem bleItem) {
                    Log.i(TAG,"Clicked BLACK");
                }
            });
        }

        beaconManager = SandroideApplication.beaconManager;
        //beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BeaconTags.getInstance().initLayouts(beaconManager);

        //BeaconTags.getInstance().initLayouts(beaconManager,"near");
        //BeaconTags.getInstance().initLayouts(beaconManager,"cyan");
       //BeaconTags.getInstance().initLayouts(beaconManager,"ice");
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.setBackgroundBetweenScanPeriod(30000l);
        beaconManager.setForegroundBetweenScanPeriod(2000l);
        beaconManager.bind(this);


        while(true)
        {
            Log.i("PROVA SERVICE", "Evento n."+n++);
            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            { }
        }
    }

    @Override
    public void onDestroy()
    {
        Log.i("PROVA SERVICE", "Distruzione Service");
    }

    @Override
    public void onBeaconServiceConnect() {

        BeaconTags.getInstance().clearNotifiers(beaconManager);

        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "near", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                Log.i(TAG,String.format("NEAR Beacon in range for tag:%s, key:%s, ids:%s","near", b.getParserSimpleClassname(), b.getIdentifiers().toString()));

                BeaconMsgBase beac = new BeaconMsgNearable(b).parse();
                if (beac!=null) {
                    Log.i(TAG,String.format("Found my nearable: %s",beac.toString()));
                } else {
                    Log.e(TAG,String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                }
            }
        });
        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "cyan", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                Log.i(TAG,String.format("CYAN Beacon in range for tag:%s, key:%s, ids:%s","cyan", b.getParserSimpleClassname(), b.getIdentifiers().toString()));

                BeaconMsgBase beac = new BeaconMsgNearable(b).parse();
                if (beac!=null) {
                    Log.i(TAG,String.format("Found my nearable: %s",beac.toString()));
                } else {
                    Log.e(TAG,String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                }
            }
        });
    }
}