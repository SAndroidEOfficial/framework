/**
 * Original Copyright none
 * Original code got at https://github.com/felHR85/AndSTK500
 *
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini and Angelo Vezzoli, All rights reserved.
 *
 * @author  Giovanni Lenzi
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
package com.angelo.bleembeddedflasher.flashers.Arduino.stk500;

import android.os.Handler;


import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STK500Command;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.phy.IPhy;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.responses.STK500Response;
import com.hoho.android.usbserial.driver.UsbSerialPort;
//import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * STK500v1 api
 */
public class STKCommunicator implements IPhy.OnChangesFromPhyLayer
{
    private final String TAG = "STKCommunicator";

    public final static int TIME_WRITE = 2000;

    public static UsbSerialPort phyComm;
    public static AtomicBoolean allowNewCommand;
    public static STK500Command currentCommand;
    public static STKCallback currentCallback;

    private static Runnable enableSend =  new Runnable() {
        @Override
        public void run() {
            STKCommunicator.allowNewCommand.set(true);
        }
    };

    private static Handler resetHandler = new Handler();

    // Usb constructor
    public STKCommunicator(UsbSerialPort sPort)
    {
        phyComm = sPort;
        allowNewCommand = new AtomicBoolean(true);
    }

    /**
     * Public api
     */
    public static int send(byte[] buff){
        resetHandler.removeCallbacks(enableSend);
        resetHandler.postDelayed(enableSend, 1500);

        int ret= 0;
        try {
            ret = phyComm.write(buff, TIME_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }


    private static final int MAX_BUFFER = 256 + 5; // Max command length
    private byte[] buffer;

    @Override
    public STK500Response onDataReceived(byte[] dataReceived) throws Exception
    {
        STK500Response rsp = null;
        if (currentCommand!=null && dataReceived.length>0) {

            if (buffer==null) {
                buffer = ByteBuffer.wrap(dataReceived).array();
            } else {
                ByteBuffer newb = ByteBuffer.allocate(buffer.length+dataReceived.length);
                newb = newb.put(buffer);
                newb = newb.put(dataReceived);
                buffer=newb.array();
            }

            rsp = currentCommand.generateResponse(buffer);
            if (rsp != null) {
                int rspDataLength=rsp.getData().length;

                if (buffer.length == rspDataLength) {
                    buffer = null;
                } else {
                    ByteBuffer readBuffer = ByteBuffer.wrap(buffer);
                    readBuffer.position(rspDataLength);
                    buffer = readBuffer.slice().array();
                }

                currentCommand=null;
                allowNewCommand.set(true);
                currentCallback.callbackCall(rsp);
            }
        }
        return rsp;
    }
}
