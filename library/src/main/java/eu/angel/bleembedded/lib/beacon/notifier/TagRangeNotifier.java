package eu.angel.bleembedded.lib.beacon.notifier;

import org.altbeacon.beacon.Beacon;

import eu.angel.bleembedded.lib.beacon.msg.BeaconMsgBase;

/**
 * Created by giova on 02/02/2017.
 */

public interface TagRangeNotifier {
    void onTaggedBeaconReceived(BeaconMsgBase b);
}

