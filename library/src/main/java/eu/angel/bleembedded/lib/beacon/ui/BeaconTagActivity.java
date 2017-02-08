package eu.angel.bleembedded.lib.beacon.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import java.util.Collection;
import java.util.Iterator;

import eu.angel.bleembedded.lib.activities.SandroideBaseActivity;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgAltBeacon;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgBase;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneTLM;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneUID;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgEddystoneURL;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgGimbal;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgIBeacon;
import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgNearable;
import eu.angel.bleembedded.lib.beacon.BeaconTags;

/**
 * Created by giova on 25/01/2017.
 */

public class BeaconTagActivity extends SandroideBaseActivity implements BeaconConsumer {

    private final int LISTBEACONS_VIEW_ID = View.generateViewId();
    private final int LISTTAGS_VIEW_ID = View.generateViewId();

    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_LIST = Menu.FIRST + 1;

    private ListView lvTags;
    private ListView lvBeacons;

    private BeaconManager beaconManager;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // render UI
        renderListViews();
        renderTabs();

        // attach adapters to listviews
        lvBeacons.setAdapter(BeaconTags.getInstance().getBeaconsListAdapter());
        lvTags.setAdapter(BeaconTags.getInstance().getTagsListAdapter());

        // start beacon monitoring
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        try {
            // load tagged beacons from shared preferences
            BeaconTags.getInstance().load(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BeaconTags.getInstance().initLayouts(beaconManager);

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
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (collection.size()>0) {
                    Iterator<Beacon> it = collection.iterator();
                    while(it.hasNext()) {
                        Beacon beacon = it.next();
                        BeaconMsgBase b = null;
                        if (b==null) b = new BeaconMsgGimbal(beacon).parse();
                        if (b==null) b = new BeaconMsgNearable(beacon).parse();
                        if (b==null) b = new BeaconMsgEddystoneURL(beacon).parse();
                        if (b==null) b = new BeaconMsgEddystoneUID(beacon).parse();
                        if (b==null) b = new BeaconMsgEddystoneTLM(beacon).parse();
                        if (b==null) b = new BeaconMsgAltBeacon(beacon).parse();
                        if (b==null) b = new BeaconMsgIBeacon(beacon).parse();

                        if (b!=null) {
                            final BeaconMsgBase beac = b;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    BeaconTags.getInstance().beaconInRange(beac);
                                }
                            });
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private void renderListViews() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.TOP);

        lvBeacons = new ListView(this);
        lvBeacons.setId(LISTBEACONS_VIEW_ID);
        lvBeacons.setDivider(null);
        lvBeacons.setDividerHeight(0);

        lvTags = new ListView(this);
        lvTags.setId(LISTTAGS_VIEW_ID);
        lvTags.setVisibility(View.INVISIBLE);
        lvTags.setDivider(null);
        lvTags.setDividerHeight(0);

        layout.addView(lvBeacons, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1));
        layout.addView(lvTags, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1));

        setContentView(layout);
    }

    public void renderTabs() {
        // create tabs, attach pager & tab listener
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                switch(tab.getTag().toString()){
                    case "tags":
                        lvBeacons.setVisibility(View.GONE);
                        lvTags.setVisibility(View.VISIBLE);
                        break;
                    case "beacons":
                        lvBeacons.setVisibility(View.VISIBLE);
                        lvTags.setVisibility(View.GONE);
                        break;
                }
                // show the given tab
                //mViewPager.setCurrentItem(tab.getPosition());
            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        actionBar.addTab(actionBar.newTab().setTag("beacons").setText("Beacons nearby").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setTag("tags").setText("Tags").setTabListener(tabListener));

        /*mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });*/
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, MENU_ADD, Menu.NONE, "Load").setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_LIST, Menu.NONE, "Save").setIcon(android.R.drawable.ic_menu_upload);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_ADD:
                try {
                    BeaconTags.getInstance().load(this);
                    Toast.makeText(this,"Tags reloaded",Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MENU_LIST:
                try {
                    BeaconTags.getInstance().store(this);
                    Toast.makeText(this,"Tags saved",Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        BeaconTags.getInstance().getBeaconsListAdapter().notifyDataSetChanged();
        return false;
    }

    @Override
    protected void onDestroy() {
        if (beaconManager.isBound(this)) beaconManager.unbind(this);
        super.onDestroy();
    }
}
