/**
 * Original Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
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
package it.unibs.sandroide.flasher.ble.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibs.sandroide.flasher.R;
import it.unibs.sandroide.flasher.RootActivity;

import it.unibs.sandroide.lib.communication.BLEOnConnectionEventListener;
import it.unibs.sandroide.lib.communication.BluetoothLeDevice;
import it.unibs.sandroide.lib.device.DevicesDescriptorNew;
import it.unibs.sandroide.lib.device.GattAttributesComplements;
import it.unibs.sandroide.lib.item.BleResourcesHandler;



import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceControlActivity extends RootActivity implements View.OnClickListener {

	private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    protected boolean mConnected = false;
    protected boolean mServicesAcquired = false;
    protected Context context;
    protected BluetoothLeDevice mBluetoothLeDevice;

    private ExpandableListView mGattServicesList;
    private TextView mConnectionState;

    protected HashMap<String, HashMap<String, BluetoothGattCharacteristic>> mServices = 
    		new HashMap<>();

    private String mDeviceFriendlyName;
    private String mAttributeDeviceName;
    private String mDeviceAddress;

    List<DevicesDescriptorNew> devicesDescriptors;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        context=this;
        final Intent intent = getIntent();
        mDeviceFriendlyName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        if (getActionBar()!=null) getActionBar().setTitle(mDeviceFriendlyName);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.mBluetoothLeDevice=new BluetoothLeDevice(this, mDeviceAddress);
        this.mBluetoothLeDevice.setBLEOnConnectionEventListener(mBLEOnConnectionEventListener);
        this.mBluetoothLeDevice.initialize();
        this.mBluetoothLeDevice.connect(mDeviceAddress);

        (findViewById(R.id.button_store_attribute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFrameSetDevName df=new DialogFrameSetDevName();
                df.builderDialog();
            }
        });
    }

//    public DevicesDescriptorNew getDeviceDescriptorByName(String name){
//        DevicesDescriptorNew devicesDescriptorNew=null;
//        if (devicesDescriptors==null)
//            devicesDescriptors=DevicesDescriptorNew.parseDevices(DeviceControlActivity.this);
//        for (DevicesDescriptorNew devicesDescriptor:devicesDescriptors){
//            if (devicesDescriptor.getDeviceType().equalsIgnoreCase(name)){
//                devicesDescriptorNew=devicesDescriptor;
//                break;
//            }
//        }
//        return devicesDescriptorNew;
//    }
    
	
    private final BLEOnConnectionEventListener mBLEOnConnectionEventListener = new BLEOnConnectionEventListener() {
        @Override
        public void onConnectionEvent(int state, BluetoothGattCharacteristic characteristic) {

            DevicesDescriptorNew devicesDescriptorNew=null;
            if (BluetoothLeDevice.ACTION_GATT_CONNECTED==state) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                mBluetoothLeDevice.discoverServices();
            } else if (BluetoothLeDevice.ACTION_GATT_DISCONNECTED==(state)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                mBluetoothLeDevice.close();
            } else if (BluetoothLeDevice.ACTION_GATT_SERVICES_DISCOVERED==(state)) {
                //FIXME: this patch is for LAPIS device (doesn't have a characteristic name)
                String name=mBluetoothLeDevice.getBluetoothGatt().getDevice().getName();

                devicesDescriptorNew=DevicesDescriptorNew.getDeviceDescriptorByName(name, devicesDescriptors);

                if(devicesDescriptorNew!=null){
                    if (GattAttributesComplements.isCharacteristicNameAvailable(devicesDescriptorNew.getDeviceType()))
                        askForDeviceName(mBluetoothLeDevice.getBluetoothGatt().getServices());
                    else{
                        if (name != null && name.length() > 0)
                        {
                            mAttributeDeviceName=name;
                            mBluetoothLeDevice.setmBluetoothDeviceName(new String(name));
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getActionBar()!=null) getActionBar().setTitle(mDeviceFriendlyName+": "+mAttributeDeviceName);}
                            });
                            listGattServicesAndAttributes(devicesDescriptorNew,
                                    mBluetoothLeDevice.getBluetoothGatt().getServices());
                            mBluetoothLeDevice.disconnect();
                        }
                    }
                } else
                    askForDeviceName(mBluetoothLeDevice.getBluetoothGatt().getServices());

                //listGattServicesAndAttributes(mBluetoothLeDevice.getmBluetoothDeviceName(), mBluetoothLeDevice.getBluetoothGatt().getServices());
                mServicesAcquired=true;
                //DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager(BluetoothLeDevice.ACTION_GATT_SERVICES_DISCOVERED, me);
                
            } else if (BluetoothLeDevice.ACTION_DATA_AVAILABLE==(state)) {
                final byte[] name = characteristic.getValue();
                if (name != null && name.length > 0) {
                    mAttributeDeviceName=new String(name);
                    mBluetoothLeDevice.setmBluetoothDeviceName(mAttributeDeviceName);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getActionBar()!=null) getActionBar().setTitle(mDeviceFriendlyName+": "+mAttributeDeviceName);}
                    });

                    devicesDescriptorNew=DevicesDescriptorNew.getDeviceDescriptorByName
                            (mBluetoothLeDevice.getmBluetoothDeviceName(), devicesDescriptors);
                    if (devicesDescriptorNew!=null)
                        listGattServicesAndAttributes(devicesDescriptorNew,
                                mBluetoothLeDevice.getBluetoothGatt().getServices());
                    mBluetoothLeDevice.disconnect();
                }
            }
    //--------------------------------------------------------------------------

        }
    };


    private BluetoothGattCharacteristic getDeviceNameCharFromCharacteristics
            (List<BluetoothGattService> gattServices)
    {
        for (BluetoothGattService gattService:gattServices)
        {
            if (gattService.getUuid().equals(DevicesDescriptorNew.DEVICE_NAME_SERVICE_UUID))
            {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                for(BluetoothGattCharacteristic bluetoothGattCharacteristic:gattCharacteristics)
                {
                    if(bluetoothGattCharacteristic.getUuid()
                            .equals(DevicesDescriptorNew.DEVICE_NAME_ATTRIBUTE_UUID))
                        return bluetoothGattCharacteristic;
                }
            }
        }
        return null;
    }

    private void askForDeviceName(List<BluetoothGattService> gattServices)
    {
        Log.d(TAG, "asking dev name");
        BluetoothGattCharacteristic bluetoothGattCharacteristic=
                getDeviceNameCharFromCharacteristics(gattServices);
        if (bluetoothGattCharacteristic!=null)
        {
            Log.d(TAG, "bluetoothGattCharacteristic: "+bluetoothGattCharacteristic);
            int charaProp = bluetoothGattCharacteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                Log.d(TAG, "reading dev name");
                mBluetoothLeDevice.readCharacteristic(bluetoothGattCharacteristic);
            }
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    
    
    
    protected void listGattServicesAndAttributes
            (DevicesDescriptorNew devicesDescriptorNew, List<BluetoothGattService> gattServices)
    {
        if (gattServices == null) return;
        String uuid;
        mServices = new HashMap<>();

        String charact;

        //deviceCharacteristics=new ArrayList<>();

        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<>();
        
        // Loops through available GATT Services.
        String LIST_NAME = "NAME";
        String LIST_UUID = "UUID";
        for (BluetoothGattService gattService : gattServices) {
        	Log.d(TAG, "service: "+gattServices);
            uuid = gattService.getUuid().toString();

            String service= GattAttributesComplements.getStringFromUUID(devicesDescriptorNew, uuid);
            if (service!=null)
            {
            ///plot in list
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
//            currentServiceData.put(
//                    LIST_NAME, GattAttributesComplements.
//                            getStringFromUUIDWithDefault(mBluetoothLeDevice.getmBluetoothDeviceName()
//                                    , uuid, unknownServiceString));
            currentServiceData.put(
                    LIST_NAME, GattAttributesComplements.
                            getStringFromUUIDWithDefault
                                    (devicesDescriptorNew, uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            /////
            

            	Log.d(TAG, "-service: "+service);
                List<BluetoothGattCharacteristic> gattCharacteristics =
            		  gattService.getCharacteristics();
                
              ///plot in list
                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<>();
                //ArrayList<BluetoothGattCharacteristic> charas =
                //        new ArrayList<BluetoothGattCharacteristic>();
//                ArrayList<String> charasString =
//                        new ArrayList<>();
                //////////////
                
            	HashMap<String, BluetoothGattCharacteristic> characteristics = 
            			new HashMap<>();
            
	            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
	            	Log.d(TAG, "-charact: "+gattCharacteristics);
	            	//charact=null;
	                uuid = gattCharacteristic.getUuid().toString();
	                charact=GattAttributesComplements.getStringFromUUID(devicesDescriptorNew, uuid);
//	                
//	              ///plot in list
//	                HashMap<String, String> currentCharaData = new HashMap<String, String>();
//	            	//charas.add(gattCharacteristic);
//	                String charString=SampleGattAttributes.getStringFromUUIDWithDefault(mDeviceName, uuid, unknownCharaString);
//	                charasString.add(charString);
//	                currentCharaData.put(
//	                        LIST_NAME, charString);
//	                currentCharaData.put(LIST_UUID, uuid);
//	                gattCharacteristicGroupData.add(currentCharaData);
//
//	                //////////////////////
	                
	                if (charact!=null)
	                {
		                
	  	              ///plot in list
	  	                HashMap<String, String> currentCharaData = new HashMap<>();

//	  	                String charString=GattAttributesComplements.
//                                getStringFromUUIDWithDefault(mBluetoothLeDevice.getmBluetoothDeviceName()
//                                        , uuid, unknownCharaString);
                        String charString=GattAttributesComplements.
                                getStringFromUUIDWithDefault(devicesDescriptorNew,
                                        uuid, unknownCharaString);

	  	                currentCharaData.put(
                                LIST_NAME, charString);
	  	                currentCharaData.put(LIST_UUID, uuid);
	  	                gattCharacteristicGroupData.add(currentCharaData);

	  	                //////////////////////
	                	characteristics.put(charact, gattCharacteristic);
                        Log.d(TAG, "-characteristic: " + charact + " UUID: " + gattCharacteristic);
                        //deviceCharacteristics.add(charact);
	                }
	            }
	            ///plot in list
                //mGattCharacteristics.add(charas);
                //mGattCharacteristicsString.add(charasString);

                gattCharacteristicData.add(gattCharacteristicGroupData);
                //////////////////////
	            mServices.put(service, characteristics);
            }
        }
        
        Log.d(TAG, "-serviceL: "+gattServiceData.size()+", attrL: "+gattCharacteristicData.size());
        
        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mGattServicesList.setAdapter(gattServiceAdapter);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }


    public class DialogFrameSetDevName {


        boolean resourcesStored=false;

        // response to Host dialog "ok" button click
        DialogInterface.OnClickListener dialogHOCLok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processInput();
                if (resourcesStored){
                Toast.makeText(context,
                        R.string.device_attributes_stored, Toast.LENGTH_SHORT).show();
                DeviceControlActivity.this.finish();}
            }
        };

        // response to Host and UserName dialog
        // "cancel" button click since it does the same thing
        DialogInterface.OnClickListener dialogOCLcancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        EditText et;

        public void builderDialog() {
            // Request the host from the user.
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    DeviceControlActivity.this);
            et = new EditText(DeviceControlActivity.this);
            et.setOnClickListener(DeviceControlActivity.this);
            et.requestFocus();
            builder.setMessage(R.string.Enter_a_specific_name_for_the_selected_Device)
                    .setTitle(R.string.Device_name)
                    .setPositiveButton(R.string.Ok, dialogHOCLok)
                    .setNegativeButton(R.string.Cancel, dialogOCLcancel)
                    .setView(et);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void processInput() {
            String input = et.getText().toString();
            if (input == null || input.trim().length() == 0) {
                Toast.makeText(context,
                        R.string.resource_name_cannot_be_left_blank, Toast.LENGTH_SHORT).show();
                resourcesStored=false;
            } else {
//                InsertBLEResource.writeAllAttributes
//                        (mDeviceAddress, mDeviceName, deviceCharacteristics, "", input);
                if (mAttributeDeviceName!=null&&mServices!=null)
                {
                    DevicesDescriptorNew devicesDescriptorNew=
                            DevicesDescriptorNew.getDeviceDescriptorByName
                                    (mAttributeDeviceName, devicesDescriptors);
                    if (devicesDescriptorNew!=null){
                    resourcesStored=true;
                    BleResourcesHandler.storeAllDevResources2(context, input, devicesDescriptorNew, "",
                        mDeviceAddress, mServices);}
                }
                else
                {
                    resourcesStored=false;
                    Toast.makeText(context,
                            R.string.services_not_found_yet, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}










