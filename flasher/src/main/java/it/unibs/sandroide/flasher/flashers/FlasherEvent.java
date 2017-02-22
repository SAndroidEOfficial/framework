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

import it.unibs.sandroide.flasher.usb.USB_DeviceID;

public class FlasherEvent {

    public static final int EVENT_START_ERASING=0;
    public static final int EVENT_STOP_ERASING=1;
    public static final int EVENT_START_WRITING=2;
    public static final int EVENT_STOP_WRITING=3;
    public static final int EVENT_ERROR_ERASING=4;
    public static final int EVENT_ERROR_WRITNG=5;
    public static final int EVENT_ERROR_LOADING_FILE=6;
    public static final int EVENT_ERROR_NO_PERMISSION=7;
    public static final int EVENT_USB_PERMISSION_GRANTED=8;
    public static final int EVENT_ERROR=9;
    public static final int EVENT_START_LOADING_FILE=10;
    public static final int EVENT_STOP_LOADING_FILE=11;
    public static final int EVENT_START_ERASING_TASK=12;
    public static final int EVENT_STOP_ERASING_TASK=13;
    public static final int EVENT_START_WRITING_TASK=14;
    public static final int EVENT_STOP_WRITING_TASK=15;
    public static final int EVENT_ERROR_TARGET_LOCKET=16;
    public static final int EVENT_UNABLE_TO_CONNECT_TO_USB_FROM_NATIVE_CODE=17;
    public static final int EVENT_PROBABLY_UNABLE_TO_RECOGNIZE_TARGET_DEVICE=18;

    public final int event;
    public final USB_DeviceID usb_deviceID;

    public FlasherEvent(USB_DeviceID usb_deviceID, int event)
    {
        this.event=event;
        this.usb_deviceID=usb_deviceID;
    }
}
