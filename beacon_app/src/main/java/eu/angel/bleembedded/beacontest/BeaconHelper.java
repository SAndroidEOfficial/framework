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
package eu.angel.bleembedded.beacontest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BeaconHelper {

    /**
     * Decodes the Radius Networks IBeacon proximity constant passed in and returns an
     * appropriate human readable String.
     * @param proximity constant expressing proximity from Radius Networks IBeacon class
     * @return human readable String representing that proximity
     */
    public static String getProximityString(int proximity) {
        String proximityString;
        switch (proximity) {
            case 1 : proximityString = "Immediate";
                break;
            case 2 : proximityString = "Near";
                break;
            case 3 : proximityString = "Far";
                break;
            default: proximityString = "Unknown";
        }
        return proximityString;
    }

    /**
     * Converts the Radius Networks beacon proximity value passed in and returns an
     * appropriate human readable String.
     * @param proximity double value expressing proximity in metres from Radius Networks beacon class
     * @return human readable String representing that proximity
     */
    public static String getProximityString(double proximity) {
        String proximityString;
        if (proximity == -1.0) {
            // -1.0 is passed back by the SDK to indicate an unknown distance
            proximityString = "Unknown";
        } else if (proximity < 0.5) {
            proximityString = "Immediate";
        } else if (proximity < 2.0) {
            proximityString = "Near";
        } else {
            proximityString = "Far";
        }
        return proximityString;
    }

    /**
     * Get the current date and time formatted as expected by PBS' application.
     * @return
     */
    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.US);
        Date now = new Date();
        return sdf.format(now);
    }

}

