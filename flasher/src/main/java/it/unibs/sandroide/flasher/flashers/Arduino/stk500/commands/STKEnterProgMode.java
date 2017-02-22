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
package it.unibs.sandroide.flasher.flashers.Arduino.stk500.commands;


import it.unibs.sandroide.flasher.flashers.Arduino.stk500.STK500Constants;
import it.unibs.sandroide.flasher.flashers.Arduino.stk500.STKCallback;
import it.unibs.sandroide.flasher.flashers.Arduino.stk500.STKCommunicator;

import java.nio.ByteBuffer;

/**
 * Enter Programming mode for the selected device. The Programming mode and device
 *programming parameters must have been set by Cmnd_STK_SET_DEVICE prior to
 *calling this command, or the command will fail with a Resp_STK_NODEVICE response.
 */

public class STKEnterProgMode extends STK500Command
{
    public STKEnterProgMode()
    {
        super(STK500Constants.Cmnd_STK_ENTER_PROGMODE, 2);
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((byte) STK500Constants.Cmnd_STK_ENTER_PROGMODE);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }

    @Override
    public void send(STKCallback cbk) {
        if(STKCommunicator.allowNewCommand.get())
        {
            STKCommunicator.allowNewCommand.set(false);
            STKCommunicator.currentCommand = this;
            STKCommunicator.currentCallback = cbk;
            STKCommunicator.send(getCommandBuffer());
        }
    }

}
