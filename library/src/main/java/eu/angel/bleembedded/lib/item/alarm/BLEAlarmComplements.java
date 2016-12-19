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
package eu.angel.bleembedded.lib.item.alarm;

import java.util.HashMap;

/**
 * Support class for matching the {@link BLEAlarm} data.
 */
public class BLEAlarmComplements {

    public static final int ALARM_LOW_LEVEL = 1;
    public static final int ALARM_HIGH_LEVEL = 2;

    public final static String ALARM_LOW_LEVEL_STRING = "low";
    public final static String ALARM_HIGH_STRING = "high";

    public static HashMap<String, Integer> alarm_level_base = new HashMap<>();

    static{
        alarm_level_base.put(ALARM_LOW_LEVEL_STRING, ALARM_LOW_LEVEL);
        alarm_level_base.put(ALARM_HIGH_STRING, ALARM_HIGH_LEVEL);

    }

    /**
     * Handles the Alarm MAp direction device configuration to library configuration.
     * @param alarm_level_relation the HashMap which links the library keyword to the value (retrieved from the xml
     *                 file)
     * @return the maps with match the values direction device configuration to library configuration.
     */
    public static HashMap<Integer, Integer> returAlarmLevelMapDeviceToLib
            (HashMap<String, Integer> alarm_level_relation){
        HashMap<Integer, Integer> gpio_mode = new HashMap<>();
        for (String key: alarm_level_base.keySet()){
            gpio_mode.put(alarm_level_base.get(key), alarm_level_base.get(key));
        }
        return gpio_mode;
    }

    /**
     * Handles the Alarm MAp direction library configuration to device configuration.
     * @param alarm_level_relation the HashMap which links the library keyword to the value (retrieved from the xml
     *                 file)
     * @return the maps with match the values direction library configuration to device configuration.
     */
    public static HashMap<Integer, Integer> returAlarmLevelMapLibToDevice
            (HashMap<String, Integer> alarm_level_relation){
        HashMap<Integer, Integer> gpio_mode = new HashMap<>();
        for (String key: alarm_level_base.keySet()){
            gpio_mode.put(alarm_level_base.get(key), alarm_level_base.get(key));
        }
        return gpio_mode;
    }

}
