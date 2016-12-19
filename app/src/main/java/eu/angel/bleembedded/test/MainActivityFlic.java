package eu.angel.bleembedded.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import eu.angel.bleembedded.R;
import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.button.BLEButton;
import eu.angel.bleembedded.lib.item.button.BLEOnClickListener;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class MainActivityFlic extends Activity {
    private static final String TAG = "MainActivityFlic";

    BLEButton mButton1;
    BLEButton mButton2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flic);

        BLEContext.initBLE(this);

        mButton2 = (BLEButton) BLEContext.findViewById("cyan_flicbutton");
        if (mButton2 != null) {
            mButton2.setOnClickListener(new BLEOnClickListener() {
                @Override
                public void onClick(BLEItem bleItem) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = (TextView) findViewById(R.id.textView);
                            tv.setText("clickedddddd cyan");
                        }
                    });
                }
            });
        }

        mButton1 = (BLEButton) BLEContext.findViewById("white_flicbutton");
        if (mButton1 != null) {
            mButton1.setOnClickListener(new BLEOnClickListener() {
                @Override
                public void onClick(BLEItem bleItem) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = (TextView) findViewById(R.id.textView);
                            tv.setText("clickedddddd white");
                        }
                    });
                }
            });
        }



    }
}