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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconBeaconNotifier;

/**  This is the example activity used to listen the in-range BLE Beacons and to compile the XML description file.
 *  FIXME PAOLO
 */
public class MainActivityBeacon extends Activity implements View.OnClickListener {

    private ListView mList;
    private Button button;
    private Button button_start;
    private List<BLEBeacon> bleBeacons;
    private String configurationStatusString;
    private TextView configurationStatus;

    /** The example Android onCreate() function. This function init the SAndroide BLEContext, retrieve
     *  the Beacon parsers configuration file from the phone root directory and copies it into the application
     *  memory space. Set up the interface and all the callbacks for the interface.
     * FIXME PAOLO
     * @param savedInstanceState refer to [Android Docs](http//developer.android.com) for further information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_with_list);

        //init the SAndroidE Context
        BLEContext.initBLE(this);
        // copy the Beacon parsers configuration file from the phone root to the application memory space
        //XmlHandler.saveParserInPrivateMemory(this);

        configurationStatus=(TextView) findViewById(R.id.connection_state);


        // Define the Cluster handling button
        button=(Button) findViewById(R.id.cluster_handling);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when the button is pressed start the Cluster Activity via intent
                final Intent intent = new Intent(MainActivityBeacon.this, ClusterActivity.class);
                startActivity(intent);
            }
        });

        // todo che list è?
        mList=(ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFrame dialogFrame=new DialogFrame();
                dialogFrame.builderDialog(position);
            }
        });

        // define the start config button
        button_start =(Button) findViewById(R.id.start_config);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivityBeacon.this,
                        "configuration on", Toast.LENGTH_SHORT).show();
                BLEBeaconManager.startBeaconClusterConfiguring(new BLEBeaconBeaconNotifier() {
                    @Override
                    public void didRangeBeacons(Collection<BLEBeacon> bleBeacons1) {
                        List<String> s=new ArrayList<String>();
                        bleBeacons=new ArrayList<BLEBeacon>();
                        for(BLEBeacon bleBeacon:bleBeacons1)
                        {
                            bleBeacons.add(bleBeacon);
                            StringBuilder s1=new StringBuilder();
                            List<Identifier> identifiers=bleBeacon.getIdentifiers();
                            for (Identifier identifier:identifiers){
                                s1.append(identifier.toString()).append("; ");
                            }

                            s1.append("RSSI: ").append(bleBeacon.getRssi());
                            s.add(s1.toString());
                        }
                        final List<String> sfinal=new ArrayList<String>(s);
                        (MainActivityBeacon.this).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAdapter(sfinal);
                            }
                        });
                    }
                });

                configurationStatusString="configuration on";
                SharedPreferences sharedPref = PreferenceManager
                        .getDefaultSharedPreferences(MainActivityBeacon.this);
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString(ClusterActivity.CONF_STATUS_KEY, configurationStatusString);
                edit.commit();
                (MainActivityBeacon.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        configurationStatus.setText(configurationStatusString);}
                });

            }
        });

        bleBeacons=new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        configurationStatusString=sharedPref.getString(ClusterActivity.CONF_STATUS_KEY, "configuration off");

        if (configurationStatusString!=null)
            configurationStatus.setText(configurationStatusString);

    }

    public void updateAdapter(List<String> stringList)
    {
        final ArrayAdapter arrayAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList);
        mList.setAdapter(arrayAdapter);

    }

    @Override
    public void onClick(View v) {

    }

    private class DialogFrame {
        int position=-1;
        boolean resourcesStored=false;
        BLEBeacon bleBeacon;
        DialogInterface.OnClickListener dialogHOCLok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processInput();
            }
        };


        DialogInterface.OnClickListener dialogOCLcancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        EditText et;

        public void builderDialog(int position) {
            this.position=position;
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivityBeacon.this);
            et = new EditText(MainActivityBeacon.this);
            et.setOnClickListener(MainActivityBeacon.this);
            et.requestFocus();
            builder.setMessage(R.string.Enter_a_uid_for_the_beacon)
                    .setTitle(R.string.Beacon)
                    .setPositiveButton(R.string.Ok, dialogHOCLok)
                    .setNegativeButton(R.string.Cancel, dialogOCLcancel)
                    .setView(et);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void processInput() {
            String input = et.getText().toString();
            if (input == null || input.trim().length() == 0) {
                Toast.makeText(MainActivityBeacon.this,
                        R.string.uid_name_cannot_be_blank, Toast.LENGTH_SHORT).show();
                resourcesStored=false;
            } else {
                if ((position<0)||(position>bleBeacons.size()-1))
                    Toast.makeText(MainActivityBeacon.this,
                            R.string.position_list_error, Toast.LENGTH_SHORT).show();
                else
                {
                    resourcesStored=true;
                    bleBeacon=bleBeacons.get(position);
                    BLEBeacon bleBeacon1=new BLEBeacon.Builder()
                            .setSeedBeacon(bleBeacon)
                            .setUid(input)
                            .build();
                    Toast.makeText(MainActivityBeacon.this,
                            R.string.select_cluster_for_beacon, Toast.LENGTH_SHORT).show();
                    final Intent intent =
                            new Intent(MainActivityBeacon.this, ClusterActivity.class);
                    intent.putExtra("eu.angel.bleembedded.lib.beacon.BLEBeacon", (Parcelable) bleBeacon1);
                    startActivity(intent);

                }
            }
        }
    }
}
