package eu.angel.bleembedded.lib.beacon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgAltBeacon;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgBase;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneTLM;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneUID;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneURL;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgGimbal;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgIBeacon;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgNearable;
import eu.angel.bleembedded.lib.beacon.notifier.TagMonitorNotifier;
import eu.angel.bleembedded.lib.beacon.notifier.TagRangeNotifier;
import eu.angel.bleembedded.lib.beacon.ui.BeaconTagActivity;
import eu.angel.bleembedded.lib.beacon.ui.BeaconsListAdapterLinked;
import eu.angel.bleembedded.lib.beacon.ui.TagsListAdapterLinked;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by giova on 25/01/2017.
 */

public class BeaconTags {

    private static BeaconTags instance;

    // these are adapter for listview used in BeaconTagActivity
    private BeaconsListAdapterLinked beaconsListAdapter;
    private TagsListAdapterLinked tagsListAdapter;

    // this is load/stored from shared preferences as json object.
    private LinkedHashMap<String,ArrayList<String>> beaconsToTags = new LinkedHashMap<>();

    // used by UI in beaconsListAdapter, so elements in this map must not be removed. They will be made invisible when last message from beacon is too old
    private LinkedHashMap<String,BeaconMsgBase> beaconsInRange = new LinkedHashMap<>();

    // used by UI in tagsListAdapter, so elements in this map must not be removed. They should be left empty so tagsListAdapter may remove them from UI (making them invisible)
    private LinkedHashMap<String,ArrayList<String>> tagsToBeacons = new LinkedHashMap<>();

    private ArrayList<String> customParsers = new ArrayList();

    public TagsListAdapterLinked getTagsListAdapter(){
        if (tagsListAdapter==null)
            tagsListAdapter = new TagsListAdapterLinked(tagsToBeacons);
        return tagsListAdapter;
    }

    public BeaconsListAdapterLinked getBeaconsListAdapter(){
        if (beaconsListAdapter==null)
            beaconsListAdapter = new BeaconsListAdapterLinked(beaconsInRange);
        return beaconsListAdapter;
    }

    public static BeaconTags getInstance() {
        if (instance==null) {
            instance = new BeaconTags();
        }
        return instance;
    }

    public static void startTaggingActivity(Context ctx) {
        Intent intent = new Intent(ctx, BeaconTagActivity.class);
        ctx.startActivity(intent);
    }

    public void load(Context ctx) throws JSONException {
        SharedPreferences prefs = ctx.getSharedPreferences("BEACON_TAGS", MODE_PRIVATE);
        JSONObject config = new JSONObject(prefs.getString("config","{}"));

        // empty beaconsToTags: not bound to UI. elements may be safely removed
        beaconsToTags.clear();

        // empty tagsToBeacons: this is bound to UI as tagsListAdapter datasource, so elements must be cleared, not removed!!
        Iterator<String> itkeys = tagsToBeacons.keySet().iterator();
        while (itkeys.hasNext()) {
            tagsToBeacons.get(itkeys.next()).clear();
        }

        // fill in beaconsToTags and tagsToBeacons maps from json object
        itkeys = config.keys();
        while (itkeys.hasNext()) {
            String beaconk = itkeys.next();
            JSONArray jsonArr = config.getJSONArray(beaconk);
            ArrayList<String> cList = beaconsToTags.get(beaconk);
            if (cList == null) {
                cList = new ArrayList<>();
                beaconsToTags.put(beaconk, cList);
            }
            for (int i = 0; i < jsonArr.length(); i++) {
                String clkey = jsonArr.getString(i);
                if (!cList.contains(clkey)) cList.add(clkey);

                ArrayList<String> bList = tagsToBeacons.get(clkey);
                if (bList == null) {
                    bList = new ArrayList<>();
                    tagsToBeacons.put(clkey, bList);
                }

                if (!bList.contains(beaconk)) bList.add(beaconk);
            }
        }
    }

    public boolean store(Context ctx) throws JSONException {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("BEACON_TAGS", MODE_PRIVATE).edit();
        JSONObject newconfig = new JSONObject();
        Iterator<String> itbeac = beaconsToTags.keySet().iterator();
        while (itbeac.hasNext()) {
            String beaconk = itbeac.next();
            ArrayList<String> arrTags = beaconsToTags.get(beaconk);
            if (arrTags!=null && arrTags.size()>0) {
                newconfig.put(beaconk,new JSONArray());
                Iterator<String> ittags = arrTags.iterator();
                while (ittags.hasNext()) {
                    newconfig.getJSONArray(beaconk).put(ittags.next());
                }
            }
        }
        editor.putString("config",newconfig.toString());
        return editor.commit();
    }

    public void removeTag(String key) {
        ArrayList<String> arr = getBeaconsForTag(key);
        for(int i=0; i<arr.size(); i++) {
            ArrayList<String> tags = getTagsForBeacon(arr.get(i));
            tags.remove(tags.indexOf(key));
        }
        arr.clear();
        getBeaconsListAdapter().notifyDataSetChanged();
        getTagsListAdapter().notifyDataSetChanged();
    }

    public void changeTagsForBeacon(String beaconk, String strTags) {

        ArrayList<String> oldTags = getTagsForBeacon(beaconk);
        if (oldTags==null) oldTags = new ArrayList<>();
        ArrayList<String> newTags = new ArrayList<>();
        for (String t: strTags.split(",")) {
            t=t.trim();
            if (t.length()>0) {
                newTags.add(t);
            }
        }

        Iterator<String> itkeys = oldTags.iterator();
        while (itkeys.hasNext()) {
            String t = itkeys.next();
            if (!newTags.contains(t)) { // remove tag from beacon and beacon from tag
                oldTags.remove(oldTags.indexOf(t));
                getBeaconsForTag(t).remove(getBeaconsForTag(t).indexOf(beaconk));
                itkeys = oldTags.iterator();
            }
        }

        itkeys = newTags.iterator();
        while (itkeys.hasNext()) {
            String t = itkeys.next();
            if (!oldTags.contains(t)) { // add tag to beacon and beacon to tag
                oldTags.add(t);
                ArrayList<String> beac = getBeaconsForTag(t);
                if (beac==null) beac = new ArrayList<>();
                beac.add(beaconk);
                tagsToBeacons.put(t,beac);
            }
        }

        if (oldTags.size()>0) {
            beaconsToTags.put(beaconk,oldTags);
        } else {
            beaconsToTags.remove(beaconk);
        }

        getTagsListAdapter().notifyDataSetChanged();
        getBeaconsListAdapter().notifyDataSetChanged();
    }

    public ArrayList<String> getTagsForBeacon(String beaconkey) {
        return beaconsToTags.get(beaconkey);
    }

    public int getTagsNumberForBeacon(String beaconkey) {
        ArrayList<String> arr = beaconsToTags.get(beaconkey);
        return arr==null?0:arr.size();
    }

    public ArrayList<String> getBeaconsForTag(String tagkey) {
        return tagsToBeacons.get(tagkey);
    }

    public String[] getTagNames() {
        return tagsToBeacons.keySet().toArray(new String[0]);
    }

    public void beaconInRange(BeaconMsgBase b) {
        beaconsInRange.put(b.getKeyIdentifier(),b);
        getBeaconsListAdapter().notifyDataSetChanged();
    }

    public ArrayList<String> getParsersNameForTag(String tag) {
        ArrayList<String> parsersName = new ArrayList<>();
        ArrayList<String> beacons = tagsToBeacons.get(tag);
        if (beacons!=null && beacons.size()>0) {
            for (int i=0;i<beacons.size(); i++) {
                String p =  beacons.get(i).split("_")[0];
                if (!parsersName.contains(p)) parsersName.add(p);
        }
        }
        return parsersName;
    }

    public void addCustomParser(Class c) {
        if (!customParsers.contains(c.getName()))
            customParsers.add(c.getName());
    }

    public ArrayList<String>  getAllParsers(){
        ArrayList<String> arr = new ArrayList<>();
        arr.addAll(customParsers);

        arr.add(BeaconMsgGimbal.class.getCanonicalName());
        arr.add(BeaconMsgNearable.class.getCanonicalName());
        arr.add(BeaconMsgEddystoneURL.class.getCanonicalName());
        arr.add(BeaconMsgEddystoneUID.class.getCanonicalName());
        arr.add(BeaconMsgEddystoneTLM.class.getCanonicalName());
        arr.add(BeaconMsgAltBeacon.class.getCanonicalName());
        arr.add(BeaconMsgIBeacon.class.getCanonicalName());

        return arr;
    }

    public void initLayouts(BeaconManager mgr) {
        initLayouts(mgr, getAllParsers());
    }

    public void initLayouts(BeaconManager mgr, String tag) {
        initLayouts(mgr,getParsersNameForTag(tag));
    }

    public void initLayouts(BeaconManager mgr, ArrayList<String> parsers) {
        // load from sharedpref if not yet loaded

        mgr.getBeaconParsers().clear();
        // add parser layout to manager
        for (int i=0; i<parsers.size(); i++) {
            try {
                String pname = parsers.get(i);
                Class clazz = Class.forName(pname);
                Field layoutField = clazz.getField("MSG_LAYOUT");
                mgr.getBeaconParsers().add(new BeaconParser(pname).setBeaconLayout(layoutField.get(new String[0]).toString()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearNotifiers(BeaconManager mgr) {
        mgr.removeAllRangeNotifiers();
        mgr.removeAllMonitorNotifiers();
        mgr.getBeaconParsers().clear();
        mgr.getMonitoredRegions().clear();
        mgr.getRangedRegions().clear();
    }

    private Region computeRegionForBeacons(ArrayList<String> beaconsInTag, String regionName) {
        // All beacons corresponding to layouts initialized are in the region.
        Region r = new Region(regionName, null,null,null);
        if (beaconsInTag!=null && beaconsInTag.size() == 1) {
            // When tag includes ONE beacon only, then it has very sense to compute the most strict region for that beacon.
            // This allows the monitoring service to not wake up our code when a beacon with same parser, but different id comes
            ArrayList<Identifier> ids = new ArrayList<>();
            if (beaconsInTag.size() > 0) {
                String[] identifiers = beaconsInTag.get(0).split("_")[1].replace("[", "").replace("]", "").split(",");
                for (int i = 0; i < identifiers.length; i++) {
                    ids.add(Identifier.parse(identifiers[i].trim()));
                }
                r = new Region(regionName, ids);
            }
        } else {
            // TODO: maybe instead of using (null,null,null) as Region, an attempt to compute the most strict region from all identifiers of beacons in the tag, may be done
            // However, even if this would work, it is sufficient that two beacons in the tag have different first identifier, to let the region be (null,null,null)
            // Thus, this computation gets a sense only if user assigns tags to beacons coherently with beacon identifiers, major and minor versions.
        }
        return r;
    }

    public void addRangeNotifierForTag(BeaconManager mgr, final String tag, final TagRangeNotifier tn) {
        final ArrayList<String> beaconsInTag = getBeaconsForTag(tag);
        final Region computedRegion = computeRegionForBeacons(beaconsInTag, tag);

        if (beaconsInTag!=null && beaconsInTag.size()>0) {
            mgr.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                    if (collection.size() > 0) {
                        Iterator<Beacon> it = collection.iterator();
                        while (it.hasNext()) {
                            BeaconMsgBase beac = new BeaconMsgBase(it.next());
                            if (beaconsInTag.contains(beac.getKeyIdentifier())) {
                                //invoke callback on current BeaconMsgBase
                                tn.onTaggedBeaconReceived(beac);
                            }
                        }
                    }
                }
            });

            try {
                mgr.startRangingBeaconsInRegion(computedRegion);
            } catch (RemoteException e) {
            }
        }
    }


    public void addMonitorNotifier(BeaconManager mgr, final String tag, final TagMonitorNotifier tn) {
        final ArrayList<String> beaconsInTag = getBeaconsForTag(tag);
        final Region computedRegion = computeRegionForBeacons(beaconsInTag, tag);
        final boolean computedIsGlobalRegion = computedRegion.hasSameIdentifiers(new Region("region",null,null,null));

        if (beaconsInTag!=null && beaconsInTag.size()>0) {
            mgr.addMonitorNotifier(new MonitorNotifier() {
                @Override
                public void didEnterRegion(Region region) {
                    if (computedIsGlobalRegion || region.hasSameIdentifiers(computedRegion))
                        tn.didEnterTag(tag);
                }

                @Override
                public void didExitRegion(Region region) {
                    if (computedIsGlobalRegion || region.hasSameIdentifiers(computedRegion))
                        tn.didExitTag(tag);
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
                    if (computedIsGlobalRegion || region.hasSameIdentifiers(computedRegion))
                        tn.didDetermineStateForTag(i, tag);
                }
            });

            try {
                mgr.startMonitoringBeaconsInRegion(computedRegion);
            } catch (RemoteException e) {
            }
        }
    }
}
