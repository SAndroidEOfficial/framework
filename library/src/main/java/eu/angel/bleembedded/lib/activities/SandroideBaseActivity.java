package eu.angel.bleembedded.lib.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import eu.angel.bleembedded.lib.BLEContext;

/**
 * Created by giova on 09/01/2017.
 */

public class SandroideBaseActivity extends Activity {

    public static final int PERMISSIONS_FOR_SANDROIDE = 1;

    public static final String ALERT_PERMISSIONS_GRANTED = "Thanks for granting permissions. Sandroide will work correctly now!";
    public static final String ALERT_PERMISSIONS_NOTGRANTED = "Sandroide won't work correctly withouth granting permissions. Please, go to your phone settings and grant permissions to this application.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BLEContext.initBLE(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            switch (requestCode) {
                case PERMISSIONS_FOR_SANDROIDE: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        BLEContext.permissionsGranted = true;

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        throw new ToastException(ALERT_PERMISSIONS_GRANTED);

                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        throw new ToastException(ALERT_PERMISSIONS_NOTGRANTED);
                    }
                }
            }
        } catch (ToastException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public class ToastException extends Exception {

        public ToastException(String message){
            super(message);
        }

    }

    public class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }



}
