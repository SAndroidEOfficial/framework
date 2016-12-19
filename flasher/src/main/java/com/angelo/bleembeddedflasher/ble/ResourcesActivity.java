/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini and Angelo Vezzoli, All rights reserved.
 *
 * @author  Angelo Vezzoli
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
package com.angelo.bleembeddedflasher.ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.angelo.bleembeddedflasher.R;
import com.angelo.bleembeddedflasher.RootActivity;

import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.device.DevicesDescriptorNew;
import eu.angel.bleembedded.lib.device.GattAttributesComplements;
import eu.angel.bleembedded.lib.item.BleResourcesHandler;
import eu.angel.bleembedded.lib.item.Bleresource;

public class ResourcesActivity extends RootActivity implements View.OnClickListener {


    private static final String RESOURCE_NAME= "RESOURCE_NAME";
    private static final String  RESOURCE_TYPE = "RESOURCE_TYPE";
    private static final String RESOURCES_DESCR_INCIPIT="RESOURCES_DESCR_INCIPIT";
    private static final String RESOURCES_DESCR_BODY="RESOURCES_DESCR_BODY";
    private ExpandableListView mResourcesList;
    Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resources_layout);

        context = this;

        mResourcesList = (ExpandableListView) findViewById(R.id.resources_list);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        this.refreshListView();

        (findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog alert = builder
                        .setTitle(
                                R.string.alert_delete_resource_title
                                        )
                        .setMessage(R.string.confirmation)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog,
                                                        int whichButton)
                                    {
                                        try {
                                            XmlHandler.flushResources(context);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (SAXException e) {
                                            e.printStackTrace();
                                        }
                                        ((Activity)context).finish();
                                    }
                                }).setNegativeButton(android.R.string.no, null).create();
                alert.show();
            }
        });

        mResourcesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                int childPosition = ExpandableListView.getPackedPositionChild(id);
                int depth = ExpandableListView.getPackedPositionChild(id);
                String text1 = ((TwoLineListItem) view).getText1().getText().toString();
                final String from = ((TwoLineListItem) view).getText2().getText().toString();

                //childPosition == -1
                if ("DEVICE NAME:".equalsIgnoreCase(text1)) {
                    // chiedo come rinominare il device
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ResourcesActivity.this);
                    final EditText et = new EditText(ResourcesActivity.this);
                    et.requestFocus();
                    builder.setMessage(R.string.Enter_a_specific_name_for_the_selected_Device)
                            .setTitle(R.string.Device_name)
                            .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dlg, int which) {
                                    String input = et.getText().toString();
                                    try {
                                        if (input == null || input.trim().length() == 0) throw new ToastException(context.getString(R.string.resource_name_cannot_be_left_blank));
                                        dlg.dismiss();

                                        try {
                                            XmlHandler.renameDevice(context, from, input);
                                        } catch (FileNotFoundException e) {
                                            //TODO: evaluate whether throwing exception
                                            e.printStackTrace();
                                        } catch (SAXException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        refreshListView();

                                        throw new ToastException(context.getString(R.string.device_attributes_stored));

                                    } catch (ToastException ex) {
                                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.Cancel,  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dlg, int which) {
                                    dlg.dismiss();
                                }
                            })
                            .setView(et)
                            .create().show();

                    // You now have everything that you would as if this was an OnChildClickListener()
                    // Add your logic here.

                    // Return true as we are handling the event.
                    return true;
                }

                return false;
            }
        });

    }


    public void refreshListView(){
        ArrayList<HashMap<String, String>> resourcesNameType =
                new ArrayList<HashMap<String, String>>();
        List<Bleresource>bleresources= BleResourcesHandler.getAllDevResources(this);

        if (bleresources.size()==0)
        {
            Toast.makeText(this, R.string.no_resources_found, Toast.LENGTH_LONG).show();
            finish();
        }else
        {
            ArrayList<ArrayList<HashMap<String, String>>> resourcesDescriptions
                    = new ArrayList<ArrayList<HashMap<String, String>>>();
            for (Bleresource bleresource:bleresources)
            {
                HashMap<String, String> resourceNameType = new HashMap<String, String>();
                resourceNameType.put(
                        RESOURCE_NAME, bleresource.getName());
                resourceNameType.put(RESOURCE_TYPE, bleresource.getType());

                String[] descriptionBody={bleresource.getDevname(),
                        bleresource.getDevtype(), bleresource.getDevversion(),
                        bleresource.getDevmacaddress(), bleresource.getType(),
                        bleresource.getDevItem(),
                        Integer.toString(bleresource.getCardinality())};

                String[] descriptionIncipit={"DEVICE NAME:", "DEVICE TYPE:", "DEVICE VERSION:",
                        "DEVICE MAC ADDR:", "RESOURCE TYPE:", "DEVICE ITEM NAME",
                        "RESOURCE CARDINALITY:"};
                ArrayList<HashMap<String, String>> resourceDescriptions =
                        new ArrayList<HashMap<String, String>>();
                HashMap<String, String> resourceDescription;
                for (int i=0;i<6;i++)
                {
                    resourceDescription = new HashMap<String, String>();
                    resourceDescription.put(RESOURCES_DESCR_INCIPIT,descriptionIncipit[i]);
                    resourceDescription.put(RESOURCES_DESCR_BODY, descriptionBody[i]);
                    resourceDescriptions.add(resourceDescription);
                }
                resourcesNameType.add(resourceNameType);
                resourcesDescriptions.add(resourceDescriptions);
            }

            SimpleExpandableListAdapter simpleAdapter = new SimpleExpandableListAdapter(
                    this,
                    resourcesNameType,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[] {RESOURCE_NAME, RESOURCE_TYPE},
                    new int[] { android.R.id.text1, android.R.id.text2 },
                    resourcesDescriptions,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[] {RESOURCES_DESCR_INCIPIT, RESOURCES_DESCR_BODY},
                    new int[] { android.R.id.text1, android.R.id.text2 }
            );
            mResourcesList.setAdapter(simpleAdapter);
        }

    }

    @Override
    public void onClick(View v) {

    }


}
