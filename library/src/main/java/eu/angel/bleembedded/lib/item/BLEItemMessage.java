/**
 * @author  Angelo Vezzoli
 * @date    2016
 * @version 1.0
 *
 * Copyright (c) Angelo Vezzoli, University of Brescia (I guess), All Rights Reserved.
 *
 *
 * This software is the confidential and proprietary information of the authors and
 * the University of Brescia
 *
 */
package eu.angel.bleembedded.lib.item;

public class BLEItemMessage {

    public final float[] values;

    /**
     * The Item that generated this event. See
     * for details.
     */
    public BLEItem item;

    /**
     * The accuracy of this event. See for details.
     */
    public int accuracy;

    /**
     * The time in nanosecond at which the event happened
     */
    public long timestamp;

    BLEItemMessage(int valueSize) {
        values = new float[valueSize];
    }
	
}
