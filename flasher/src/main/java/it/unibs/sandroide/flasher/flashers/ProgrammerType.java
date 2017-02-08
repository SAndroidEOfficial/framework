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

public class ProgrammerType {

    public final static USB_DeviceID DeviceTable[] = {
            new USB_DeviceID((short)0x0451, (short)0x16A2, null, null, null, "CC Debugger",
                    USB_DeviceID.protocolType.PROTOCOL_TI),
            new USB_DeviceID((short)0x11A0, (short)0xDB20, null, null, null, "SmartRF04 Evaluation Board",
                    USB_DeviceID.protocolType.PROTOCOL_TI),
            new USB_DeviceID((short)0x11A0, (short)0xEB20, null, null, null, "SmartRF04 Evaluation Board (Chinese)",
                    USB_DeviceID.protocolType.PROTOCOL_CHIPCON),
            new USB_DeviceID((short)0x0451, (short)0x16A0, null, null, null, "SmartRF05 Evaluation Board",
                    USB_DeviceID.protocolType.PROTOCOL_TI),
            new USB_DeviceID((short)0x0483, (short)0x3748, null, null, null, "STLINK_DEVICE",
                    USB_DeviceID.protocolType.PROTOCOL_TI),
            new USB_DeviceID((short)0x2341, (short)0x0043, null, null, null, "ARDUINO",
                    USB_DeviceID.protocolType.PROTOCOL_TI)
    };
}
