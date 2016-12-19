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
package com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands;


import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Download a block of data to the starterkit and program it in FLASH or EEPROM of the
 * current device. The data block size should not be larger than 256 bytes.
 */
public class STKProgramPage extends STK500Command
{
    public static final String EEPROM = "E";
    public static final String FLASH = "F";

    private int bytesHigh;
    private int bytesLow;
    private int memType;
    private byte[] data;

    public STKProgramPage(String memType, byte[] data)
    {
        super(STK500Constants.Cmnd_STK_PROG_PAGE, 2);

        if(memType.equals(EEPROM))
            this.memType = EEPROM.getBytes()[0];
        else
            this.memType = FLASH.getBytes()[0];

        this.bytesHigh = (data.length >> 8) & 0xff;
        this.bytesLow = data.length & 0xff;
        this.data = data;
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
        buffer.put((byte) STK500Constants.Cmnd_STK_PROG_PAGE);
        buffer.put((byte) bytesHigh);
        buffer.put((byte) bytesLow);
        buffer.put((byte) memType);
        buffer.put(data);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }

}
