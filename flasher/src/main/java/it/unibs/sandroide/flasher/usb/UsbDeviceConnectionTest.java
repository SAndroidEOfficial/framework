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
package it.unibs.sandroide.flasher.usb;


import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

import it.unibs.sandroide.flasher.complements.HexSupport;

public class UsbDeviceConnectionTest {

	private static final String TAG = "UsbDeviceConnectionTest";

	public static int bulkTransferT(UsbEndpoint Ue, byte[] vect, int i, int u, UsbDeviceConnection conn)
	{
		
		Log.d(TAG, "bulk: "+Ue+"; vect: "+ HexSupport.bytesToHexWithSpace(vect));
		return conn.bulkTransfer(Ue, vect , i, u);
	}
	
	public static int controlTransferT(int reqType, 
			int req, int value, int index, byte[] buffer, int length , int timeOut, UsbDeviceConnection conn)
	{
		int ret= conn.controlTransfer(reqType, 
				req, value, index, buffer,length , timeOut);
		Log.d(TAG, "control: reType = "+reqType+"; req = "+req+"; value = "+value+"; index = "+index+"; buffer = "+HexSupport.bytesToHexWithSpace(buffer)+"; length = "+length);
		return ret;
	}
}
