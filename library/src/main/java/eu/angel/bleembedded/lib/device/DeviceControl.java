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
package eu.angel.bleembedded.lib.device;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.communication.BLEOnConnectionEventListener;
import eu.angel.bleembedded.lib.communication.BLEOnConnectionEventToDevicesManagerListener;
import eu.angel.bleembedded.lib.communication.BluetoothLeDevice;
import eu.angel.bleembedded.lib.device.action.BLEAction;
import eu.angel.bleembedded.lib.device.action.BLEActionEnded;
import eu.angel.bleembedded.lib.device.action.BLEInit;
import eu.angel.bleembedded.lib.device.read.BLEDeviceDataCluster;
import eu.angel.bleembedded.lib.device.read.BLEReadableCharacteristic;
import eu.angel.bleembedded.lib.device.read.FakeNotification;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.BLEOnItemInitiatedListener;

/**
 * Handles the remote device
 */
public class DeviceControl {

	private final static String TAG = DeviceControl.class.getSimpleName();

	private final Lock lock = new ReentrantLock();

	private DevicesDescriptorNew devicesDescriptor;
	private boolean deviceDescriptorSet=false;
	
    public boolean mConnected = false;
    public boolean mServicesAcquired = false;
    
    protected DeviceControl me;
    protected Context context;
    protected BluetoothLeDevice mBluetoothLeDevice;
	boolean mOperative=false;
    protected HashMap<String, HashMap<String, BluetoothGattCharacteristic>> mServices =
    		new HashMap<>();

	private ScheduledExecutorService scheduledSequenceElements = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture scheduledFuture=null;

	
	protected BLEOnConnectionEventToDevicesManagerListener DevicesManagerBLEOnConnectionEventListener;
	protected List<BLEItem> items=new ArrayList<>();

	protected List<BLEReadableCharacteristic> bleReadableCharacteristics;
	protected List<BLEAction> bleActions;
	protected List<BLEInit> bleInits;
	protected List<FakeNotification> fakeNotifications;

	public boolean isDeviceDescriptorSet() {
		return deviceDescriptorSet;
	}

	public boolean getmOperative() {
		return mOperative;
	}
    private String mDeviceName;
    private String mDeviceAddress;
    public String getmDeviceName() {
		return mDeviceName;
	}

	public Lock getLock() {
		return lock;
	}

	/*
	public void setmDeviceName(String mDeviceName) {
		this.mDeviceName = mDeviceName;
	}*/


	public String getmDeviceAddress() {
		return mDeviceAddress;
	}


	public void setmDeviceAddress(String mDeviceAddress) {
		this.mDeviceAddress = mDeviceAddress;
	}


	/**
	 * Whether the devItem is supported by this {@link DeviceControl}
	 *
	 * @param devItem
	 * @return true if the devItem is embedded, false otherwise
	 */
	public boolean isDevItemEmbedded(String devItem)
	{

		Log.d(TAG, "Service acquisition status1:" + mServicesAcquired);
		Log.d(TAG, "->->Services: " + mServices);
		Log.d(TAG, "devItem is: "+devItem);
		HashMap<String, String[]> mSA=GattAttributesComplements.getServicesCharacteristicsMandatoryForDevItem
				(devicesDescriptor, devItem);
		if (mSA!=null)
		{
			for (String mKey:mSA.keySet())
			{
				String[]sAnda=mSA.get(mKey);
				if (sAnda!=null)
				{
					if (mServices.get(mKey)!=null)
					{
						for (int i=0;i<sAnda.length;i++)
						{
							if((mServices.get(mKey)).get(sAnda[i])==null)
							{
								Log.d(TAG, "item not embedded");
								return false;
							}
						}
						Log.d(TAG, "item embedded");
						return true;
					}
				}
			}
		}
		Log.d(TAG, "item not embedded");
		return false;
	}


    /*
    private void reStartServices()
    {
    	for (BLEItem item: items)
    		item.initDeviceControl();
    };*/
    /*
    private void releaseItems()
    {
    	for (BLEItem item: items)
    		DevicesManager.addBLEItemToBLEItems(item);
    	items=new ArrayList<BLEItem>();
    }*/


	/**
	 * {@link DeviceControl} constructor use to create a Device Control. ATTENTION: in order to Start the DeviceControl
	 * initDeviceControl() shall be called (create a {@link BluetoothLeDevice})
	 * @param context
	 * @param deviceName
	 * @param deviceAddress
	 * @param DevicesManagerBLEOnConnectionEventListener
	 */
	public DeviceControl(Context context, String deviceName,
						 String deviceAddress,
						 BLEOnConnectionEventToDevicesManagerListener DevicesManagerBLEOnConnectionEventListener) {
		this.mDeviceName = deviceName;
		this.mDeviceAddress = deviceAddress;
		this.context=context;
		this.DevicesManagerBLEOnConnectionEventListener=DevicesManagerBLEOnConnectionEventListener;
		//this.mBluetoothLeDevice.setBLEOnConnectionEventListener(mBLEOnConnectionEventListener);
		this.me=this;
//		this.devicesDescriptor=devicesDescriptor;
//		this.bleReadableCharacteristics=devicesDescriptor.cloneReadableCharacteristic();
//		this.bleActions=bleActions;
	}


	/**
	 * Set the {@link DevicesDescriptorNew} based on the type of remote device handled
	 *
	 * @return true if the remote device is supported by the library, false otherwise
	 */
	public boolean setDeviceDescriptor(){
		String name=mBluetoothLeDevice.getmBluetoothDeviceName();
		Log.d(TAG, "name: " + name);
		DevicesDescriptorNew devicesDescriptor=null;
		if (name==null){
			name=getmDeviceName();

		}

		//TODO: parser should be done only at the beginning
		devicesDescriptor=
				DevicesDescriptorNew.getDeviceDescriptorByName(name);
		if (devicesDescriptor==null)
			return false;
		this.devicesDescriptor=devicesDescriptor;
		listGattServicesAndAttributes(
				mBluetoothLeDevice.getBluetoothGatt().getServices());

		this.bleReadableCharacteristics=devicesDescriptor.cloneReadableCharacteristic();
		this.bleActions=devicesDescriptor.cloneBleActions();
		this.bleInits=devicesDescriptor.cloneBleInits();
		this.deviceDescriptorSet=true;
		return true;
	}

	/**
	 * Implementation of the interface used to handle the communication with the remote device.
	 * The callback is triggered by the {@link BluetoothLeDevice} associated with this {@link DeviceControl}
	 *
	 */
    private final BLEOnConnectionEventListener mBLEOnConnectionEventListener = new BLEOnConnectionEventListener() {
        @Override
        public void onConnectionEvent(int state, BluetoothGattCharacteristic characteristic) {
        	
        	switch (state)
        	{
        	
        	case BluetoothLeDevice.ACTION_DATA_AVAILABLE:
				if (characteristic.getUuid().equals(DevicesDescriptorNew.DEVICE_NAME_ATTRIBUTE_UUID))
				{
					final byte[] name = characteristic.getValue();
					if (name != null && name.length > 0) {
						mBluetoothLeDevice.setmBluetoothDeviceName(new String(name));
						DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
								(BluetoothLeDevice.ACTION_NAME_DEVICE_AVAILABLE_FROM_GATT_SERVICE, me);
					}
				}
        		handleData(characteristic);
        		break;
        	
        	case BluetoothLeDevice.ACTION_GATT_CONNECTED:
        		mConnected = true;
				DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
						(state, me);
        		break;
        		
        	case BluetoothLeDevice.ACTION_GATT_DISCONNECTED:
        		mConnected = false;
                DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
            	(state, me);
        		break;
        		
        		
        	//case BluetoothLeDevice.ACTION_GATT_DISCONNECTED_STATE_8:
        	case BluetoothLeDevice.ACTION_GATT_SERVICES_DISCOVERED:
				DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
						(state, me);
				String name;
				if (devicesDescriptor==null)
                    name = mBluetoothLeDevice.getBluetoothGatt().getDevice().getName();
				else
					name = devicesDescriptor.getDeviceType();
				//FIXME:GENERAL_LIB introduced only for Lapis, which doesn't return its name as characteristic
				//FIXME: without scan before the connection the nickname isn't known (LAPIS will be evere returned)
                    if (GattAttributesComplements.isCharacteristicNameAvailable(name))
						askForDeviceName(mBluetoothLeDevice.getBluetoothGatt().getServices());
                    else {
                        if (name == null){
                            name = "LAPIS BLE SLD";
                            mBluetoothLeDevice.setmBluetoothDeviceName(new String(name));
//                            listGattServicesAndAttributes(mBluetoothLeDevice.getmBluetoothDeviceName(),
//                                    mBluetoothLeDevice.getBluetoothGatt().getServices());

                            DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
                                    (BluetoothLeDevice.ACTION_NAME_DEVICE_AVAILABLE_FROM_GATT_SERVICE, me);
                        } else if (name != null && name.length() > 0) {
                            mBluetoothLeDevice.setmBluetoothDeviceName(new String(name));
//                            listGattServicesAndAttributes(mBluetoothLeDevice.getmBluetoothDeviceName(),
//                                    mBluetoothLeDevice.getBluetoothGatt().getServices());

                            DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
                                    (BluetoothLeDevice.ACTION_NAME_DEVICE_AVAILABLE_FROM_GATT_SERVICE, me);
                        }
                    }

				mServicesAcquired=true;

        		break;

        	}

    //--------------------------------------------------------------------------

        }
    };

	/**
	 * Lists the known services and characteristics (knowledge based on the {@link DevicesDescriptorNew}).
	 * Storing them into the {@link DeviceControl#mServices}.
	 * @param gattServices {@link BluetoothGattService} list retrieved by the bluetooth remote device
	 *
	 */
    protected void listGattServicesAndAttributes(List<BluetoothGattService> gattServices)
    {
        if (gattServices == null) return;
        String uuid = null;
        mServices = new HashMap<>();

        String charact;
        
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

        	
            uuid = gattService.getUuid().toString();
            String service=GattAttributesComplements.getStringFromUUID(devicesDescriptor, uuid);
            if (service!=null)
            {
            	Log.d(TAG, "-service: " + service);
                List<BluetoothGattCharacteristic> gattCharacteristics =
            		  gattService.getCharacteristics();
            
            	HashMap<String, BluetoothGattCharacteristic> characteristics =
            			new HashMap<>();
            
	            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
	            	charact=null;
	                uuid = gattCharacteristic.getUuid().toString();
	                charact=GattAttributesComplements.getStringFromUUID(devicesDescriptor, uuid);
	                if (charact!=null)
	                {
	                	characteristics.put(charact, gattCharacteristic);
	                	Log.d(TAG, "-characteristic: " + charact + " UUID: " + gattCharacteristic);
	                }
	            }
	            mServices.put(service, characteristics);
            }
        }
    }

	/**
	 * @param gattServices the list of all the {@link BluetoothGattService} of the remote device
	 * @return  the standard name characteristic ({@link DevicesDescriptorNew#DEVICE_NAME_SERVICE_UUID})
	 * used to identify the type of the remote device
	 *
	 */
	private BluetoothGattCharacteristic getDeviceNameCharFromCharacteristics(List<BluetoothGattService> gattServices)
	{
		for (BluetoothGattService gattService:gattServices)
		{
			if (gattService.getUuid().equals(DevicesDescriptorNew.DEVICE_NAME_SERVICE_UUID))
			{
				List<BluetoothGattCharacteristic> gattCharacteristics =
						gattService.getCharacteristics();
				for(BluetoothGattCharacteristic bluetoothGattCharacteristic:gattCharacteristics)
				{
					if(bluetoothGattCharacteristic.getUuid().equals(DevicesDescriptorNew.DEVICE_NAME_ATTRIBUTE_UUID))
						return bluetoothGattCharacteristic;
				}
			}
		}
		return null;
	}


	/**
	 *
	 * Requests the standard name characteristic value of the remote device
	 * @param gattServices the list of all the {@link BluetoothGattService} of the remote device
	 *
	 */
	private void askForDeviceName(List<BluetoothGattService> gattServices)
	{
		Log.d(TAG, "asking dev name");
		BluetoothGattCharacteristic bluetoothGattCharacteristic=getDeviceNameCharFromCharacteristics(gattServices);
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
    
    //region Element Executor
	/**
	 *
	 * Adds the {@link eu.angel.bleembedded.lib.device.action.BLESequenceElement} runnable with the
	 * delay which will be introduced from the last runnable introduced
	 * @param runnable the {@link Runnable} handled by the {@link DeviceControl#scheduledSequenceElements}
	 * @param relativeDelay the delay of the execution of the runnable from the last {@link Runnable}
	 *                      added
	 */
	public synchronized boolean addBLEElement(Runnable runnable, int relativeDelay){
		Log.d(TAG, "adding runnable: "+runnable+", relativeDelay: "+relativeDelay);
		if (scheduledFuture!=null){
			long time = scheduledFuture.getDelay(TimeUnit.MILLISECONDS);
			try {
				Log.d(TAG, "remainingDelaytime: "+time);
				if (time<0)
					time=0;
				scheduledFuture=scheduledSequenceElements
                        .schedule(runnable, relativeDelay+time, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else{
			try {
				scheduledFuture=scheduledSequenceElements
                        .schedule(runnable, relativeDelay, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	//endregion

	/**
	 *
	 * handles the incoming data from the bluetooth communication filtering on the base of the
	 * {@link BLEReadableCharacteristic}s of the {@link DeviceControl}
	 * @param characteristic the incoming {@link BLEReadableCharacteristic}
	 */
	protected void handleData(final BluetoothGattCharacteristic characteristic) {
		for (int i=0;i<bleReadableCharacteristics.size();i++){
			if (bleReadableCharacteristics.get(i).getUUID().equals(characteristic.getUuid()))
			{
				bleReadableCharacteristics.get(i).read(characteristic.getValue());
			}
		}
	}

	/**
	 * init the {@link DeviceControl} connecting it to the the remote device by the means of
	 * {@link BluetoothLeDevice}
	 */
	protected void initDeviceControl()
	{
		mBluetoothLeDevice = new BluetoothLeDevice(BLEContext.context, mDeviceAddress);
        mBluetoothLeDevice.setBLEOnConnectionEventListener(mBLEOnConnectionEventListener);

        mBluetoothLeDevice.initialize();
        mBluetoothLeDevice.connect();	

	}

	/**
	 * stop the {@link DeviceControl} disconnecting it from the the remote device by the means of
	 * {@link BluetoothLeDevice}
	 */
	protected void stopBluetoothLeDevice()
	{
		mBluetoothLeDevice.disconnect();
		mBluetoothLeDevice.close();
		mBluetoothLeDevice=null;
	}
	
	protected void addInItems(BLEItem bleItem)
	{
		items.add(bleItem);
	}
	
	protected List<BLEItem> getItems()
	{
		return items;
	}
	
	protected void releaseItems()
	{
		items=new ArrayList<>();
	}


	/**
	 * Reads the {@link BluetoothGattCharacteristic} value from the remote device.
	 * @param characteristic {@link BluetoothGattCharacteristic} to be read
	 */
	public void readChar(BluetoothGattCharacteristic characteristic){
		Log.d(TAG, "bt char: "+characteristic);
		mBluetoothLeDevice.readCharacteristic(characteristic);
	}

	/**
	 * Reads the {@link BluetoothGattCharacteristic} value from the remote device.
	 * @param service name of the service owner of the {@link BluetoothGattCharacteristic} to be read
	 * @param characteristic name of {@link BluetoothGattCharacteristic} to be read
	 */
	public void readChar(String service, String characteristic){
		Log.d(TAG, "serv: "+service+", char: "+characteristic);
		BluetoothGattCharacteristic gattCharacteristic = mServices.get(service).get(characteristic);
		readChar(gattCharacteristic);
	}

	/**
	 * Write the {@link BluetoothGattCharacteristic} value from the remote device.
	 * @param service name of the service owner of the {@link BluetoothGattCharacteristic} to be written
	 * @param characteristic name of {@link BluetoothGattCharacteristic} to be written
	 * @param command array of values to write on the {@link BluetoothGattCharacteristic}
	 */
	public void writeChar(String service, String characteristic, byte[] command)
	{
		BluetoothGatt mBluetoothGatt = mBluetoothLeDevice.getBluetoothGatt();

		BluetoothGattCharacteristic gattCharacteristic = mServices.get(service).get(characteristic);
		gattCharacteristic.setValue(command);
		boolean a=mBluetoothGatt.writeCharacteristic(gattCharacteristic);
		Log.d(TAG, "writing was " + a);
		Log.d(TAG, "byteArray was " + Arrays.toString(command));
		Delay(20);
	}

	/**
	 * Enables or disables notification on a given characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled If true, enable notification.  False otherwise.
	 */
	//FIXME: procedura di confronto UUID inefficiente sarebbe meglio tenere il service in String fino a qui
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
											  boolean enabled)
	{
		BluetoothGatt mBluetoothGatt = mBluetoothLeDevice.getBluetoothGatt();
		if (mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
				DevicesDescriptorNew.CLIENT_CHARACTERISTIC_CONFIG_UUID);
		boolean stored=descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

		boolean write=mBluetoothGatt.writeDescriptor(descriptor);
		Log.d(TAG, "char is " + characteristic);
		Log.d(TAG, "stored is " + stored + " write is " + write);
		Delay(20);

	}

	/**
	 * Enables  notification on a given characteristic.
	 * @param service name of the Service owner of the Characteristic
	 * @param characteristic name of the Characteristic to act on.
	 */
	public void startCharacteristicNotification(String service, String characteristic){
		BluetoothGattCharacteristic charact=
				mServices.get(service).get
						(characteristic);
		int charaProp = charact.getProperties();
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
		{
			setCharacteristicNotification(charact, true);
		}
	}

	/**
	 * Enables the {@link FakeNotification} on a given characteristic.
	 * @param service name of the Service owner of the Characteristic
	 * @param characteristic name of the Characteristic to act on.
	 */
	public void startFakeCharacteristicNotification(String service, String characteristic){
		BluetoothGattCharacteristic charact=
				mServices.get(service).get
						(characteristic);
		FakeNotification fakeNotification=new FakeNotification(charact);
		if (fakeNotifications==null)
			fakeNotifications=new ArrayList<>();
		fakeNotifications.add(fakeNotification);
		//TODO make delay and period variables
		fakeNotification.startFakeNotification(this, 10L, 500L);
	}

	/**
	 * Stops the Characteristic notification on a given characteristic.
	 * @param service name of the Service owner of the Characteristic
	 * @param characteristic name of the Characteristic to act on.
	 */
	public void stopCharacteristicNotification(String service, String characteristic){
		BluetoothGattCharacteristic charact=
				mServices.get(service).get
						(characteristic);
		int charaProp = charact.getProperties();
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
		{
			setCharacteristicNotification(charact, false);
		}
	}

	public void Delay(int delay){
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void requestServices()
	{
		mBluetoothLeDevice.discoverServices();
	}


//	public abstract boolean isResourceOfDevItemFree(String devItem);

	public void resetAfterDisconnection(){}

//	public void deviceControlDetachmentRequest()
//	{
//		DevicesManagerBLEOnConnectionEventListener.onConnectionEventToDevicesManager
//				(BluetoothLeDevice.ACTION_DEVICE_DETACHMENT_REQUEST, this);
//	}

	/**
	 * Retrieves the {@link BLEDeviceDataCluster}s gathered by group of the specified {@link BLEItem}.
	 * @param bleItem {@link BLEItem} associated to the required {@link BLEDeviceDataCluster}.
	 * @return the HashMap which links the groups with the related {@link BLEDeviceDataCluster}s
	 * (the relations are specified by the xml file).
	 */
	public HashMap<String, List<BLEDeviceDataCluster>> getBLEDeviceDataClusterForGroup(BLEItem bleItem){
		HashMap<String, List<BLEDeviceDataCluster>> bleDeviceDataClustersForGroup=new HashMap<>();
		List<BLEDeviceDataCluster> bleDeviceDataClusters;
		for (String mKey:GattAttributesComplements
				.getReadableCharacteristicsAndClustersPlusGroupForDevItem
						(devicesDescriptor, bleItem.getBleresource().getDevItem()).keySet()){
			for(BLEReadableCharacteristic bleReadableCharacteristic:bleReadableCharacteristics){
				if (bleReadableCharacteristic.getName().equals(mKey)){
					for (String[] clusterNameAndGroup:GattAttributesComplements
								 .getReadableCharacteristicsAndClustersPlusGroupForDevItem
										 (devicesDescriptor, bleItem.getBleresource().getDevItem()).get(mKey)){
						for (List<BLEDeviceDataCluster> bleDeviceDataClustersAux:
								bleReadableCharacteristic.getBLEDeviceDataClustersList()){
							for (BLEDeviceDataCluster bleDeviceDataCluster:bleDeviceDataClustersAux){
								//if the Parsing is semantic integer i could have null clusters in the List, nevertheless
								//I must keep the order of the list with the null objects inside
								if (bleDeviceDataCluster!=null){
									if (bleDeviceDataCluster.getId().equals(clusterNameAndGroup[0])){
										bleDeviceDataClusters=
											bleDeviceDataClustersForGroup.get(clusterNameAndGroup[1]);
										if (bleDeviceDataClusters==null){
											bleDeviceDataClusters=new ArrayList<>();
											bleDeviceDataClusters.add(bleDeviceDataCluster);
											bleDeviceDataClustersForGroup.put(clusterNameAndGroup[1],
													bleDeviceDataClusters);
										}
										else{
											bleDeviceDataClusters.add(bleDeviceDataCluster);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return bleDeviceDataClustersForGroup;
	}

	/**
	 * gets the {@link BLEAction}s of the specified {@link BLEItem}.
	 * @param bleItem {@link BLEItem} associated to the required {@link BLEAction}.
	 * @return the list of the {@link BLEAction} related with the bleItem.
	 */
	public List<BLEAction> getBLEDeviceActionsForDevItem(BLEItem bleItem){
		List<BLEAction> bleActions=new ArrayList<>();
		for (String action:GattAttributesComplements
				.getActionsForDevItem
						(devicesDescriptor, bleItem.getBleresource().getDevItem())){
			for (BLEAction bleAction:this.bleActions){
				if (bleAction.getId().equalsIgnoreCase(action))
					if (bleAction.getItemId().equals(bleItem.getDevItem()))
						bleActions.add(bleAction);
			}
		}
		return bleActions;
	}

	/**
	 * gets the {@link BLEInit} of the specified {@link BLEItem}.
	 * @param bleItem {@link BLEItem} associated to the required {@link BLEInit}.
	 * @return the {@link BLEInit} related with the bleItem.
	 */
	@Nullable
	public BLEInit getBLEDeviceInitForDevItem(BLEItem bleItem){
		String init=GattAttributesComplements
				.getInitForDevItem
						(devicesDescriptor, bleItem.getBleresource().getDevItem());
		for (BLEInit bleInit:this.bleInits){
			if (bleInit.getId().equalsIgnoreCase(init))
					return bleInit;
		}
		return null;
	}


	private List<BLEOnItemInitiatedListener> bleOnItemInitiatedListeners=new ArrayList<>();

	/**
	 * init the Item.
	 * @param bleInit the {@link BLEItem} executed in order to correctly initialize the remote device
	 *                resource for the {@link BLEItem}
	 * @param bleOnItemInitiatedListener the listener of the initialization of the remote device
	 * @param bleItem {@link BLEItem} related to the initialized resource
	 */
	public synchronized void initItem(final BLEInit bleInit,
									  final BLEOnItemInitiatedListener bleOnItemInitiatedListener,
									  BLEItem bleItem){
		Log.d(TAG, "initItem, bleInit: "+bleInit);
		if (bleInit!=null) {
			bleInit.addInitiatedItem(bleItem);
			Log.d(TAG, "bleInit is initiated: "+bleInit.isInitiated());
			if (!bleInit.isInitiated()) {
				if (bleOnItemInitiatedListener!=null)
					bleOnItemInitiatedListeners.add(bleOnItemInitiatedListener);
				bleInit.setBleActionEnded(new BLEActionEnded() {
					@Override
					public void onBleActionEnded() {
						bleInit.setInitializationEnded(true);
						for (BLEOnItemInitiatedListener onItemInitiatedListener
								:bleOnItemInitiatedListeners)
							onItemInitiatedListener.onItemInitiated();
						bleOnItemInitiatedListeners.clear();
					}
				});
				bleInit.runAction(this, null);
				bleInit.setInitiated(true);
			} else if (!bleInit.isInitializationEnded()){
				if(bleOnItemInitiatedListener!=null)
					bleOnItemInitiatedListeners.add(bleOnItemInitiatedListener);
			} else{
				if (bleOnItemInitiatedListener!=null)
					bleOnItemInitiatedListener.onItemInitiated();
			}
		}
	}

}










