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
package eu.angel.bleembedded.lib.item.sensor;

public interface BLESingleSensorEventListener {


	    /**
	     * Called when sensor values have changed.
	     * <p>See {@link android.hardware.SensorManager SensorManager}
	     * for details on possible sensor types.
	     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.
	     *
	     * <p><b>NOTE:</b> The application doesn't own the
	     * {@link android.hardware.SensorEvent event}
	     * object passed as a parameter and therefore cannot hold on to it.
	     * The object may be part of an internal pool and may be reused by
	     * the framework.
	     *
	     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
	     */
	    void onSensorChanged(BLESensorEvent event);

	    /**
	     * Called when the accuracy of the registered sensor has changed.
	     *
	     * <p>See the SENSOR_STATUS_* constants in
	     * {@link android.hardware.SensorManager SensorManager} for details.
	     *
	     * @param accuracy The new accuracy of this sensor, one of
	     *         {@code SensorManager.SENSOR_STATUS_*}
	     */
	    void onAccuracyChanged(BLESensor sensor, int accuracy);
}
