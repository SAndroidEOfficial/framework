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
package com.angelo.bleembeddedflasher.flashers;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import com.angelo.bleembeddedflasher.usb.UsbConnectionEventListener;
import com.angelo.bleembeddedflasher.usb.USB_DeviceID;
import com.angelo.bleembeddedflasher.usb.UsbConnectionController;

public abstract class Flasher {

    private static final String TAG = "Flasher";
    private static FlasherTaskEventListener flasherTaskEventListener;
    protected static UsbConnectionController sUsbConnectionController;
    protected static Context context;
    protected USB_DeviceID mDeviceID;
    protected UsbDevice device;


    public Flasher(Context context,USB_DeviceID mDeviceID,
                   FlasherTaskEventListener flasherTaskEventListener,
                   UsbDevice device)
    {
        this.context=context;
        this.mDeviceID=mDeviceID;
        this.flasherTaskEventListener=flasherTaskEventListener;
        this.device=device;
        this.sUsbConnectionController = UsbConnectionController.getInstance(context, getmConnectionHandler());
    }

    public void setflasherTaskEventListener(FlasherTaskEventListener flasherTaskEventListener)
    {
        flasherTaskEventListener=flasherTaskEventListener;
    }

    public abstract void flasher_erase(Context context);

    public abstract void flasher_write(Context context, String file_path);


    public boolean refreshUsbConnection(Context context)
    {
        if (!flasher_connectToDev())
        {
            Log.d(TAG, "failed init USB connection");
            return false;
        }
        else
            return true;
    }

    protected abstract UsbConnectionEventListener getmConnectionHandler();

    public static void triggerFlashserTaskEventListener(FlasherEvent event)
    {
        if (flasherTaskEventListener!=null)
            flasherTaskEventListener.on_flasherTaskEvent(event);
    }

    public void setFlashserTaskEventListener(FlasherTaskEventListener flashserTaskEventListener)
    {
            this.flasherTaskEventListener=flashserTaskEventListener;
    }

    protected boolean flasher_connectToDev()
    {
        UsbDeviceConnection conn= sUsbConnectionController.connectToDev(device,mDeviceID);
        if (conn!=null)
            return init_device(conn);
        else
            return false;
    }

    protected abstract boolean init_device(UsbDeviceConnection conn);

}
