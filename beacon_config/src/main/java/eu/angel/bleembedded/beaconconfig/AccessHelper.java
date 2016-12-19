package eu.angel.bleembedded.beaconconfig;

import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;

public class AccessHelper {

    BLEBeaconCluster bleBeaconCluster;
    BLEBeaconRegion bleBeaconRegion;
    boolean beaconIn;

    AccessHelper(BLEBeaconRegion bleBeaconRegion, BLEBeaconCluster bleBeaconCluster){
        this.bleBeaconRegion=bleBeaconRegion;
        this.bleBeaconCluster=bleBeaconCluster;
    }



}
