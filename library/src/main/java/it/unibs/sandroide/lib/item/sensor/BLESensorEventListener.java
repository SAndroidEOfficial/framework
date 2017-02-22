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
package it.unibs.sandroide.lib.item.sensor;

/**
 * The interface for the communication of the sensor to the Application.
 */
public interface BLESensorEventListener {


	    /**
	     * Called when sensor values have changed.
	     * <p>See {@link BLESensorManager SensorManager}
	     * for details on possible sensor types.
	     * <p>See also {@link BLESensorEvent SensorEvent}.
	     *
	     *
	     * @param event the {@link BLESensorEvent SensorEvent}.
	     */
	    void onSensorChanged(BLESensorEvent event);

	    /**
	     * Not implemented yet
		 * <p>Called when the accuracy of the registered sensor has changed.</p>
	     *
	     *
	     * @param accuracy The new accuracy of this sensor, one of
	     *         {@code BLESensorManager.SENSOR_STATUS_*}
	     */
	    void onAccuracyChanged(BLESensor sensor, int accuracy);
}
