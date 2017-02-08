package eu.angel.bleembedded.lib.beacon.ui;

/**
 * Created by giova on 30/01/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import eu.angel.bleembedded.lib.beacon.BeaconTags;

public class TagsListAdapterLinked extends BaseAdapter {

    private final int TITLE_VIEW_ID = View.generateViewId();
    private final int TAGBTN_VIEW_ID = View.generateViewId();

    private final LinkedHashMap<String,ArrayList<String>> mData;
    private Iterator<String> itBeacon;
    private int posLastGot;

    public TagsListAdapterLinked(LinkedHashMap map) {
        mData = map;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        if (position==0) {
            itBeacon = mData.keySet().iterator();
            if (itBeacon.hasNext()) {
                posLastGot = 0;
                return itBeacon.next();
            }
            return null;
        }
        if (position==posLastGot+1) {
            if (itBeacon.hasNext()) {
                posLastGot++;
                return itBeacon.next();
            }
            return null;
        }
        String[] arr = mData.keySet().toArray(new String[0]);
        if (position>=0&&position<arr.length) {
            posLastGot = position;
            return arr[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    private View.OnClickListener btnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Context context = v.getContext();
            final String key = v.getTag().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog alert = builder
                    .setTitle("Clear tag")
                    .setMessage("Do you really want to clear this tag for all beacons?")
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int whichButton)
                                {
                                    BeaconTags.getInstance().removeTag(key);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("Undo", null).create();

            alert.show();
        }
    };


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View result=null;

        String key = getItem(position);
        ArrayList<String> beaconsInTag = mData.get(key);

        if (key != null) {
            Button btnTag;

            if (convertView == null) {

                LinearLayout layout = new LinearLayout(parent.getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.TOP);
                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                LinearLayout textLayout= new LinearLayout(parent.getContext());
                textLayout.setOrientation(LinearLayout.VERTICAL);
                textLayout.setGravity(Gravity.TOP);
                textLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,2));

                btnTag = new Button(parent.getContext());
                btnTag.setId(TAGBTN_VIEW_ID);
                btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,1));
                btnTag.setPadding(10,10,10,10);
                btnTag.setTag(key);
                btnTag.setOnClickListener(btnTagClickListener);

                layout.addView(textLayout);
                layout.addView(btnTag);

                // TEXT SECTION : title above and features below
                TextView title = new TextView(parent.getContext());
                title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                title.setId(TITLE_VIEW_ID);
                title.setPadding(10,10,10,10);
                title.setSingleLine(true);
                title.setHorizontallyScrolling(true);

                textLayout.addView(title);

                result = layout;
            } else {
                result = convertView;
            }

            if (result!=null) {
                if (beaconsInTag.size()<=0) {  // don't show if no beacons in this tag
                    result.setLayoutParams(new AbsListView.LayoutParams(-1,1));
                    result.setVisibility(View.GONE);
                } else {
                    result.setVisibility(View.VISIBLE);
                    result.setLayoutParams(new AbsListView.LayoutParams(-1, -2));

                    TextView title = ((TextView) result.findViewById(TITLE_VIEW_ID));
                    title.setText(key);

                    btnTag = ((Button) result.findViewById(TAGBTN_VIEW_ID));
                    btnTag.setText(String.format("%d", beaconsInTag.size()));
                }
            }
        }

        return result;
    }


}