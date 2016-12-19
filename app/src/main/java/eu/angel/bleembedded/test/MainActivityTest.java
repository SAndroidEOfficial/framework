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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import eu.angel.bleembedded.R;
import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.complements.Complements;
import eu.angel.bleembedded.lib.complements.expressionParser.AdditionExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.ConstantExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.EvaluationException;
import eu.angel.bleembedded.lib.complements.expressionParser.ExponentiationExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.ExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.FunctionExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.MultiplicationExpressionNode;
import eu.angel.bleembedded.lib.complements.expressionParser.Parser;
import eu.angel.bleembedded.lib.complements.expressionParser.ParserException;
import eu.angel.bleembedded.lib.complements.expressionParser.SetVariable;
import eu.angel.bleembedded.lib.complements.expressionParser.VariableExpressionNode;
import eu.angel.bleembedded.lib.data.ByteLogicHandler;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.alarm.BLEAlarm;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.button.BLEOnClickListener;
import eu.angel.bleembedded.lib.item.sensor.BLESensor;
import eu.angel.bleembedded.lib.item.sensor.BLESensorEvent;
import eu.angel.bleembedded.lib.item.sensor.BLESensorEventListener;
import eu.angel.bleembedded.lib.item.sensor.BLESensorManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class MainActivityTest extends Activity implements BLESensorEventListener,BeaconConsumer
{

    private static final String TAG = "MainActivityTest";
    BLESensorManager bleSensorManager;
    BLESensor mThermometer;
    BLEAlarm mbleAlarm;
    BLEButton mbleButton;
    Context context;
    TextView tv;
    long[] pattern = {(long)3000, (long)2000, (long)3000, (long)1000};

    private BeaconManager beaconManager;
    private Region region;

    private boolean BEACON_ENABLED = true;
    private boolean BUTTONS_ENABLED = false;
    private boolean SANDROIDE_ENABLED = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;
        tv=(TextView) findViewById(R.id.textView);

        String exprstr = "2*(1+sin(pi/2))^2+pi";

        //String exprstr = "(hF0_AND15)-100";
        //exprstr = "10+(2_ls4)";
        exprstr = "(byte1#RS#4#LS#8)#OR#byte2";

        Parser parser = new Parser();

        byte[] bytes={(byte)0x32, (byte)0xFF};
        int test=(int) (bytes[1]&0xFF);
        Log.d(TAG, "test: "+test);
        try{
            ByteLogicHandler byteLogicHandler=new ByteLogicHandler(exprstr);
        if (byteLogicHandler.setValue(new byte[]{bytes[0], bytes[1]})) {
            Log.d(TAG, "incoming bytes: " + Arrays.toString(bytes));
            int bytesInt = byteLogicHandler.handle();
            bytes = Complements.beIntToByteArray(bytesInt);
        }
//        try
//        {
//            ExpressionNode expr = parser.parse(exprstr);
//            for (ExpressionNode expressionNode:parser.getVariableExpressionNodes()){
//                if (((VariableExpressionNode)expressionNode).getName().equals("byte1"))
//                    ((VariableExpressionNode)expressionNode).setValue(5);
//
//                if (((VariableExpressionNode)expressionNode).getName().equals("byte2"))
//                    ((VariableExpressionNode)expressionNode).setValue(1);
//            }
//
////            expr.accept(new SetVariable("pi", Math.PI));
////            expr.accept(new SetVariable("byte1",8));
////            expr.accept(new SetVariable("byte2",2.5));
//            System.out.println("The value of the expression is "+expr.getValue());
//
//            for (ExpressionNode expressionNode:parser.getVariableExpressionNodes()){
//                if (((VariableExpressionNode)expressionNode).getName().equals("byte2"))
//                    ((VariableExpressionNode)expressionNode).setValue(5);
//
//                if (((VariableExpressionNode)expressionNode).getName().equals("byte1"))
//                    ((VariableExpressionNode)expressionNode).setValue(1);
//            }
//
////            expr.accept(new SetVariable("pi", Math.PI));
////            expr.accept(new SetVariable("byte1",8));
////            expr.accept(new SetVariable("byte2",2.5));
//            System.out.println("The value of the expression is "+expr.getValue());

        }
        catch (ParserException e)
        {
            System.out.println(e.getMessage());
        }
        catch (EvaluationException e)
        {
            System.out.println(e.getMessage());
        }



        if (BEACON_ENABLED) {
            beaconManager = BeaconManager.getInstanceForApplication(this);

            if (beaconManager.getBeaconParsers().size()<5) {
                // Add parser for iBeacons;
                beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
                // Detect the Eddystone main identifier (UID) frame:
                beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
                // Detect the Eddystone telemetry (TLM) frame:
                beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
                                //setBeaconLayout("s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
                // Detect the Eddystone URL frame:
                beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
            }


            // Get the details for all the beacons we encounter.
            region = new Region("justGiveMeEverything", null, null, null);
            if (!beaconManager.isBound(this)) {
                beaconManager.bind(this);
            }
        }

        if (SANDROIDE_ENABLED) BLEContext.initBLE(this);
        if (BUTTONS_ENABLED) {
            /*bleSensorManager=(BLESensorManager) BLEContext.getSystemService(BLEContext.SENSOR_SERVICE);
            mThermometer=bleSensorManager.getDefaultSensor
                    (BLESensor.TYPE_TEMPERATURE, "base_sensor_nrg_thermometer");*/
            mbleAlarm = (BLEAlarm) BLEContext.getSystemService
                    (BLEContext.ALARM_SERVICE, "alarm_key_alarm");
            mbleButton = (BLEButton) BLEContext.findViewById("remote_key_button_1");

            if (mbleButton != null) {
                mbleButton.setOnClickListener(new BLEOnClickListener() {
                    @Override
                    public void onClick(BLEItem bleItem) {
                        Log.d(TAG, "clickedddddd_button1");
                        (MainActivityTest.this).runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      mbleAlarm.alarm(5000);
                                      //mbleAlarm.cancel();
                                  }
                              }
                        );
                    }
                });
            }
        }

        //tv.setText("Temperature:\n"+"25.0 "+getString(R.string.deg)+"C");
        //tv.setText("Temperature:\n"+"27.0 "+getString(R.string.deg)+"C");
        //tv.setText("Temperature:\n"+"25.5 "+getString(R.string.deg)+"C");
        //tv.setText("Temperature:\n"+"25.0 "+getString(R.string.deg)+"C");
        //tv.setText("Temperature:\n"+"27.0 "+getString(R.string.deg)+"C");
        //tv.setText("Temperature:\n"+"25.0 "+getString(R.string.deg)+"C");

    }


    @Override
    public void onBeaconServiceConnect() {
       /*beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    } */


        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Iterator<Beacon> beaconIterator = beacons.iterator();
                    while (beaconIterator.hasNext()) {
                        Beacon beacon = beaconIterator.next();
                        // Debug - logging a beacon - checking background logging is working.
                        logBeaconData(beacon);
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG,e.getMessage());
        }

    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //bleSensorManager.registerListener(this, mThermometer, 10000);
        //bleSensorManager.registerListener(this, mThermometer, 3000);
    }

    @Override
    public void onSensorChanged(BLESensorEvent bleSensorEvent) {
        long timestamp = bleSensorEvent.timestamp;
        final float temperature = bleSensorEvent.values[0]/10;
        (MainActivityTest.this).runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   tv.setText("Temperature:\n"+Float.toString(temperature)+" C");}
           }
        );

        if (temperature>26)
        {
            (MainActivityTest.this).runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           mbleAlarm.alarm(9000);
                           //mbleAlarm.alarm(2000);
                       }
                   }
            );
        }


    }

    @Override
    public void onAccuracyChanged(BLESensor bleSensor, int i) {

    }

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BEACON_ENABLED) beaconManager.unbind(this);
        if (SANDROIDE_ENABLED) BLEContext.releaseBLE();
    }

    /**
     *
     * @param beacon The detected beacon
     */
    private void logBeaconData(Beacon beacon) {

        StringBuilder scanString = new StringBuilder();

        if (beacon.getServiceUuid() == 0xfeaa) {

            if (beacon.getBeaconTypeCode() == 0x00) {
                logGenericBeacon(scanString, beacon);
                scanString.append(" Eddystone-UID -> ");
                scanString.append(" Namespace : ").append(beacon.getId1());
                scanString.append(" Identifier : ").append(beacon.getId2());

                logEddystoneTelemetry(scanString, beacon);

            } else if (beacon.getBeaconTypeCode() == 0x10) {
                logGenericBeacon(scanString, beacon);
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                scanString.append(" Eddystone-URL -> " + url);

            } else if (beacon.getBeaconTypeCode() == 0x20) {
                logGenericBeacon(scanString, beacon);
                scanString.append(" Eddystone-TLM -> ");
                logEddystoneTelemetry(scanString, beacon);

            }

        } else {

            // Just an old fashioned iBeacon or AltBeacon...
            logGenericBeacon(scanString, beacon);

        }

        Log.d(TAG,scanString.toString());
        System.out.println(scanString.toString());

        //logToDisplay(scanString.toString());
        //scanString.append("\n");

    }

    /**
     * Logs iBeacon & AltBeacon data.
     */
    private void logGenericBeacon(StringBuilder scanString, Beacon beacon) {
        scanString.append(" UUID: ").append(beacon.getId1());
        /*scanString.append(" Maj. Mnr.: ");
        if (beacon.getId2() != null) {
            scanString.append(beacon.getId2());
        }
        scanString.append("-");
        if (beacon.getId3() != null) {
            scanString.append(beacon.getId3());
        }*/

        scanString.append(" RSSI: ").append(beacon.getRssi());
        scanString.append(" Proximity: ").append(BeaconHelper.getProximityString(beacon.getDistance()));
        scanString.append(" Power: ").append(beacon.getTxPower());
        //scanString.append(" Timestamp: ").append(BeaconHelper.getCurrentTimeStamp());
    }

    private void logEddystoneTelemetry(StringBuilder scanString, Beacon beacon) {
        // Do we have telemetry data?
        if (beacon.getExtraDataFields().size() > 0) {
            long telemetryVersion = beacon.getExtraDataFields().get(0);
            long batteryMilliVolts = beacon.getExtraDataFields().get(1);
            long pduCount = beacon.getExtraDataFields().get(3);
            long uptime = beacon.getExtraDataFields().get(4);

            scanString.append(" Telemetry version : " + telemetryVersion);
            scanString.append(" Uptime (sec) : " + uptime);
            scanString.append(" Battery level (mv) " + batteryMilliVolts);
            scanString.append(" Tx count: " + pduCount);
        }
    }



}
