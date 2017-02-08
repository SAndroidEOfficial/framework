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
package it.unibs.sandroide.flasher.flashers.ST;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import it.unibs.sandroide.flasher.flashers.Flasher;
import it.unibs.sandroide.flasher.flashers.FlasherEvent;
import it.unibs.sandroide.flasher.flashers.FlasherTaskEventListener;
import it.unibs.sandroide.flasher.usb.UsbConnectionEventListener;
import it.unibs.sandroide.flasher.usb.USB_DeviceID;


public class stFlasher extends Flasher{

    	static {
		System.loadLibrary("stlink");
	}


    public static native String invokeNativeFunction();
    public static native int openUsbFirst(String devicePath);

    public static native int eraseSTLinkV2(int fb);
    public static native int writeSTLinkV2(int fb, String filePath);

    private enum task_req{NONE, FLASHER_ERASE, FLASHER_WRITE};

	private static task_req last_task_req=task_req.NONE;
	private static String write_filePath;
    private static final String TAG = "stFlasher";


    public stFlasher(Context context,USB_DeviceID mDeviceID,
                     FlasherTaskEventListener flasherTaskEventListener,
                     UsbDevice device)
    {
        super(context, mDeviceID, flasherTaskEventListener, device);
    }

    @Override
    public boolean refreshUsbConnection(Context context)
    {
        String devName=device.getDeviceName();
        if (openUsbFirst(devName)==0)
        {
            Log.d(TAG, "openUsbFirst==0");
            if (super.refreshUsbConnection(context))
            {
                return true;
            }
            else
                return false;
        }
        else
        {
            triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_UNABLE_TO_CONNECT_TO_USB_FROM_NATIVE_CODE));
            return false;
        }
    }

    @Override
    protected UsbConnectionEventListener getmConnectionHandler() {
        return  new UsbConnectionEventListener() {
            @Override
            public void onUsbStopped() {
                Log.e(TAG, "Usb stopped!");
            }

            @Override
            public void onDeviceNotFound() {
                if (sUsbConnectionController != null) {
                    sUsbConnectionController.stop();
                    sUsbConnectionController = null;
                }
            }

            @Override
            public void onPermissionGranted(UsbDevice dev) {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_USB_PERMISSION_GRANTED));
                if (!flasher_connectToDev()) {
                    triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_ERROR));
                    Log.d(TAG, "failed init USB connection after permission granted");
                    return;
                }
            }

        };
    }


    public void executeLastCommand()
    {
        Log.d(TAG, "executing last command... "+last_task_req);
        switch (last_task_req) {
        case FLASHER_ERASE:
            Log.d(TAG, "starting erasing flash");
            triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_ERASING));
            if (eraseSTLinkV2(sUsbConnectionController.getConn().getFileDescriptor())==0)
            {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_STOP_ERASING));
            }
            else
            {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_PROBABLY_UNABLE_TO_RECOGNIZE_TARGET_DEVICE));
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_ERROR_ERASING));
            }
            triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_STOP_ERASING_TASK));
            break;

        case FLASHER_WRITE:
            Log.d(TAG, "starting writing flash");
            if (write_filePath != null){
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_WRITING));
                if (writeSTLinkV2(sUsbConnectionController.getConn().getFileDescriptor(), write_filePath)==0)
                {
                    triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_STOP_WRITING));
                }
                else
                {
                    triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_PROBABLY_UNABLE_TO_RECOGNIZE_TARGET_DEVICE));
                    triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_ERROR_WRITNG));
                }
                Log.d(TAG, "filepath: "+write_filePath);
            }else {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_ERROR_LOADING_FILE));
                Log.d(TAG, "no file path");
            }
            write_filePath = null;
            triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_STOP_WRITING_TASK));
            break;

        default:
            break;
    }
    Log.d(TAG, "last command executed "+last_task_req);

    last_task_req = task_req.NONE;
    }

    @Override
    public void flasher_erase(Context context)
    {
        Log.d(TAG, "saving command "+task_req.FLASHER_ERASE);
        triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_ERASING_TASK));
    	last_task_req=task_req.FLASHER_ERASE;
    	refreshUsbConnection(context);
        //FIXME: se non si connette all'USB nel codice nativo non si accorge che il task non finisce
    	Log.d(TAG, "released flasher_erase"+task_req.FLASHER_ERASE);
    }

    @Override
    public void flasher_write(Context context, String filePath)
    {
        Log.d(TAG, "saving command "+task_req.FLASHER_WRITE);
        triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_WRITING_TASK));
        write_filePath=filePath;
    	set_last_req(task_req.FLASHER_WRITE);
    	refreshUsbConnection(context);
    }
    
    public static void set_last_req(task_req req)
    {
    	last_task_req=req;
    }

    public boolean init_device(UsbDeviceConnection conn)
    {
        Log.d(TAG, "init_device: STLINK_DEVICE");
        executeLastCommand();
        return true;
    }

}