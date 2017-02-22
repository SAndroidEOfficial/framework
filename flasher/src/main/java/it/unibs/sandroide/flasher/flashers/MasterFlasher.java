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
package it.unibs.sandroide.flasher.flashers;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import it.unibs.sandroide.flasher.flashers.Arduino.Arduino_flasher;
import it.unibs.sandroide.flasher.flashers.ST.stFlasher;
//import it.unibs.sandroide.flasher.flashers.TI.cc_flasher;
import it.unibs.sandroide.flasher.usb.USB_DeviceID;
import it.unibs.sandroide.flasher.usb.UsbConnectionController;

public class MasterFlasher {

    private static final String TAG = "MasterFlasher";
    private static MasterFlasher instance;
    private static FlasherTaskEventListener flasherTaskEventListener;

    /**
     * Set the {@FlasherTaskEventListener} for the {@MasterFlasher} and the flasher created succesfully, if a flasher is already created this {@FlasherTaskEventListener} is not inherited
     * @param flasherTaskEventListener
     */
    public void setFlasherTaskEventListener(FlasherTaskEventListener flasherTaskEventListener)
    {
        this.flasherTaskEventListener=flasherTaskEventListener;
    }

    private MasterFlasher(){};

    static {
        instance = new MasterFlasher();
    }

    public static MasterFlasher getInstance() {
        return instance;
    }

    public Flasher getCorrectFlasher(Context context)
    {
        UsbConnectionController sUsbConnectionController = UsbConnectionController.getInstance(context, null);

        if (sUsbConnectionController.UsbDevices.size()!=0)
        {
            for (UsbDevice device: sUsbConnectionController.UsbDevices)
            {
                for (USB_DeviceID deviceID:ProgrammerType.DeviceTable)
                {
                    if (device.getVendorId() == deviceID.vendor_id &&
                            device.getProductId() == deviceID.product_id)
                    {
                        Log.d(TAG, deviceID.description);
                        switch(deviceID.description)
                        {
                            //FIXME: a new version of cc_flasher has to created (license issue)
//                            case "CC Debugger":
//                            case "SmartRF04 Evaluation Board":
//                            case "SmartRF04 Evaluation Board (Chinese)":
//                            case "SmartRF05 Evaluation Board":
//                                return new cc_flasher(context,deviceID,
//                                        flasherTaskEventListener, device);

                            case "STLINK_DEVICE":
                                return new stFlasher(context, deviceID,
                                        flasherTaskEventListener,device);

                            case "ARDUINO":
                                return new Arduino_flasher(context, deviceID,
                                        flasherTaskEventListener, device,
                                        sUsbConnectionController.getmUsbManager());

                        }
                    }
                }
            }
            return null;
        }
        else
            return null;

    }

    public void erase(Context context)
    {
        Flasher fl=getCorrectFlasher(context);
        if (fl!=null)
            fl.flasher_erase(context);
    }

    public void write(Context context, String filePath)
    {
        Flasher fl=getCorrectFlasher(context);
        if (fl!=null)
            fl.flasher_write(context, filePath);
    }

    public void stop(Context context)
    {
        UsbConnectionController.getInstance(context, null).stop();
    }

}
