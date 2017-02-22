/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.unibs.sandroide.lib.beacon.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import it.unibs.sandroide.lib.beacon.msg.BeaconMsgBase;
import it.unibs.sandroide.lib.beacon.BeaconTags;

public class BeaconsListAdapterLinked extends BaseAdapter {

    private final int IMG_VIEW_ID = View.generateViewId();
    private final int TITLE_VIEW_ID = View.generateViewId();
    private final int DISTANCE_VIEW_ID = View.generateViewId();
    private final int LASTSEEN_VIEW_ID = View.generateViewId();
    private final int PARSER_VIEW_ID = View.generateViewId();
    private final int TAGBTN_VIEW_ID = View.generateViewId();

    private final LinkedHashMap<String,BeaconMsgBase> mData;
    private Iterator<String> itBeacon;
    private int posLastGot;

    public BeaconsListAdapterLinked(LinkedHashMap<String,BeaconMsgBase> map) { mData = map; }

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

            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_dropdown_item_1line, BeaconTags.getInstance().getTagNames());

            final MultiAutoCompleteTextView acv = new MultiAutoCompleteTextView(context);
            acv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
            acv.setPadding(30,30,30,30);
            acv.setAdapter(tagsAdapter);
            acv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            ArrayList<String> currTags = BeaconTags.getInstance().getTagsForBeacon(key);
            acv.setText(currTags!=null?currTags.toString().replace("[","").replace("]",""):"");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog alert = builder
                    .setTitle("Tag beacon:")
                    .setMessage("Enter down below a name to tag the selected beacon. You can assign multiple tags to the same beacon or use a same tag for different beacons. Empty this field to clear existing tags.")
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int whichButton)
                                {
                                    BeaconTags.getInstance().changeTagsForBeacon(key,acv.getText().toString());
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("Undo", null).create();

            alert.setView(acv);


            alert.show();
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result;

        String key = getItem(position);
        BeaconMsgBase item = mData.get(key);
        Button btnTag;

        if (convertView == null) {

            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.TOP);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // ROW : beacon type image, text section, tag button
            ImageView img = new ImageView(parent.getContext());
            img.setId(IMG_VIEW_ID);
            byte[] decodedString = Base64.decode(item.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            img.setImageBitmap(decodedByte);
            img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,0));
            img.setPadding(10,10,10,10);

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


            layout.addView(img);
            layout.addView(textLayout);
            layout.addView(btnTag);

            // TEXT SECTION : title above and features below
            TextView title = new TextView(parent.getContext());
            title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            title.setId(TITLE_VIEW_ID);
            title.setPadding(10,10,10,10);
            title.setSingleLine(true);
            title.setHorizontallyScrolling(true);

            LinearLayout featuresLayout = new LinearLayout(parent.getContext());
            featuresLayout.setOrientation(LinearLayout.HORIZONTAL);
            featuresLayout.setGravity(Gravity.TOP);
            featuresLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            textLayout.addView(title);
            textLayout.addView(featuresLayout);

            // TEXT FEATURES : parserid, meters and seconds ago
            TextView parser = new TextView(parent.getContext());
            parser.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1));
            parser.setGravity(Gravity.LEFT);
            parser.setId(PARSER_VIEW_ID);

            TextView distance = new TextView(parent.getContext());
            distance.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,2));
            distance.setGravity(Gravity.RIGHT);
            distance.setId(DISTANCE_VIEW_ID);

            TextView lastSeen = new TextView(parent.getContext());
            lastSeen.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,2));
            lastSeen.setGravity(Gravity.RIGHT);
            lastSeen.setId(LASTSEEN_VIEW_ID);

            featuresLayout.addView(parser);
            featuresLayout.addView(distance);
            featuresLayout.addView(lastSeen);


            result = layout;
        } else {
            result = convertView;
        }

        if (result!=null) {

            if (new Date().getTime()/1000-item.getLastSeen() > 20) {  // don't show if too old
                result.setLayoutParams(new AbsListView.LayoutParams(-1,1));
                result.setVisibility(View.GONE);
            } else {
                result.setVisibility(View.VISIBLE);
                result.setLayoutParams(new AbsListView.LayoutParams(-1,-2));

                TextView title = ((TextView) result.findViewById(TITLE_VIEW_ID));
                title.setText(item.getIdentifiers().toString());
                ((TextView) result.findViewById(DISTANCE_VIEW_ID)).setText(String.format("%.1f m", item.getDistance()));

                ((TextView) result.findViewById(LASTSEEN_VIEW_ID)).setText(String.format("%d s ago", new Date().getTime() / 1000 - item.getLastSeen()));
                ((TextView) result.findViewById(PARSER_VIEW_ID)).setText(item.getClass().getSimpleName());
                //((TextView) result.findViewById(DISTANCE_VIEW_ID)).setText(item.getParserIdentifier());

                title.setEnabled((new Date().getTime() / 1000) - item.getLastSeen() < 10);

                btnTag = ((Button) result.findViewById(TAGBTN_VIEW_ID));
                btnTag.setText(String.format("%d", BeaconTags.getInstance().getTagsNumberForBeacon(key)));
            }
        }


        return result;
    }


}