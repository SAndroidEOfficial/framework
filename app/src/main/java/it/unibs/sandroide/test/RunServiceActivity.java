package it.unibs.sandroide.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.beacon.BeaconTags;

/**
 * Created by giova on 27/03/2017.
 */

public class RunServiceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }

    public void startService(View v)
    {
        startService(new Intent(this,LogService.class));
    }

    public void stopService(View v)
    {
        stopService(new Intent(this,LogService.class));
    }

    public void startTagConfiguration(View v) {
        BeaconTags.getInstance().startTaggingActivity(this);
    }

}
