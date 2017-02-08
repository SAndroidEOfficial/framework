package it.unibs.sandroide.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgBase;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgNearable;
import it.unibs.sandroide.lib.beacon.BeaconTags;
import it.unibs.sandroide.lib.beacon.notifier.TagMonitorNotifier;
import it.unibs.sandroide.lib.beacon.notifier.TagRangeNotifier;

/**
 * Created by giova on 11/01/2017.
 */

public class MainActivityBeacon extends SandroideBaseActivity implements BeaconConsumer {
    protected static final String TAG = "MainActivityBeacon";
    private BeaconManager beaconManager;

    private ArrayList<String> logLines = new ArrayList<String>();
    private ListView mLogList;
    private ArrayAdapter logLinesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        mLogList=(ListView) findViewById(R.id.logList);
        logLinesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, logLines);
        mLogList.setAdapter(logLinesAdapter);

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BeaconTags.getInstance().initLayouts(beaconManager,"near");
        BeaconTags.getInstance().initLayouts(beaconManager,"sky");
        BeaconTags.getInstance().initLayouts(beaconManager,"ice");
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        if (beaconManager.isBound(this)) beaconManager.unbind(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startTagConfiguration(View v) {
        BeaconTags.getInstance().startTaggingActivity(this);
    }

    private void addLogLine(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            logLines.add(0,String.format("%s %s",new Date().getSeconds(),s));
            if (logLines.size()>10) logLines.remove(logLines.size()-1);
            logLinesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBeaconServiceConnect() {

        BeaconTags.getInstance().clearNotifiers(beaconManager);

        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "near", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("NEAR Beacon in range for tag:%s, key:%s, ids:%s","near", b.getParserSimpleClassname(), b.getIdentifiers().toString()));

                BeaconMsgBase beac = new BeaconMsgNearable(b).parse();
                if (beac!=null) {
                    addLogLine(String.format("Found my nearable: %s",beac.toString()));
                    Log.i("MainActivityBeacon",String.format("Found my nearable: %s",beac.toString()));
                } else {
                    addLogLine(String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                    Log.e("MainActivityBeacon",String.format("This is not a nearable message: %s",b.getKeyIdentifier()));
                }
            }
        });

        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "sky", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("SKY Beacon in range for tag:%s, key:%s, ids:%s","sky", b.getParserSimpleClassname(), b.getIdentifiers().toString()));
            }
        });
        BeaconTags.getInstance().addRangeNotifierForTag(beaconManager, "ice", new TagRangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(BeaconMsgBase b) {
                addLogLine(String.format("ICE Beacon in range for tag:%s, key:%s, ids:%s","ice", b.getParserSimpleClassname(), b.getIdentifiers().toString()));
            }
        });

        BeaconTags.getInstance().addMonitorNotifier(beaconManager, "sky", new TagMonitorNotifier(){
            @Override
            public void didEnterTag(String tag) {
                addLogLine(String.format("ENTER tag %s",tag));
            }

            @Override
            public void didExitTag(String tag) {
                addLogLine(String.format("EXIT tag %s",tag));
            }

            @Override
            public void didDetermineStateForTag(int i, String tag) {
                addLogLine(String.format("didDetermineStateForTag tag %s, %d",tag,i));
            }
        });

        BeaconTags.getInstance().addMonitorNotifier(beaconManager, "ice", new TagMonitorNotifier(){
            @Override
            public void didEnterTag(String tag) {
                addLogLine(String.format("ENTER tag %s",tag));
            }

            @Override
            public void didExitTag(String tag) {
                addLogLine(String.format("EXIT tag %s",tag));
            }

            @Override
            public void didDetermineStateForTag(int i, String tag) {
                addLogLine(String.format("didDetermineStateForTag tag %s, %d",tag,i));
            }
        });

        /*
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void onTaggedBeaconReceived(Collection<Beacon> collection, Region region) {
                //BLEBeacon b = bleBeacons.iterator().next();
                //final String msg = String.format("%s: %s",new Date().toString(),b.getDataFields().toString()+b.getDistance());
                if (collection.size()>0) {
                    Iterator<Beacon> it = collection.iterator();
                    while(it.hasNext()) {
                        String str = "";
                        Beacon beacon = it.next();
                        Beacon b = null;
                        if (b==null) b = new BeaconMsgGimbal(beacon).parse();
                        if (b==null) b = new BeaconMsgEddystoneURL(beacon).parse();
                        if (b==null) b = new BeaconMsgNearable(beacon).parse();

                        if (b!=null) {
                            str = b.toString();
                            //str = String.format("%s: typecode:%s, id:%s, distance:%f,\nd:%s", new Date().toString(), Long.toHexString(b.getBeaconTypeCode()), b.toString(), b.getDistance(), b.getDataFields().toString());
                            //str = String.format("%s: id:%s, distance:%f,\nd:%s", new Date().toString(), b.toString(), b.getDistance(), b.getDataFields().toString());
                        }

                        if (str!="") {
                            final String msg = str;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView tv = (TextView) findViewById(R.id.tvBeacon);
                                    tv.setText(msg);
                                }
                            });
                            Log.d("MainActivityBeacon", msg);
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }

        */
    }

}
