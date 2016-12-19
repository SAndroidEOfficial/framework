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
package eu.angel.bleembedded.transmissionbeacon;

import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

import eu.angel.bleembedded.R;
import eu.angel.bleembedded.lib.BLEContext;

public class MainActivityBeacon extends Activity{

    private static final String TAG = "MainActivityBeacon";
    private Button button;
    private Button button_stop;
    BeaconTransmitter beaconTransmitter;
    Beacon beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_with_list);

        BLEContext.initBLE(this);

        button=(Button) findViewById(R.id.start_beacon_adv);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                        @Override
                        public void onStartFailure(int errorCode) {
                            Log.e(TAG, "Advertisement start failed with code: "+errorCode);
                        }

                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            Log.i(TAG, "Advertisement start succeeded.");
                        }
                    });
                }
            }
        });

        button_stop=(Button) findViewById(R.id.stop_beacon_adv);
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconTransmitter.stopAdvertising();
            }
        });

         beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6") // UUID for beacon
                .setId2("1") // Major for beacon
                .setId3("5") // Minor for beacon
                .setManufacturer(0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
                .setTxPower(-56) // Power in dB
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

    }

}
