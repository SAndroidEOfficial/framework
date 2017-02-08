package it.unibs.sandroide.test;

import android.os.Bundle;
import android.widget.TextView;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.button.BLEButton;
import it.unibs.sandroide.lib.item.button.BLEOnClickListener;

public class MainActivityFlic extends SandroideBaseActivity {
    private static final String TAG = "MainActivityFlic";

    BLEButton mButton1;
    BLEButton mButton2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flic);

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