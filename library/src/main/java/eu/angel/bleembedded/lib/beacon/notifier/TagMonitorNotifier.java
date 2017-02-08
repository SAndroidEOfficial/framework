package eu.angel.bleembedded.lib.beacon.notifier;

public interface TagMonitorNotifier {
    int INSIDE = 1;
    int OUTSIDE = 0;

    void didEnterTag(String tag);

    void didExitTag(String tag);

    void didDetermineStateForTag(int i, String tag);
}
