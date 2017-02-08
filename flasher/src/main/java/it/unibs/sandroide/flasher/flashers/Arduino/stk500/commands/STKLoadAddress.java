package it.unibs.sandroide.flasher.flashers.Arduino.stk500.commands;


/**
 * Original Copyright none
 * Original code got at https://github.com/felHR85/AndSTK500
 *
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
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
import it.unibs.sandroide.flasher.flashers.Arduino.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Load 16-bit address down to starterkit. This command is used to set the address for the
 * next read or write operation to FLASH or EEPROM. Must always be used prior to
 * Cmnd_STK_PROG_PAGE or Cmnd_STK_READ_PAGE
 */
public class STKLoadAddress extends STK500Command
{
    private int addr;

    public STKLoadAddress(int addr)
    {
        super(STK500Constants.Cmnd_STK_LOAD_ADDRESS, 2);
        this.addr = addr;
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) STK500Constants.Cmnd_STK_LOAD_ADDRESS);
        buffer.put((byte) (addr & 0xff));
        buffer.put((byte) ((addr >> 8) & 0xff));
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }
}
