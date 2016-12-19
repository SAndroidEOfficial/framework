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
package eu.angel.bleembedded.lib.beacon;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconBeaconNotifier;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconMonitorNotifier;
import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconRangeNotifier;

/**
 * The static class that handles the functions useful to listen for the BLE Beacons.
 */
public class BLEBeaconManager {

    private static final String TAG = "BLEBeaconManager";


    private static BeaconManager beaconManager;
    private static boolean isBeaconRanging=false;
    private static boolean isBeaconMonitoring=false;
    private static boolean isBeaconServiceConnected=false;
    private static boolean isBeaconServiceConnectedForConfig=false;
    private static boolean isBeaconServiceConnectedForConfigRequired=false;

    private static List<BLEBeaconParser> bleBeaconParsers;
    //TODO: move this to BLEBeaconParser e settato private
    public static List<BLEBeaconParser> allBleBeaconParsers;
    private static List<BLEBeaconStarter> bleBeaconMonitoringStarters;
    private static List<BLEBeaconStarter> bleBeaconRangingStarters;
    /**
     * {@link RangeNotifier} filtered callback
     *
     */
    private static BLEBeaconRangeNotifier beaconRangeNotifier;
    /**
     * {@link MonitorNotifier}  filtered callback
     *
     */
    private static BLEBeaconMonitorNotifier beaconMonitorNotifier;

    //clusters configuring section
    private static BLEBeaconBeaconNotifier bleBeaconBeaconNotifier;
    private static List<BLEBeacon> configBLEBeacons;



    //region beacon by Cluster section
    //----------------------------------------------------------------------------------------------
    //region listening section
    //----------------------------------------------------------------------------------------------
    private static List<BLEBeaconCluster> bleBeaconClustersRanging;
    private static List<BLEBeaconCluster> bleBeaconClustersMonitoring;


    /**
     * The generic function used to make available the activation of the cluster to be ranged
     *
     */
    static BLEBeaconGenericFunction bleBeaconClusterStartingRangingFunction = new BLEBeaconGenericFunction() {
        @Override
        public void execute(int position) {
            if (bleBeaconClustersRanging!=null)
            {
                BLEBeaconCluster bleBeaconCluster=bleBeaconClustersRanging.get(position);
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    activateBeaconRegionRanging(bleBeaconRegion);
                }
            }
        }
    };

    /**
     * The generic function used to make available the activation of the cluster to be monitored
     *
     */
    static BLEBeaconGenericFunction bleBeaconClusterStartingMonitoringFunction = new BLEBeaconGenericFunction() {
        @Override
        public void execute(int position) {
            if (bleBeaconClustersMonitoring!=null)
            {
                BLEBeaconCluster bleBeaconCluster=bleBeaconClustersMonitoring.get(position);
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    activateBeaconRegionMonitoring(bleBeaconRegion);
                }
            }
        }
    };


    /**
     * Static function which get beacon parser ({@link BLEBeaconParser#getAllParsers()}) and
     * the Clusters stored ({@link XmlHandler#parseBLEBeaconClusters(android.content.Context)}). The
     * parser needed for the handling the stored clusters are then passed to Altbeacon library to
     * parse and filter the beacon received. The Clusters are used as global list and for each parsable
     * {@link BLEBeaconCluster} the related {#link eu.angel.bleembedded.lib.beacon.BLEBeaconStarter}
     * is created. At last the Ranging function is trigger by the
     * {@link BLEBeaconManager#startRangingBeaconFunction(java.util.List, boolean)}
     *
     */
    private static void getBeaconsClustersRanging()
    {
        allBleBeaconParsers=BLEBeaconParser.getAllParsers();
        List<BLEBeaconCluster> bleBeaconClusters=
                XmlHandler.parseBLEBeaconClusters(BLEContext.context);
        List<String> parsers = new ArrayList<>();
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);
        List<BLEBeaconStarter> bleBeaconStarters = new ArrayList<>();
        BLEBeaconCluster bleBeaconCluster;
        if (bleBeaconClusters!=null)
        {
            for (int i=0;i<bleBeaconClusters.size();i++)
            {
                bleBeaconCluster=bleBeaconClusters.get(i);
                cluster_region_for:
                for (BLEBeaconRegion bleBeaconRegion: bleBeaconCluster.getBleBeaconRegions())
                {
                    for (String regionParser:bleBeaconRegion.getRegionsParsers())
                    {
                        if (parsers.size()>0)
                        {
                            for (String parser:parsers)
                            {
                                if (parser.equalsIgnoreCase(regionParser))
                                    continue cluster_region_for;
                            }
                        }
                        parsers.add(regionParser);
                    }
                }
                bleBeaconStarters.add(new BLEBeaconStarter
                        (bleBeaconClusterStartingRangingFunction, i));
            }
        }

        addBLEParsersByName(parsers);
        BLEBeaconManager.bleBeaconClustersRanging=bleBeaconClusters;
        startRangingBeaconFunction(bleBeaconStarters, isBeaconServiceConnected(beaconManager));
    }

    /**
     * Static function which get beacon parser ({@link BLEBeaconParser#getAllParsers()}) and
     * the Clusters stored ({@link XmlHandler#parseBLEBeaconClusters(android.content.Context)}). The
     * parser needed for the handling the stored clusters are then passed to Altbeacon library to
     * parse and filter the beacon received. The Clusters are used as global list and for each parsable
     * {@link BLEBeaconCluster} the related {#link eu.angel.bleembedded.lib.beacon.BLEBeaconStarter}
     * is created. At last the Monitoring function is trigger by the
     * {@link BLEBeaconManager#startMonitoringBeaconFunction(java.util.List, boolean)}
     *
     */
    private static void getBeaconsClustersMonitoring()
    {
        allBleBeaconParsers=BLEBeaconParser.getAllParsers();
        List<BLEBeaconCluster> bleBeaconClusters=
                XmlHandler.parseBLEBeaconClusters(BLEContext.context);
        List<String> parsers = new ArrayList<>();
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);

        List<BLEBeaconStarter> bleBeaconStarters = new ArrayList<>();

        BLEBeaconCluster bleBeaconCluster;
        if (bleBeaconClusters!=null)
        {
            for (int i=0;i<bleBeaconClusters.size();i++)
            {
                bleBeaconCluster=bleBeaconClusters.get(i);
                cluster_region_for:
                for (BLEBeaconRegion bleBeaconRegion: bleBeaconCluster.getBleBeaconRegions())
                {
                    for (String regionParser:bleBeaconRegion.getRegionsParsers())
                    {
                        if (parsers.size()>0)
                        {
                            for (String parser:parsers)
                            {
                                if (parser.equalsIgnoreCase(regionParser))
                                    continue cluster_region_for;
                            }
                        }
                        parsers.add(regionParser);
                    }
                }
                bleBeaconStarters.add(new BLEBeaconStarter
                        (bleBeaconClusterStartingMonitoringFunction, i));
            }
        }

        addBLEParsersByName(parsers);
        BLEBeaconManager.bleBeaconClustersMonitoring=bleBeaconClusters;
        startMonitoringBeaconFunction(bleBeaconStarters, isBeaconServiceConnected(beaconManager));
    }



    private static MonitorNotifier monitorNotifier = new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
            for (BLEBeaconCluster bleBeaconCluster:bleBeaconClustersMonitoring)
            {
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    if (bleBeaconRegion.getRegion().equals(region))
                    {
                        beaconMonitorNotifier.didEnterCluster(bleBeaconCluster, bleBeaconRegion);
                        //TODO: evaluate whether a region could be owned by more than one cluster
                        return;
                    }
                }
            }
        }

        @Override
        public void didExitRegion(Region region) {
            Log.d(TAG, "I lost a beacon in the region with namespace id " + region.getId1() +
                    " and instance id: " + region.getId2());
            for (BLEBeaconCluster bleBeaconCluster:bleBeaconClustersMonitoring)
            {
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    if (bleBeaconRegion.getRegion().equals(region))
                    {
                        beaconMonitorNotifier.didExitCluster(bleBeaconCluster, bleBeaconRegion);
                        //TODO: evaluate whether a region could be owned by more than one cluster
                        return;
                    }
                }
            }
        }

        @Override
        public void didDetermineStateForRegion(int i, Region region) {
            int state=i;
            boolean isRegionFound;
            for (BLEBeaconCluster bleBeaconCluster:bleBeaconClustersMonitoring)
            {
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    if (bleBeaconRegion.getRegion().equals(region))
                    {
                        bleBeaconRegion.setState(i);
                        if (i==BLEBeaconMonitorNotifier.INSIDE)
                            beaconMonitorNotifier.didDetermineStateForCluster(i, bleBeaconCluster);
                    }
                    if(state!=BLEBeaconMonitorNotifier.INSIDE)
                        state=bleBeaconRegion.getState();
                }
            }
        }
    };


    private static RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size()>0){
                List<BLEBeacon> bleBeacons=new ArrayList<>();
                BLEBeacon bleBeacon;
                String regionUid = region.getUniqueId();
                for (BLEBeaconCluster bleBeaconCluster:bleBeaconClustersRanging){
                    if (regionUid.startsWith(bleBeaconCluster.getUniqueId()))
                    {
                        for (BLEBeaconRegion bleBeaconRegion:
                                bleBeaconCluster.getBleBeaconRegions())
                        {
                            if (bleBeaconRegion.getRegion().getUniqueId().equals(regionUid))
                            {
                                if ((bleBeaconRegion.isSingleBeaconRegion())&&(beacons.size()==1))
                                {
                                    bleBeacon=bleBeaconRegion.getBleBeacon();
                                    for (Beacon beacon:beacons){
                                        bleBeacon.setConfigurableParameters(beacon);
                                        break;
                                    }
                                    bleBeacons.add(bleBeacon);
                                }
                                else
                                {
                                    for (Beacon beacon:beacons){
                                        bleBeacons.add(new BLEBeacon.Builder()
                                                .setSeedBeacon(beacon)
                                                .build());
                                    }
                                }
                                beaconRangeNotifier.didRangeBeaconsInRegion
                                        (Collections.unmodifiableList(bleBeacons),
                                                bleBeaconRegion, bleBeaconCluster);
                                return;
                            }
                        }
                    }
                    //}
                }
            }
        }
    };
    //----------------------------------------------------------------------------------------------
    //endregion

    //region configuration section
    //----------------------------------------------------------------------------------------------
    /**
     * Initiates the configuration of the cluster.
     * <p>WARNING: starting configuration will stop the other beacon activities, because
     * the configuration function takes control of the service for beacons acquiring.</p>
     *
     * @param bleBeaconBeaconNotifier passed by the user for handling received beacons for
     *                                configuration purposes. <p>NOTE: only the beacons parsable with
     *                                the parsers stored are configurable<p/>
     *
     */
    public static void startBeaconClusterConfiguring
    (BLEBeaconBeaconNotifier bleBeaconBeaconNotifier)
    {
        allBleBeaconParsers=BLEBeaconParser.getAllParsers();
        configBLEBeacons=new ArrayList<>();
        BLEBeaconManager.bleBeaconBeaconNotifier=bleBeaconBeaconNotifier;
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);

        for (BLEBeaconParser beaconParser: allBleBeaconParsers){
            beaconManager.getBeaconParsers().add(beaconParser.getBeaconParser());
            if(beaconParser.hasExtra_layout())
                beaconManager.getBeaconParsers().add(beaconParser.getExtra_layout_BeaconParser());
        }
        if (!beaconManager.isAnyConsumerBound())
        {
            beaconManager.bind(configuringBeaconConsumer);
        }
        else if(isBeaconServiceConnected(beaconManager))
        {
            beaconManager.unbind(beaconConsumer);
        }
        else
        {
            //TODO: other requested beaconService... do nothing
        }
    }

    /**
     * Stops the cluster configuration and save into the xml file and saves the cluster on
     * xml file. Ending the configuration sets free
     * the services used to acquire the beacons
     *
     * @param bleBeaconClusters are stored in the xml file to be used for ranging/monitoring actions.
     *                          The Cluster passed would be created using
     *                          {@link BLEBeaconManager#startBeaconClusterConfiguring} function.
     *
     */
    public static void stopAndSaveBeaconClusterConfiguring(List<BLEBeaconCluster> bleBeaconClusters)
            throws IOException, SAXException {
        if (bleBeaconClusters!=null)
            XmlHandler.saveBleBeaconClusters(BLEContext.context, bleBeaconClusters);
        configBLEBeacons=new ArrayList<>();
        BLEBeaconManager.bleBeaconBeaconNotifier=null;
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);
        beaconManager.getBeaconParsers().clear();
        beaconManager.setRangeNotifier(null);
        if (beaconManager.isAnyConsumerBound())
        {
            beaconManager.unbind(configuringBeaconConsumer);
        }
        else
        {
            //TODO: other requested beaconService... do nothing
        }
    }


    /**
     * Delete the {@link BLEBeaconCluster} stored in the xml file
     */
    public void deleteBeaconClusters() throws IOException, SAXException {
        XmlHandler.flushBleBeaconClusters(BLEContext.context);
    }

    /**
     * Stops the cluster configuration. Ending the configuration sets free
     * the services used to acquire the beacons
     */
    public static void stopBeaconClusterConfiguring() throws IOException, SAXException {

        configBLEBeacons=new ArrayList<>();
        BLEBeaconManager.bleBeaconBeaconNotifier=null;
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);

        beaconManager.getBeaconParsers().clear();
        beaconManager.setRangeNotifier(null);
        if (beaconManager.isAnyConsumerBound())
        {
            beaconManager.unbind(configuringBeaconConsumer);
        }
        else
        {
            //TODO: other requested beaconService... do nothing
        }
    }

    /**
     * Save clusters onto the xml file
     *
     * @param bleBeaconClusters are stored in the xml file to be used for ranging/monitoring actions.
     *                          The Cluster passed would be created using
     *                          {@link BLEBeaconManager#startBeaconClusterConfiguring} function.
     *
     */
    public static void saveBeaconClusterConfiguring(List<BLEBeaconCluster> bleBeaconClusters)
            throws IOException, SAXException {
        if (bleBeaconClusters!=null)
            XmlHandler.saveBleBeaconClusters(BLEContext.context, bleBeaconClusters);
    }


    private static BeaconConsumer configuringBeaconConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            //TODO: use this bool to eventually block beacon service request
            isBeaconServiceConnectedForConfig=true;
            beaconManager.setRangeNotifier(configuringRangeNotifier);
            try {
                beaconManager.startRangingBeaconsInRegion
                        (new Region("allbeac", null, null, null));
            } catch (RemoteException e) {
                Log.e(TAG,e.getMessage());
            }

        }

        @Override
        public Context getApplicationContext() {
            return BLEContext.context;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            isBeaconServiceConnectedForConfig=false;
            BLEContext.context.unbindService(serviceConnection);
            configBLEBeacons=null;
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            return BLEContext.context.bindService(intent, serviceConnection, i);
        }
    };

    /**
     * {@link RangeNotifier}  filtered callback for configuration
     *
     */
    private static RangeNotifier configuringRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size()>0){
                Iterator<Beacon> beaconIterator = beacons.iterator();
                rec_beacon_iterator:

                //TODO: getMatchingBeaconTypeCode BeaconParser library. Per inserire il parser giusto

                while (beaconIterator.hasNext()) {
                    Beacon beacon = beaconIterator.next();
                    logBeaconData(beacon);
                    BLEBeacon bleBeacon=new BLEBeacon.Builder().setSeedBeacon(beacon).build();
                    for (int i=0;i<configBLEBeacons.size();i++)
                    {
                        if (configBLEBeacons.get(i).equals(bleBeacon))
                        {
                            configBLEBeacons.set(i, bleBeacon);
                            continue rec_beacon_iterator;
                        }
                    }
                    configBLEBeacons.add(bleBeacon);
                }
                bleBeaconBeaconNotifier.didRangeBeacons
                        (Collections.unmodifiableList(configBLEBeacons));
            }}
    };

    /**
     * Create Clusters for saving purposes.
     * @param uid name of the new Cluster
     * @param bleBeaconRegions list of {@link BLEBeaconRegion} included in the Cluster
     * @param bleBeacons list of {@link BLEBeacon} included in the Cluster
     */
    public BLEBeaconCluster createBLEClusterForSaving
            (String uid, List<BLEBeaconRegion> bleBeaconRegions, List<BLEBeacon> bleBeacons)
    {
        List<BLEBeaconRegion> bleBeaconRegions1=new ArrayList<>();
        for (BLEBeacon bleBeacon:bleBeacons)
        {
            BLEBeaconRegion bleBeaconRegion=new BLEBeaconRegion.Builder()
                    .setTheSingleBLEBeacon(bleBeacon)
                    .buildNotImplementingRegion();
            bleBeaconRegion.implementRegion(uid);
            bleBeaconRegions1.add(bleBeaconRegion);
        }
        for (BLEBeaconRegion bleBeaconRegion:bleBeaconRegions)
        {
            if (bleBeaconRegion.getRegion()==null)
                bleBeaconRegion.implementRegion(uid);
            bleBeaconRegions1.add(bleBeaconRegion);
        }
        return new BLEBeaconCluster(uid, bleBeaconRegions1);
    }


    /**
     * Create Clusters for saving purposes.
     * @param uid name of the new Cluster
     * @param bleBeacons list of {@link BLEBeacon} included in the Cluster
     */
    public BLEBeaconCluster createBLEClusterForSaving(String uid, List<BLEBeacon> bleBeacons)
    {
        return createBLEClusterForSaving(uid ,null, bleBeacons);
    }



    //----------------------------------------------------------------------------------------------
    //endregion
    //----------------------------------------------------------------------------------------------
    //endregion

    //region beacon region handling
    //----------------------------------------------------------------------------------------------
    /**
     * Implementation of the {@link BeaconConsumer} to wrap the Altbeacon library, handling the
     * beacon listener service.
     *
     */
    private static BeaconConsumer beaconConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {

            isBeaconServiceConnected=true;
            if (isBeaconRanging)
            {
                beaconManager.setRangeNotifier(rangeNotifier);
                for(BLEBeaconStarter bleBeaconStarter:bleBeaconRangingStarters)
                {
                    bleBeaconStarter.execute();
                }
            }
            if (isBeaconMonitoring)
            {
                beaconManager.setMonitorNotifier(monitorNotifier);
                for(BLEBeaconStarter bleBeaconStarter:bleBeaconMonitoringStarters)
                {
                    bleBeaconStarter.execute();
                }
            }

        }

        @Override
        public Context getApplicationContext() {
            return BLEContext.context;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            isBeaconServiceConnected=false;
            isBeaconMonitoring=false;
            isBeaconRanging=false;
            BLEContext.context.unbindService(serviceConnection);
            if(isBeaconServiceConnectedForConfigRequired)
                beaconManager.bind(configuringBeaconConsumer);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            return BLEContext.context.bindService(intent, serviceConnection, i);
        }
    };

    /**
     * Check whether the service is still connected or not.
     * In the latter case set the {@link BLEBeaconManager#isBeaconServiceConnected} variable
     * to <code>false</code>
     *
     * @param beaconManager {@link BeaconManager}
     *
     * @return <code>beaconServiceConnected</code>
     *
     */
    private static boolean isBeaconServiceConnected(BeaconManager beaconManager)
    {
        if(isBeaconServiceConnected){
            if (beaconManager!=null){
                if (!beaconManager.isBound(beaconConsumer))
                    isBeaconServiceConnected=false;
            }}
        return isBeaconServiceConnected;
    }

    /**
     * Check whether the service is still connected or not.
     * In the latter case set the {@link BLEBeaconManager#isBeaconServiceConnected} variable
     * to <code>false</code> and try to connect the service
     *
     * @param beaconManager
     *
     */
    private static void connectBeaconService(BeaconManager beaconManager)
    {
        if (beaconManager!=null){
            if (!beaconManager.isBound(beaconConsumer)) {
                isBeaconServiceConnected=false;
                beaconManager.bind(beaconConsumer);
            }
        }
    }


    /**
     * If the parsers aren't already in use,
     * adds the parsers related to the passed Identifier to the {@link BLEBeaconManager#allBleBeaconParsers}
     * and passes them to the {@link BLEBeaconManager#beaconManager}
     *
     * @param parsersIdentifiers Identifier of the parser stored by the {@link BLEBeacon} in the
     *                          parameter {@link Beacon#mParserIdentifier}
     *
     */
    private static void addBLEParsersByName(List<String> parsersIdentifiers)
            throws BeaconParserException
    {
        if (allBleBeaconParsers==null)
            allBleBeaconParsers=BLEBeaconParser.getAllParsers();
        List<BLEBeaconParser> bleBeaconParsers2= new ArrayList<>();

        passed_parserIdentifier_cycle:
        for (String parsersIdentifier: parsersIdentifiers){
            for (BLEBeaconParser bleBeaconParser: allBleBeaconParsers){
                if (bleBeaconParser.getParser_identifier().equalsIgnoreCase(parsersIdentifier))
                {
                    bleBeaconParsers2.add(bleBeaconParser);
                    continue passed_parserIdentifier_cycle;
                }
            }
            throw new BeaconParserException
                    ("No beacon parser in xml file identified by '"+parsersIdentifier+"'");
        }
        total_cluster_parsers:
        for (BLEBeaconParser bleBeaconParser: bleBeaconParsers2) {
            if (bleBeaconParsers==null)
                bleBeaconParsers=new ArrayList<>();
            for (BLEBeaconParser beaconParser:bleBeaconParsers)
            {
                if (beaconParser.getParser_layout().equals(bleBeaconParser.getParser_layout()))
                    continue total_cluster_parsers;
            }
            bleBeaconParsers.add(bleBeaconParser);
            beaconManager.getBeaconParsers().add(bleBeaconParser.getBeaconParser());
            if (bleBeaconParser.hasExtra_layout())
                beaconManager.getBeaconParsers()
                        .add(bleBeaconParser.getExtra_layout_BeaconParser());
        }
    }

    /**
     * If the service is connected execute the {@link BLEBeaconGenericFunction}
     * <code>function</code> passed,
     * with the {@link BLEBeaconStarter} altough add the <code>function</code>
     *
     * @param starters
     * @param isServiceConnected
     *
     */
    private static void startMonitoringBeaconFunction(List<BLEBeaconStarter> starters,
                                                      boolean isServiceConnected)
    {
        if (isServiceConnected)
        {
            for (BLEBeaconStarter bleBeaconStarter:starters)
                bleBeaconStarter.execute();
        }
        else
        {
            bleBeaconMonitoringStarters=starters;
            //this call has to be placed after the parsers collecting
            connectBeaconService(beaconManager);
        }
    }

    /**
     * If the service is connected execute the {@link BLEBeaconGenericFunction}
     * <code>function</code> passed,
     * with the {@link BLEBeaconStarter} altough add the <code>function</code>
     *
     * @param starters
     * @param isServiceConnected
     *
     */
    private static void startRangingBeaconFunction(List<BLEBeaconStarter> starters,
                                                   boolean isServiceConnected)
    {
        if (isServiceConnected)
        {
            for (BLEBeaconStarter bleBeaconStarter:starters)
                bleBeaconStarter.execute();
        }
        else
        {
            bleBeaconRangingStarters=starters;
            //this call has to be necessarily placed after the parsers collecting
            connectBeaconService(beaconManager);
        }
    }

    private static void activateBeaconRegionRanging(BLEBeaconRegion bleBeaconRegion)
    {
        try {
            beaconManager.startRangingBeaconsInRegion(bleBeaconRegion.getRegion());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void activateBeaconRegionMonitoring(BLEBeaconRegion bleBeaconRegion)
    {
        try {
            beaconManager.startMonitoringBeaconsInRegion(bleBeaconRegion.getRegion());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiates the ranging of the clusters.
     * <p>NOTE: Starting ranging and monitoring function simultaneously is allowed.</p>
     *
     * @param beaconRangeNotifier passed by the user for handling received beacons for
     *                                Ragning purposes. <p>NOTE: only the beacons stored in the
     *                            blebeaconcluster.xml file are listened    <p/>
     *
     */
    public static void startBeaconRangeNotifier(BLEBeaconRangeNotifier beaconRangeNotifier)
    {
        BLEBeaconManager.beaconRangeNotifier=beaconRangeNotifier;
        getBeaconsClustersRanging();
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);
        //FIXME: in questo modo sto lasciando la possibilità di utilizzare solo i cluster presenti nell'xml
        if (!isBeaconRanging)
        {
            isBeaconRanging=true;
            if (isBeaconServiceConnected(beaconManager))
            {
                beaconManager.setRangeNotifier(rangeNotifier);
                for(BLEBeaconStarter bleBeaconStarter:bleBeaconRangingStarters)
                {
                    bleBeaconStarter.execute();
                }
            }
            else
                connectBeaconService(beaconManager);
        }
    }

    /**
     * Stop the ranging of the clusters.
     */
    //TODO: to be tested
    public static void stopBeaconRangeNotifier() throws IOException, SAXException {

        if (isBeaconRanging)
        {
            isBeaconRanging=false;
            BLEBeaconManager.beaconRangeNotifier=null;
            beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);

            beaconManager.getBeaconParsers().clear();
            beaconManager.setRangeNotifier(null);
            if (beaconManager.isAnyConsumerBound())
            {
                if (beaconManager.isBound(beaconConsumer))
                    beaconManager.unbind(beaconConsumer);
            }
            else
            {
                //TODO: other requested beaconService... do nothing
            }
        }
    }

    /**
     * Initiates the monitoring of the cluster.
     * <p>NOTE: Starting ranging and monitoring function simultaneously is allowed.</p>
     *
     * @param beaconMonitorNotifier passed by the user for handling received beacons for
     *                                Ragning purposes. <p>NOTE: only the beacons stored in the
     *                            blebeaconcluster.xml file are listened    <p/>
     *
     */
    public static void startBeaconMonitorNotifier(BLEBeaconMonitorNotifier beaconMonitorNotifier)
    {
        BLEBeaconManager.beaconMonitorNotifier=beaconMonitorNotifier;
        getBeaconsClustersMonitoring();
        beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);
        //FIXME: in questo modo sto lasciando la possibilità di utilizzare solo i cluster presenti nell'xml
        if (!isBeaconMonitoring)
        {
            isBeaconMonitoring=true;
            if (isBeaconServiceConnected(beaconManager))
            {
                beaconManager.setMonitorNotifier(monitorNotifier);
                for(BLEBeaconStarter bleBeaconStarter:bleBeaconMonitoringStarters)
                {
                    bleBeaconStarter.execute();
                }
            }
            else
                connectBeaconService(beaconManager);
        }
    }

    /**
     * Stop the monitoring of the clusters.
     */
    //TODO: to be tested
    public static void stopBeaconMonitorNotifier() throws IOException, SAXException {

        if (!isBeaconMonitoring)
        {
            isBeaconMonitoring=false;
            BLEBeaconManager.beaconRangeNotifier=null;
            beaconManager = BeaconManager.getInstanceForApplication(BLEContext.context);

            beaconManager.getBeaconParsers().clear();
            beaconManager.setRangeNotifier(null);
            if (beaconManager.isAnyConsumerBound())
            {
                if (beaconManager.isBound(beaconConsumer))
                    beaconManager.unbind(beaconConsumer);
            }
            else
            {
                //TODO: other requested beaconService... do nothing
            }
        }
    }


    /**
     * Log the fields of the beacon passed
     * @param beacon
     */
    private static void logBeaconData(Beacon beacon) {

        StringBuilder scanString = new StringBuilder();

        scanString.append("beaconXXX: ");
        if (beacon.getServiceUuid() == 0xfeaa) {

            if (beacon.getBeaconTypeCode() == 0x00) {

                scanString.append(" Eddystone-UID -> ");
                scanString.append(" Namespace : ").append(beacon.getId1());
                scanString.append(" Identifier : ").append(beacon.getId2());

                logEddystoneTelemetry(scanString, beacon);

            } else if (beacon.getBeaconTypeCode() == 0x10) {

                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                scanString.append(" Eddystone-URL -> " + url);
                logEddystoneTelemetry(scanString, beacon);

            } else if (beacon.getBeaconTypeCode() == 0x20) {

                scanString.append(" Eddystone-TLM -> ");
                logEddystoneTelemetry(scanString, beacon);

            }

            scanString.append(" RSSI: ").append(beacon.getRssi());
            scanString.append(" Proximity: ").append(Double.toString(beacon.getDistance()));
            scanString.append(" Power: ").append(beacon.getTxPower());

        } else {

            // Just an old fashioned iBeacon or AltBeacon...
            logGenericBeacon(scanString, beacon);

        }

        Log.d(TAG,scanString.toString());

    }

    private static void logEddystoneTelemetry(StringBuilder scanString, Beacon beacon) {
        // Do we have telemetry data?
        if (beacon.getExtraDataFields().size() > 0) {
            long telemetryVersion = beacon.getExtraDataFields().get(0);
            long batteryMilliVolts = beacon.getExtraDataFields().get(1);
            long temperature = beacon.getExtraDataFields().get(2);
            long pduCount = beacon.getExtraDataFields().get(3);
            long uptime = beacon.getExtraDataFields().get(4);

            scanString.append(" Telemetry version : " + telemetryVersion);
            scanString.append(" Uptime (sec) : " + uptime);
            scanString.append(" Battery level (mv) " + batteryMilliVolts);
            scanString.append(" Tx count: " + pduCount);
            scanString.append(" temperature: " + temperature/260);
        }
    }

    /**
     * Logs iBeacon & AltBeacon data.
     */
    private static void logGenericBeacon(StringBuilder scanString, Beacon beacon) {
        List<Identifier> mIdentifiers = beacon.getIdentifiers();
        if (mIdentifiers != null)
        {
            int length = mIdentifiers.size();
            if (length>0)
                scanString.append(" UUID: ").append(beacon.getId1());
            if (length>2)
            {
                scanString.append(" Maj. Mnr.: ");
                if (beacon.getId2() != null) {
                    scanString.append(beacon.getId2());
                }
                scanString.append("-");
                if (beacon.getId3() != null)
                    scanString.append(beacon.getId3());
            }

            scanString.append(" RSSI: ").append(beacon.getRssi());
            scanString.append(" Proximity: ").append(Double.toString(beacon.getDistance()));
            scanString.append(" Power: ").append(beacon.getTxPower());
            //scanString.append(" Timestamp: ").append(BeaconHelper.getCurrentTimeStamp());
        }
    }


    //----------------------------------------------------------------------------------------------
    //endregion


    /**
     * This method shall to be inserted in the onDestroy() callback of the Activity
     * to allow the  proper handling of the resources
     */
    public static void onDestroy()
    {
        //Unbound the beacon service
        if (beaconManager!=null){
            if (beaconManager.isBound(beaconConsumer)) {
                beaconManager.unbind(beaconConsumer);
            }
        }
    }

}
