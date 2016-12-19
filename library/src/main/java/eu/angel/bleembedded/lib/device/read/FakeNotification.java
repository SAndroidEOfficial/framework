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
package eu.angel.bleembedded.lib.device.read;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.angel.bleembedded.lib.device.DeviceControl;

/**
 * Emulates the notification mechanism using the reading of the characteristic and the timer
 */
public class FakeNotification {

    ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    final BluetoothGattCharacteristic charact;

    public FakeNotification(BluetoothGattCharacteristic charact){
        this.charact=charact;
    }

    /**
     * Starts the fake notification
     *
     * @param deviceControl {@link DeviceControl}
     */
    public void startFakeNotification
            (final DeviceControl deviceControl, long initialDelay, long sampleRate){

        final Runnable readChar = new Runnable() {
            @Override
            public void run() {
                deviceControl.readChar(charact);
            }
        };

        timer.scheduleAtFixedRate(readChar, initialDelay, sampleRate, TimeUnit.MILLISECONDS);

    }
}
