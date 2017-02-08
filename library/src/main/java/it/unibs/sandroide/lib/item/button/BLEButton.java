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
package it.unibs.sandroide.lib.item.button;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.data.BLEDeviceData;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.BLEOnItemUpdateListener;
import it.unibs.sandroide.lib.item.Bleresource;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;


/**
 * The class which implements the BLEButton resources.
 */
public class BLEButton extends BLEItem{


	private BLEOnClickListener mBleOnClickListener;
	public final static int EVENT_START_BUTTON = 0;
	public final static int EVENT_STOP_BUTTON = 1;
	private int buttonNumber=0;

	private boolean onClickListenerCalled=false;

	/**
	 * Constructor of the {@link BLEButton} class.
	 * @param name the name of the device to connect with (the name identifies exactly the
	 *                   remote device anf the firmware version).
	 * @param whichOne number of the button (used for old library releases)
	 * @param bleresource {@link Bleresource} which defines the Item.
	 */
	public BLEButton(String name, int whichOne, Bleresource bleresource) {
		super(BLEItem.TYPE_BUTTON, name, bleresource);
		//TODO: remove maybe
		buttonNumber=whichOne;
	}

	/**
	 * Constructor of the {@link BLEButton} class.
	 * @param name of the Item.
	 * @param whichOne number of the button (used for old library releases)
	 * @param bleresource {@link Bleresource} which defines the Item.
	 */
	public BLEButton(String name, String whichOne, Bleresource bleresource)
	{
		super(BLEItem.TYPE_BUTTON, name, bleresource);
		try {
			buttonNumber= Integer.parseInt(whichOne);
		} catch (NumberFormatException e) {
			buttonNumber=1;
			e.printStackTrace();
		}
	}


	/**
	 * SEts the listener for the click of the virtual button and init the Item (performs the action
	 * useful to start the communication with the device e.g. notification for BLE).
	 * @param mBleOnClickListener of the Item.
	 */
	public void setOnClickListener(BLEOnClickListener mBleOnClickListener)
	{
		this.mBleOnClickListener=mBleOnClickListener;
		initOnClickListener();
	}

	/**
	 * Init the click listener, setting the {@link BLEOnItemUpdateListener} and initializing the Item
	 */
	void initOnClickListener()
	{
		onClickListenerCalled=true;
		// search mac address in flicmanager knownbuttons and then assign this callback to him
		if ( "FlicButton".equalsIgnoreCase(bleresource.getDevtype())) {
			if (BLEContext.flicManager!=null) {
				FlicButton button = BLEContext.flicManager.addToKnownButtons(bleresource.getDevmacaddress());
				//FlicButton button = BLEContext.flicManager.getButtonByDeviceId(mac);
				if (button != null) {
					button.removeAllFlicButtonCallbacks();
					button.addFlicButtonCallback(new FlicButtonCallback() {
						@Override
						public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
							if (isUp) mBleOnClickListener.onClick(BLEButton.this);
						}

						/*  Implementando click or double click non funziona!!!
						@Override
						public void onButtonSingleOrDoubleClick(String mac, boolean wasQueued, int timeDiff, int action) {
							mBleOnClickListener.onClick(BLEButton.this);
						}*/

					});
					button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.UP_OR_DOWN);
					//button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.CLICK_OR_DOUBLE_CLICK);
					button.setActiveMode(true);
				}
			}
		} else if (mDeviceControl!=null) {
			setBleItemListeners(new BLEOnItemUpdateListener() {
				@Override
				public void onItemUpdate(BLEDeviceData[] data) {
					if (data[0].getValue()==1)
						mBleOnClickListener.onClick(BLEButton.this);
				}
			}, null);
			initItem();

		}
	}

	/**
	 * deleteOnClickListener
	 */
	//TODO
	void deleteOnClickListener()
	{
//		mDeviceControl.setbleOnClickListenerButton(null, buttonNumber);
//		if (mDeviceControl!=null)
//		{
//			if (mDeviceControl.isResourceOfDevItemFree(getDevItem()))
//				mDeviceControl.addBLEEvent(new BLEEvent
//						(this.getType(), true, EVENT_STOP_BUTTON));
//		}
	}

	/**
	 * Overrides the initialization of the {@link it.unibs.sandroide.lib.device.DeviceControl}
	 */
	@Override
	public void initDeviceControl()
	{
		super.initDeviceControl();
		if (onClickListenerCalled) initOnClickListener();
	}

	public void onFlicManagerInitialized(){
		if (onClickListenerCalled) initOnClickListener();
	}
}
