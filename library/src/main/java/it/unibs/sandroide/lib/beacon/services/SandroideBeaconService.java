/**
 * Copyright (c) 2018 University of Brescia, Alessandra Flammini, All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.unibs.sandroide.lib.beacon.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.beacon.BeaconTags;

/**
 * Created by Paolo Bellagente on 05/04/2018.
 */

public class SandroideBeaconService extends Service implements BeaconConsumer {
    public final static String BEACON_RELOAD_TAG = "it.unibs.sandroide.ACTION_BEACON_RELOAD_TAG";
    private final static String TAG = "SandroideBeaconService";
    //    todo: find a way to modify the following variable from the MainActivity
    private final static long ACTIVE_BETWEEN_SCAN_PERIOD = 10000L;
    private final static long ACTIVE_SCAN_PERIOD = 3000L;
    private final static long BACKGROUND_BETWEEN_SCAN_PERIOD = 10000L; //10 sec
    private final static long BACKGROUND_SCAN_PERIOD = 3000L; //3 secs
    //    private final static int IBEACON_MESSAGE_CODE = 533;
    public BeaconManager beaconManager = null;
    // ACTIONS
    LoadBeaconTagsReceiver tagsReloader = new LoadBeaconTagsReceiver();
    private Context context = this;
    private boolean isBackgroundmode;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.TAG, "create");
        isBackgroundmode = false;
        if (beaconManager == null)
            initBeaconManager();
        //register the tag reloader listener
        IntentFilter reloadTagFilter = new IntentFilter();
        reloadTagFilter.addAction(BEACON_RELOAD_TAG);
        registerReceiver(tagsReloader, reloadTagFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new RuntimeException("Please Override the onBind method for SandroideBeaconService");
    }

    /**
     * Load Beacon Tags definition from shared preferences
     */
    public void loadBeaconTags() {
        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this.getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BeaconTags.getInstance().initLayouts(beaconManager);
    }

    public void initBeaconManager() {

        BLEContext.initBLE(this.getApplicationContext());
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext()); // AltBeacon Line
        loadBeaconTags();

        //Let's set the powerSaver so can reduce battery drain when in background
//        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setBackgroundBetweenScanPeriod(BACKGROUND_BETWEEN_SCAN_PERIOD);
        beaconManager.setBackgroundScanPeriod(BACKGROUND_SCAN_PERIOD);

        beaconManager.setForegroundBetweenScanPeriod(ACTIVE_BETWEEN_SCAN_PERIOD);
        beaconManager.setForegroundScanPeriod(ACTIVE_SCAN_PERIOD);
    }


    @Override
    public void onDestroy() {
        stopBeaconsListener();
        unregisterReceiver(tagsReloader);
        Log.d(TAG, "Destroy SAndroideBEaconService");
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.e(TAG, "Please Override this");
        throw new RuntimeException("Please Override the onBeaconServiceConnect to do operate with beacons");
    }

    public void startBeaconsListener() {
        beaconManager.bind(this);
    }

    public void stopBeaconsListener() {
        try {
            for (Region region : beaconManager.getRangedRegions()) {
                beaconManager.stopRangingBeaconsInRegion(region);
            }
            Log.d(TAG, "End Beacon Scan------------");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (beaconManager != null)
            beaconManager.unbind(this);
    }

    /**
     * Receive the BEACON_RELOAD_TAG action intent and reload the tags definition from shared preferences.
     */
    private class LoadBeaconTagsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadBeaconTags();
        }
    }


}