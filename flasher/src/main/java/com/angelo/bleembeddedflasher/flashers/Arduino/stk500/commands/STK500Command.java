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
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STKCallback;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STKCommunicator;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.responses.STK500Response;

import java.nio.ByteBuffer;

public abstract class STK500Command
{
    protected int commandId;
    protected int length;

    public STK500Command(int commandId, int length)
    {
        this.commandId = commandId;
        this.length = length;
    }

    public abstract byte[] getCommandBuffer();

    public int getLength()
    {
        return length;
    }

    public int getCommandId()
    {
        return commandId;
    }

    public STK500Response generateResponse(byte[] buffer) throws Exception {
        if (buffer.length>0) {
            switch(buffer[0]){
                case STK500Constants.Resp_STK_NOSYNC:
                    throw new Exception("NO_SYNC received as first byte in response to "+this.getClass().getSimpleName());

                case STK500Constants.Resp_STK_INSYNC:
                    if (buffer.length>=this.getLength()) {
                        if (buffer[1]==STK500Constants.Resp_STK_OK) {
                            byte[] dst = new byte[this.getLength()];
                            ByteBuffer.wrap(buffer).get(dst,0,this.getLength());
                            return new STK500Response(commandId,null,dst,true);
                        }
                        throw new Exception("Second byte SHOULD BE STK_OK(0x10)");
                    }
                    return null; // incomplete response, waiting for next reads

                default:
                    throw new Exception("Unknown received as first byte in response to "+this.getClass().getSimpleName());

            }
        }
        throw new Exception("Buffer length SHOULDN'T be zero here");
    }

    public void send(STKCallback cbk) {
        if(STKCommunicator.allowNewCommand.get())
        {
            STKCommunicator.allowNewCommand.set(false);
            STKCommunicator.currentCommand = this;
            STKCommunicator.currentCallback = cbk;
            STKCommunicator.send(getCommandBuffer());
        }
    };

}
