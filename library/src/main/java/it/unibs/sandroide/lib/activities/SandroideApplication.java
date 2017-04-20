package it.unibs.sandroide.lib.activities;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.altbeacon.beacon.BeaconManager;

/**
 * Created by giova on 11/01/2017.
 */

public class SandroideApplication extends Application implements  Application.ActivityLifecycleCallbacks {

    public static Activity CurrentActivity = null;
    public static BeaconManager beaconManager = null;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        CurrentActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        CurrentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        CurrentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        this.unregisterActivityLifecycleCallbacks(this);
    }
}
