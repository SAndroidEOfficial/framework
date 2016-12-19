package eu.angel.bleembedded.beaconconfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconManager;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;


public class ClusterActivity extends Activity implements View.OnClickListener {

    public static final String CONF_STATUS_KEY = "conf_status";
    private static final String TAG = "ClusterActivity";
    private static List<BLEBeaconCluster.Builder> bleBeaconClusterBuilders;
//    List<BLEBeaconCluster> bleBeaconClusters;
//    File tempFile;
    private String configurationStatusString;
    private TextView configurationStatus;
    private TextView beaconToJoin;
    private ListView mList;
    private Button button;
    private Button button_stop;
    private BLEBeacon bleBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cluster_list);

        configurationStatus = (TextView) findViewById(R.id.config_state);
        beaconToJoin = (TextView) findViewById(R.id.beacon);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            bleBeacon =
                    b.getParcelable("eu.angel.bleembedded.lib.beacon.BLEBeacon");
        }


        button = (Button) findViewById(R.id.create_new_cluster);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFrame df = new DialogFrame();
                df.builderDialog();
            }
        });
        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFrameAddBeacon dialogFrame = new DialogFrameAddBeacon();
                dialogFrame.builderDialog(position);
            }
        });

        button_stop = (Button) findViewById(R.id.stop_cluster_config);
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ClusterActivity.this,
                        "configuration off", Toast.LENGTH_SHORT).show();

                List<BLEBeaconCluster> bleBeaconClusters = new ArrayList<>();
                for (BLEBeaconCluster.Builder builder : bleBeaconClusterBuilders)
                    bleBeaconClusters.add(builder.build());
                try {
                    BLEBeaconManager.stopAndSaveBeaconClusterConfiguring(bleBeaconClusters);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

                configurationStatusString = "configuration off";
                SharedPreferences sharedPref = PreferenceManager
                        .getDefaultSharedPreferences(ClusterActivity.this);
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString(CONF_STATUS_KEY, configurationStatusString);
                edit.commit();

                (ClusterActivity.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        configurationStatus.setText(configurationStatusString);
                    }
                });


            }
        });

        Button button_del = (Button) findViewById(R.id.delete_clusters);
        button_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleBeaconClusterBuilders = new ArrayList<>();
                updateAdapter();
            }
        });

        bleBeaconClusterBuilders = new ArrayList<>();
    }

    public void updateAdapter() {
        List<String> stringList = new ArrayList<>();
        for (BLEBeaconCluster.Builder builder : bleBeaconClusterBuilders)
            stringList.add(builder.getUid());
        final ArrayAdapter arrayAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList);
        mList.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = Uri.parse
                ("content://eu.angel.bleembedded.test.fileprovider/clusters.temp");
        File file = null;
        ObjectInputStream myFile = null;
        FileInputStream fileInputStream = null;
        try {
            file = getFile(ClusterActivity.this,
                    "content://eu.angel.bleembedded.test.fileprovider/clusters.temp");
            fileInputStream = new FileInputStream(file);
            myFile = new ObjectInputStream(fileInputStream);
            bleBeaconClusterBuilders = new ArrayList<>();

            while (true) {
                bleBeaconClusterBuilders.add((BLEBeaconCluster.Builder) myFile.readObject());
            }
        } catch (EOFException e) {
            if (myFile != null) {
                try {
                    myFile.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


            e.printStackTrace();
        } catch (FileNotFoundException e) {
            if (myFile != null) {
                try {
                    myFile.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        file.delete();
        file = null;

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        configurationStatusString = sharedPref.getString(CONF_STATUS_KEY, "configuration off");

        if (configurationStatusString != null)
            configurationStatus.setText(configurationStatusString);

        if (bleBeacon != null)
            beaconToJoin.setText(bleBeacon.getUniqueId());

        updateAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        File file = null;
        ObjectOutputStream myFile = null;
        FileOutputStream fileOutputStream = null;
        try {

            file = getFile(ClusterActivity.this,
                    "content://eu.angel.bleembedded.test.fileprovider/clusters.temp");
            fileOutputStream = new FileOutputStream(file);
            myFile = new ObjectOutputStream(fileOutputStream);

            for (BLEBeaconCluster.Builder builder : bleBeaconClusterBuilders) {
                //bleBeaconClusterBuilders.
                myFile.writeObject(builder);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (myFile != null) {
            try {
                myFile.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (file != null) {

            file = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Uri uri = Uri.parse
                ("content://eu.angel.bleembedded.test.fileprovider/clusters.temp");
        try {
            OutputStream file = ClusterActivity
                    .this.getContentResolver()
                    .openOutputStream(uri);
            file.flush();
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile(Context context, String url) {
        File file;
        String fileName = Uri.parse(url).getLastPathSegment();
        file = new File(context.getCacheDir(), fileName);
        //file = File.createTempFile(fileName, null, context.getCacheDir());
        return file;

    }

    private class DialogFrame {


        boolean resourcesStored = false;
        DialogInterface.OnClickListener dialogOCLcancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        EditText et;
        DialogInterface.OnClickListener dialogHOCLok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processInput();
                if (resourcesStored) {
                    Toast.makeText(ClusterActivity.this,
                            R.string.cluster_added, Toast.LENGTH_SHORT).show();
                    //ClusterActivity.this.finish();
                }
            }
        };

        public void builderDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    ClusterActivity.this);
            et = new EditText(ClusterActivity.this);
            et.setOnClickListener(ClusterActivity.this);
            et.requestFocus();
            builder.setMessage(R.string.Enter_a_uid_for_the_beacon_cluster)
                    .setTitle(R.string.Beacon_Cluster)
                    .setPositiveButton(R.string.Ok, dialogHOCLok)
                    .setNegativeButton(R.string.Cancel, dialogOCLcancel)
                    .setView(et);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void processInput() {
            String input = et.getText().toString();
            if (input == null || input.trim().length() == 0) {
                Toast.makeText(ClusterActivity.this,
                        R.string.uid_name_cannot_be_blank, Toast.LENGTH_SHORT).show();
                resourcesStored = false;
            } else {
                resourcesStored = true;
                BLEBeaconCluster.Builder builder = new BLEBeaconCluster.Builder().setUid(input);
                bleBeaconClusterBuilders.add(builder);
                updateAdapter();
            }
        }
    }

    private class DialogFrameAddBeacon {
        int position = -1;
        boolean resourcesStored = false;

        DialogInterface.OnClickListener dialogHOCLok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processInput();
            }
        };


        DialogInterface.OnClickListener dialogOCLcancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        public void builderDialog(int position) {
            this.position = position;
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    ClusterActivity.this);

            builder.setMessage(R.string.would_you_like_adding_beacon_to_this_cluster)
                    .setTitle(R.string.join_beacon)
                    .setPositiveButton(R.string.Yes, dialogHOCLok)
                    .setNegativeButton(R.string.No, dialogOCLcancel);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void processInput() {
            if ((position >= 0) && (position <= bleBeaconClusterBuilders.size())) {
                if (bleBeacon != null) {
                    (ClusterActivity.this).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            beaconToJoin.setText("");
                        }
                    });

                    bleBeaconClusterBuilders.get(position)
                            .addBLERegion(new BLEBeaconRegion.Builder()
                                    .setTheSingleBLEBeacon(bleBeacon)
                                    .buildNotImplementingRegion());
                    bleBeacon = null;
                }
            }
        }
    }
}
