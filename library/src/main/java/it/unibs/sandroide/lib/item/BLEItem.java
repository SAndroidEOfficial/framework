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
package it.unibs.sandroide.lib.item;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.data.BLEDeviceData;
import it.unibs.sandroide.lib.device.action.BLEAction;
import it.unibs.sandroide.lib.device.action.BLEInit;
import it.unibs.sandroide.lib.device.read.BLEDeviceDataCluster;
import it.unibs.sandroide.lib.device.read.BLEDeviceDataListener;
import it.unibs.sandroide.lib.device.DeviceControl;
import it.unibs.sandroide.lib.item.alarm.BLEAlarm;

/**
 * The abstract class which implements the basic methods for all Items.
 */
public abstract class BLEItem {

	private static final String TAG ="BLEItem";

	//TODO: remove the old type references
	public static final int TYPE_NONE = 0;
	//public static final int TYPE_SENSOR = 1;
	public static final int TYPE_BUTTON = 2;
	public static final int TYPE_BATTERY = 3;
	public static final int TYPE_ALARM = 4;
	public static final int TYPE_SENSOR_ACCELEROMETER = 5;
	public static final int TYPE_SENSOR_FREEFALL = 6;
	public static final int TYPE_SENSOR_TEMPERATURE = 7;
	public static final int TYPE_SENSOR_PRESSURE = 8;
	public static final int TYPE_SENSOR_HUMIDITY = 9;
	public static final int TYPE_GENERALIO = 10;
	public static final int TYPE_SENSOR_GENERIC = 11;
	public static final int TYPE_DEVICEIO = 12;  // to exchange configuration parameters or any other message/value formatted as JSON string



	/**
	 * {@link Bleresource} which defines the Item.
	 */
	protected Bleresource bleresource;

	public Bleresource getBleresource() {
		return bleresource;
	}

	//List<BLEDeviceDataCluster> bleDeviceDataClusters;
	HashMap<String, List<BLEDeviceDataCluster>> bleDeviceDataClustersForGroup;
	HashMap<String, BLEDeviceData[]> bleDeviceDataArrayForCluster=new HashMap<>();
	//BLEDeviceData[] bleDeviceDataArray;

	/**
	 * {@link DeviceControl} related to the Item.
	 */
	protected DeviceControl mDeviceControl;
	protected String addressRef;
	protected String deviceNameRef;
	protected String actualDeviceName;
	public BLEOnItemUpdateListener bleOnItemUpdateListener;
	private boolean isbleOnItemUpdateListenerSet;
	protected BLEAction[] bleActions;
	protected BLEInit bleInit;
	private BLEOnItemInitiatedListener bleOnItemInitiatedListener;
	private boolean isbleOnItemInitiatedListenerSet;
	protected boolean isItemInitiated;

	public String getActualDeviceName() {
		return actualDeviceName;
	}

	public void setActualDeviceName(String actualDeviceName) {
		this.actualDeviceName = actualDeviceName;
	}

	protected boolean deviceControlConnected=false;

	public boolean isDeviceControlConnected() {
		return deviceControlConnected;
	}

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Constructor of the {@link BLEItem} class.
	 * @param type integer which defines the type of the Item (e.g. {@link BLEAlarm}).
	 * @param deviceName the name of the device to connect with (the name identifies exactly the
	 *                   remote device anf the firmware version).
	 * @param bleresource {@link Bleresource} which defines the Item.
	 */
	public BLEItem(int type, String deviceName, Bleresource bleresource) {
		setType(type);
		this.addressRef=bleresource.getDevmacaddress();
		this.deviceNameRef =deviceName;
		this.bleresource=bleresource;
	}
	
	
	public void setAddressRef(String addressRef)
	{
		this.addressRef=addressRef;
	}
	
	public String getAddressRef()
	{
		return addressRef;
	}
	
	public void setnameRef(String addressRef)
	{
		this.deviceNameRef =addressRef;
	}
	
	public String getDeviceNameRef()
	{
		return deviceNameRef;
	}
	
	public String getDeviceAddress()
	{
		return mDeviceControl.getmDeviceAddress();
	}

	/**
	 * Sets the device {@link DeviceControl} related to the {@link DeviceControl}
	 * @param mDeviceControl
	 */
	public void setDeviceControl(DeviceControl mDeviceControl)
	{
		this.mDeviceControl=mDeviceControl;
		deviceControlConnected=true;
		initDeviceControl();
	}

	/**
	 * Sets the device {@link BLEOnItemUpdateListener} and {@link BLEOnItemInitiatedListener} interfaces
	 * referenced by the different BLEItems for handling the communication with the {@link DeviceControl}
	 * @param bleOnItemInitiatedListener
	 * @param bleOnItemUpdateListener
	 */
	protected void setBleItemListeners(BLEOnItemUpdateListener bleOnItemUpdateListener,
									   BLEOnItemInitiatedListener bleOnItemInitiatedListener){
		setBleOnItemUpdateListener(bleOnItemUpdateListener);
		setBleOnItemInitiatedListener(bleOnItemInitiatedListener);
	}


	private void setBleOnItemUpdateListener(BLEOnItemUpdateListener bleOnItemUpdateListener) {
		this.bleOnItemUpdateListener = bleOnItemUpdateListener;
		isbleOnItemUpdateListenerSet=true;
	}

	private void setBleOnItemInitiatedListener(BLEOnItemInitiatedListener bleOnItemInitiatedListener) {
		this.bleOnItemInitiatedListener = bleOnItemInitiatedListener;
		isbleOnItemInitiatedListenerSet =true;
	}

	/**
	 * Trigger the init function of the {@link DeviceControl} for the {@link BLEItem}
	 */
	protected void initItem(){
		//mDeviceControl.initItem(bleInit, this.bleOnItemInitiatedListener);
		mDeviceControl.initItem(bleInit, this.bleOnItemInitiatedListener, this);
	}

	/**
	 * Initialization of the connection between the {@link DeviceControl} and the {@link BLEItem}.
	 * This method references the attributes shared by the two class (the first Object created with
	 * the xml information is the {@link DeviceControl} which shares its attributes with the
	 * {@link BLEItem}) connected. The attribute shared are:
	 * <ul><li>{@link BLEDeviceDataCluster} list as defined in the xml file. Only the {@link BLEDeviceData}
	 * owned by the BLEDeviceDataClusters are really shared</li>
	 * <li>{@link BLEAction} list indicating the sequences useful to handle the {@link BLEItem}</li>
	 * <li>the {@link BLEInit} for the {@link BLEItem}</li></ul>
	 */
	protected void initDeviceControl(){
		//TODO: mDeviceControl could be more than one
		bleDeviceDataClustersForGroup=this.mDeviceControl.getBLEDeviceDataClusterForGroup(this);

		for (String mKey:bleDeviceDataClustersForGroup.keySet()){
			Log.d(TAG, "group: "+mKey);
			List<BLEDeviceDataCluster> bleDeviceDataClusters = bleDeviceDataClustersForGroup.get(mKey);
			List<BLEDeviceData> bleDeviceDataList=new ArrayList<>();
			for (int i=0;i<bleDeviceDataClusters.size();i++){
				Log.d(TAG, "__Dcluster: "+bleDeviceDataClusters.get(i));
				//this.bleDeviceDataClusters[i]=bleDeviceDataClusters.get(i);
				for (BLEDeviceData bleDeviceData:bleDeviceDataClusters.get(i).getBleDeviceDataList()){
					bleDeviceDataList.add(bleDeviceData);
				}
			}

			BLEDeviceData[] bleDeviceDataArray=new BLEDeviceData[bleDeviceDataList.size()];
			for (int i=0;i<bleDeviceDataList.size();i++){
				bleDeviceDataArray[i]=bleDeviceDataList.get(i);
			}
			for (BLEDeviceDataCluster bleDeviceDataCluster:bleDeviceDataClusters){
				bleDeviceDataArrayForCluster.put(bleDeviceDataCluster.getId(), bleDeviceDataArray);
				bleDeviceDataCluster.setBleDeviceDataListener(bleDeviceDataListener);
			}
		}


		List<BLEAction> bleActions=this.mDeviceControl.getBLEDeviceActionsForDevItem(this);
		this.bleActions=new BLEAction[bleActions.size()];
		for (int i=0;i<bleActions.size();i++)
			this.bleActions[i]=bleActions.get(i);

		bleInit=this.mDeviceControl.getBLEDeviceInitForDevItem(this);
	}

	/**
	 * {@link BLEDeviceDataListener} callback set by the {@link BLEItem} implementation
	 */
	BLEDeviceDataListener bleDeviceDataListener= new BLEDeviceDataListener() {
		@Override
		public void onBLEDeviceDataRead(BLEDeviceDataCluster bleDeviceDataCluster) {
			Log.d(TAG, "message rec, Cluster: "+bleDeviceDataCluster.getId());
			if (isbleOnItemUpdateListenerSet)
				bleOnItemUpdateListener.onItemUpdate
						(bleDeviceDataArrayForCluster.get(bleDeviceDataCluster.getId()));
		}
	};

	public String getDevItem()
	{
		return bleresource.getDevItem();
	}

	/**
	 * Executes the actions passed by the {@link BLEItem#initDeviceControl()}
	 * @param action_id defines the action to be run. The ids in the current library (1.0) are special
	 *                  words related to the functions of the {@link BLEItem} inheritance
	 *
	 * @param input if needed, the inputs to performs the actions
	 */
	protected boolean runAction(String action_id, List<Float> input){
		//TODO: throw no actions if bleactions is null
		if (bleActions!=null){
		for (BLEAction bleAction:bleActions){
			if (bleAction.getId().equalsIgnoreCase(action_id)){
				bleAction.runAction(mDeviceControl, input);
				return true;
			}
		}
		return false;}
		else
			return false;
	}

	/**
	 * Execute the actions passed by the {@link BLEItem#initDeviceControl()}
	 * @param action_id defines the action to be run. The ids in the current library (1.0) are special
	 *                  words related to the functions of the {@link BLEItem} inheritance
	 **/
	protected boolean runAction(String action_id){
		List<Float> floats=new ArrayList<>();
		return runAction(action_id, floats);
	}

//	protected void stopDeviceControl()
//	{
//		deviceControlConnected=false;
//		mDeviceControl.deviceControlDetachmentRequest();
//		mDeviceControl=null;
//	}

	public void remove()
	{
		deviceControlConnected=false;
		BLEContext.removeBLEItem(this);
		mDeviceControl=null;
	}

}
