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
package com.angelo.bleembeddedflasher.flashers.Arduino.stk500.responses;


import com.hoho.android.usbserial.util.HexDump;

public class STK500Response
{
    private int commandId;
    private int[] parameters;
    private byte[] data;
    private boolean ok;

    public STK500Response(int commandId, int[] parameters, byte[] data, boolean ok)
    {
        this.commandId = commandId;
        this.parameters = parameters;
        this.data = data;
        this.ok = ok;
    }

    public int getCommandId()
    {
        return commandId;
    }

    public void setCommandId(int commandId)
    {
        this.commandId = commandId;
    }

    public int[] getParameters()
    {
        return parameters;
    }

    public void setParameters(int[] parameters)
    {
        this.parameters = parameters;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    @Override
    public String toString() {
        String strp="",str = String.format("{command:%d(0x%s): parameters:[",commandId, HexDump.toHexString(commandId));
        if (parameters!=null) {
            for (int a : parameters) {
                strp += a + " ";
            }
        }
        str += String.format("%s],data:%s,ok:%s}",strp.trim(),HexDump.dumpHexString(data),ok?"true":"false");
        return str;
    }
}
