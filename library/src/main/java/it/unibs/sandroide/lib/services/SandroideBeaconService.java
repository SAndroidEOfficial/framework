package it.unibs.sandroide.lib.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.beacon.BeaconTags;

/**
 * Created by Paolo Bellagente on 05/04/2018.
 */

public class SandroideBeaconService extends Service implements BeaconConsumer {
    private final static String TAG = "SandroideBeaconService";
    private final static long ACTIVE_BETWEEN_SCAN_PERIOD = 100L;
    private final static long ACTIVE_SCAN_PERIOD = 2000L;
    private final static long BACKGROUND_BETWEEN_SCAN_PERIOD = 120000L; //2 mins
    private final static long BACKGROUND_SCAN_PERIOD = 20000L; //20 secs
    private final static int IBEACON_MESSAGE_CODE = 533;
    private final static int MINIMUM_CONFIDENCE_VALUE_ON_FOOT_ACTIVITY = 25; //We want to listen for light motion
    private final static String NAME_REGION = "RegionDefault";
    private BeaconManager beaconManager = null;
    private Context context = this;
    private Region regionToScan;
    private HashMap<String, String> mapDeviceArea;
    private boolean isBackgroundmode;
    private String idStructure;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.TAG, "create");
        isBackgroundmode = false;
        if (beaconManager == null)
            initBeaconManager();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initBeaconManager() {

        BLEContext.initBLE(this.getApplicationContext());
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext()); // AltBeacon Line
        BeaconTags.getInstance().initLayouts(beaconManager);


        //Let's set the powerSaver so can reduce battery drain when in background
//        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setBackgroundBetweenScanPeriod(BACKGROUND_BETWEEN_SCAN_PERIOD);
        beaconManager.setBackgroundScanPeriod(BACKGROUND_SCAN_PERIOD);

        beaconManager.setForegroundBetweenScanPeriod(ACTIVE_BETWEEN_SCAN_PERIOD);
        beaconManager.setForegroundScanPeriod(ACTIVE_SCAN_PERIOD);


//        beaconManager.addRangeNotifier(new RangeNotifier() {
//            @Override
//            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
//                if (collection.size() > 0) {
//
//                    SensorizedStructurePositionDetection detection = new SensorizedStructurePositionDetection(-1, 4);
//
//                    Iterator<Beacon> it = collection.iterator();
//                    String str;
//                    while (it.hasNext()) {
//
//                        Beacon beacon = it.next();
//                        Beacon b = new BeaconMsgIBeacon(beacon).parse();
//
//
//                        if (b != null && b.getBeaconTypeCode() == IBEACON_MESSAGE_CODE) {
//                            String id = getIdentifierFromBeacon(b);
//                            String areaIdDevice = null;
//                            if (mapDeviceArea.get(id) != null && id != null) {
//                                areaIdDevice = mapDeviceArea.get(id);
//                                detection.insertDeviceDetectedInArea(areaIdDevice, beacon);
//                            }
//
////                            str = String.format("%s: id:%s, distance:%f,\nd:%s", new Date().toString(), b.toString(), b.getDistance(), b.getRssi(), b.getDataFields().toString());
//                            str = String.format(" id:%s, distance:%f, RSSI: %d", id, b.getDistance(), b.getRssi());
//
//                            CommonUtils.print(str);
//                        }
//                    }
//                    positionDetectionDecisionMaker.insertPositionDetection(detection);
//                    launchBroadcastIntentDetectionsDataUpdates();
//
//                }
//            }
//        });
    }
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//    }


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
//        if (result != null) {
//            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
//            CommonUtils.print("NEW INTENT, ENTRO");
//            if (detectedActivities != null) {
//                for (DetectedActivity detectedActivity : detectedActivities) {
//                    if (detectedActivity.getType() == DetectedActivity.ON_FOOT
//                            && detectedActivity.getConfidence() > MINIMUM_CONFIDENCE_VALUE_ON_FOOT_ACTIVITY) {
//                        CommonUtils.print("A PIEDI, LIV CONFIDENCE: " + detectedActivity.getConfidence());
//                        Toast.makeText(context, "A PIEDI, LIV CONFIDENCE: " + detectedActivity.getConfidence(), Toast.LENGTH_SHORT).show();
//                        setBackgroundMode(false);
//
//                    }
//                }
//            }
//        }
//        return START_NOT_STICKY;
//    }


    @Override
    public void onDestroy() {
        stopBeaconsListener();
        Log.d(TAG, "Destroy SAndroideBEaconService");
        super.onDestroy();
    }


//    private void populateMappingAreaDevices(ArrayList<StructureArea> areasArray) {
//        DevicesDatabaseHelper devicesDatabaseHelper = new DevicesDatabaseHelper(context);
//        for (StructureArea area : areasArray) {
//            ArrayList<StructureDevice> devices = devicesDatabaseHelper.getStructureDevicesInArea(area.getId());
//            if (devices != null)
//                for (StructureDevice device : devices) {
//                    String internalId = null;
//                    try {
//                        internalId = CommonUtils.getInternalDeviceIdentifier(device.getIdentifier());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    if (internalId != null)
//                        mapDeviceArea.put(internalId, area.getId());
//                }
//        }
//
//    }


    private String getIdentifierFromBeacon(Beacon beacon) {
        int beaconTypeCode = beacon.getBeaconTypeCode();
        switch (beaconTypeCode) {
            case IBEACON_MESSAGE_CODE:
                return (beacon.getId1().toString() + beacon.getId2().toString() + beacon.getId3().toString()).toLowerCase();
            default:
                return null;
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.e(TAG,"Please Override this");
        throw new RuntimeException("Please Override the onBeaconServiceConnect to do operate with beacons");
    }

    public void startBeaconsListener() {
        beaconManager.bind(this);
    }

    public void stopBeaconsListener() {
        try {
            beaconManager.stopRangingBeaconsInRegion(regionToScan);
            Log.d(TAG,"End Beacon Scan------------");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (beaconManager != null)
            beaconManager.unbind(this);
    }

//    private void launchBroadcastIntentDetectionsDataUpdates() {
//        Intent intentCommunication = new Intent();
//        intentCommunication.setAction(Constants.STRUCTURE_POSITION_DETECTION_UPDATES_DATA_EXTRA);
//        intentCommunication.putParcelableArrayListExtra(Constants.STRUCTURE_POSITION_DETECTION_LIST_AREAS_EXTRA, positionDetectionDecisionMaker.getOrderedAreaListByPresenceProbability());
//        intentCommunication.putExtra(Constants.STRUCTURE_POSITION_DETECTION_STABLE_POSITION_EXTRA, positionDetectionDecisionMaker.isPositionStable());
//        CommonUtils.sendLocalBroadcastMessage(context, intentCommunication);
//    }

//    private void launchBroadcastIntentBackgroundModeUpdates(boolean enable) {
//        Intent intentCommunicationBackground = new Intent();
//        intentCommunicationBackground.setAction(Constants.STRUCTURE_POSITION_DETECTION_BACKGROUNDMODE_UPDATES_EXTRA);
//        intentCommunicationBackground.putExtra(Constants.STRUCTURE_POSITION_DETECTION_BACKGROUNDMODE_VALUE_EXTRA, enable);
//        CommonUtils.sendLocalBroadcastMessage(context, intentCommunicationBackground);
//    }

//    public synchronized void setBackgroundMode(boolean enable) {
//        if (isBackgroundmode != enable) {
//            CommonUtils.print("BACKGROUND MODE: " + enable);
//            Toast.makeText(context, "BACKGROUND MODE: " + enable, Toast.LENGTH_SHORT).show();
//            beaconManager.setBackgroundMode(enable);
//            isBackgroundmode = enable;
//            launchBroadcastIntentBackgroundModeUpdates(enable);
//        }
//    }

//    public class LocalBinder extends Binder {
//        public StructureLocationDetectionService getService() {
//            return StructureLocationDetectionService.this;
//        }
//    }
}