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
package com.angelo.bleembeddedflasher.usb;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;


	public class USB_DeviceID
	{
		public enum protocolType { PROTOCOL_TI, PROTOCOL_CHIPCON };

		public short vendor_id;
		public short product_id;

		public UsbEndpoint epIN;
		public UsbEndpoint epOUT;
		public UsbDeviceConnection conn;

		public String description;
		
		public protocolType protocol;
		
		public USB_DeviceID(short vendor_id, short product_id,
				UsbEndpoint epIN, UsbEndpoint epOUT, UsbDeviceConnection conn, 
				String description, protocolType protocol) {
			this.vendor_id=vendor_id;
			this.product_id=product_id;
			this.description=description;
			this.epIN=epIN;
			this.epOUT=epOUT;
			this.conn=conn;
			this.protocol=protocol;
		}
		
		
		public USB_DeviceID copyUSB_DeviceID()
		{
			return new USB_DeviceID(vendor_id, product_id,
					epIN, epOUT, conn,description, 
					protocol);
		}
		
	};