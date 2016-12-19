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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.data.BLEBeaconData;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconRangeNotifier;

public class ListenBeaconActivity extends Activity {

    private static final String TAG = "ListenBeaconActivity";
    private ListView mList;
    private Button button;
    private Button button2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_list);

        BLEContext.initBLE(this);
        //XmlHandler.saveClusterInPrivateMemory(this);

        button=(Button) findViewById(R.id.start_beacon);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEBeaconManager.startBeaconRangeNotifier(new BLEBeaconRangeNotifier() {
                    @Override
                    public void didRangeBeaconsInRegion(Collection<BLEBeacon> bleBeacons,
                                                        BLEBeaconRegion region,
                                                        BLEBeaconCluster bleBeaconCluster) {
                        List<String> strings=new ArrayList<String>();
                        for (BLEBeacon bleBeacon:bleBeacons){
                            if (bleBeacon.getUniqueId()!=null)
                                strings.add(bleBeacon.getUniqueId());
                            else
                                strings.add("region: "+region.getUidBleRegion());
                            for (BLEBeaconData bleBeaconData:bleBeacon.getBLEBeaconData())
                                Log.d(TAG, "DATA: "+bleBeacon.getUniqueId()+"; "+
                                        bleBeaconData.getData_type());
                        }

                        final List<String> finalstring = new ArrayList<String>(strings);
                        (ListenBeaconActivity.this).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAdapter(finalstring);}
                        });
                    }
                });
            }
        });

        button2=(Button) findViewById(R.id.stop_beacon);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BLEBeaconManager.stopBeaconMonitorNotifier();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
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
