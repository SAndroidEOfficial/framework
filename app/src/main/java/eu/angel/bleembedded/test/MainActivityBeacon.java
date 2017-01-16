package eu.angel.bleembedded.test;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Collection;
import java.util.Date;

import eu.angel.bleembedded.R;
import eu.angel.bleembedded.lib.activities.SandroideBaseActivity;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconRangeNotifier;

/**
 * Created by giova on 11/01/2017.
 */

public class MainActivityBeacon extends SandroideBaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        BLEBeaconManager.startBeaconRangeNotifier(new BLEBeaconRangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<BLEBeacon> bleBeacons,
                                                BLEBeaconRegion region,
                                                final BLEBeaconCluster bleBeaconCluster) {
                final String msg = String.format("%s: beacon cluster %s in range",new Date().toString(),bleBeaconCluster.getUniqueId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.tvBeacon);
                        tv.setText(msg);
                    }
                });
                Log.d("MainActivityBeacon", msg);
            }
        });


    }
}
