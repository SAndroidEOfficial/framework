/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
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

package it.unibs.sandroide.lib.beacon.ui;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
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

import it.unibs.sandroide.lib.activities.SandroideApplication;
import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.beacon.BeaconTags;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgAltBeacon;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgBase;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgEddystoneTLM;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgEddystoneUID;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgEddystoneURL;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgGimbal;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgIBeacon;
import it.unibs.sandroide.lib.beacon.msg.BeaconMsgNearable;

/**
 * Allow to tag beacons and manage them (save, load default etc)
 */
public class BeaconTagActivity extends SandroideBaseActivity implements BeaconConsumer {

    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_LIST = Menu.FIRST + 1;
    private static final int MENU_EXPORT = Menu.FIRST + 2;
    private static final int MENU_DEFAULT = Menu.FIRST + 3;
    private final int LISTBEACONS_VIEW_ID = View.generateViewId();
    private final int LISTTAGS_VIEW_ID = View.generateViewId();
    private ListView lvTags;
    private ListView lvBeacons;

    private BeaconManager beaconManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        beaconManager = SandroideApplication.beaconManager; //BeaconManager.getInstanceForApplication(this.getApplicationContext());
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
                if (collection.size() > 0) {
                    Iterator<Beacon> it = collection.iterator();
                    while (it.hasNext()) {
                        Beacon beacon = it.next();
                        BeaconMsgBase b = null;
                        if (b == null) b = new BeaconMsgGimbal(beacon).parse();
                        if (b == null) b = new BeaconMsgNearable(beacon).parse();
                        if (b == null) b = new BeaconMsgEddystoneURL(beacon).parse();
                        if (b == null) b = new BeaconMsgEddystoneUID(beacon).parse();
                        if (b == null) b = new BeaconMsgEddystoneTLM(beacon).parse();
                        if (b == null) b = new BeaconMsgAltBeacon(beacon).parse();
                        if (b == null) b = new BeaconMsgIBeacon(beacon).parse();

                        if (b != null) {
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
        } catch (RemoteException e) {
        }
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

        layout.addView(lvBeacons, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        layout.addView(lvTags, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        setContentView(layout);
    }

    public void renderTabs() {
        // create tabs, attach pager & tab listener
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                switch (tab.getTag().toString()) {
                    case "tags":
                        lvBeacons.setVisibility(View.GONE);
                        lvTags.setVisibility(View.VISIBLE);
                        break;
                    case "beacons":
                        lvBeacons.setVisibility(View.VISIBLE);
                        lvTags.setVisibility(View.GONE);
                        break;
                }
                // show the given tab todo:delete
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
//todo: delete
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

    /**
     * Crate the option menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, MENU_ADD, Menu.NONE, "Load").setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_LIST, Menu.NONE, "Save").setIcon(android.R.drawable.ic_menu_upload);
        menu.add(0, MENU_EXPORT, menu.NONE, "Export").setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, MENU_DEFAULT, menu.NONE, "Defaults").setIcon(android.R.drawable.ic_menu_share);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * manage the menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_ADD:
                try {
//                    load the beacon tags
                    BeaconTags.getInstance().load(this);
                    Toast.makeText(this, "Tags reloaded", Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MENU_LIST:
                try {
//                    save the beacons tag
                    BeaconTags.getInstance().store(this);
                    Toast.makeText(this, "Tags saved", Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MENU_EXPORT:
                try {
                    BeaconTags.getInstance().export(this);
                    Toast.makeText(this, "Tags exported", Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            case MENU_DEFAULT:
                try {
                    BeaconTags.getInstance().loadFromResources(this);
                    Toast.makeText(this, "Defaults Tags loaded", Toast.LENGTH_SHORT);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error in  Tags Resource", Toast.LENGTH_SHORT);
                }
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
