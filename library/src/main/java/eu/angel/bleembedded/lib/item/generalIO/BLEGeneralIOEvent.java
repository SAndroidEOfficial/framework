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
package eu.angel.bleembedded.lib.item.generalIO;

/**
 * the event passed by the {@link BLEGeneralIO} through its interface
 * {@link BLEOnGeneralIOEventListener} callbacks.
 */
public class BLEGeneralIOEvent {

    /**
     * The value of this {@link BLEGeneralIOEvent}.
     */
    public final float[] values;

    /**
     * The action of this {@link BLEGeneralIOEvent}.
     */
    public final int event;

    /**
     * The {@link BLEGeneralIO} which triggered this {@link BLEGeneralIOEvent}.
     */
    public final BLEGeneralIO bleGeneralIO;

    /**
     * The accuracy of this {@link BLEGeneralIOEvent}.
     */
    public final int accuracy;

    /**
     * The time in nanosecond at which the event happened.
     */
    public final long timestamp;

    public BLEGeneralIOEvent(float[] values, int event,
                             BLEGeneralIO bleGeneralIO,int accuracy, long timestamp) {
        this.values = values;
        this.event=event;
        this.bleGeneralIO=bleGeneralIO;
        this.accuracy=accuracy;
        this.timestamp=timestamp;
    }
}

