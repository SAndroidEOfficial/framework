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
package it.unibs.sandroide.lib.item.generalIO;

import java.util.HashMap;

/**
 * Support class for matching the {@link BLEGeneralIO} data.
 */
public class BLEGeneralIOComplements {

    public final static int UNAVAILABLE = 0xFF;
    public final static int INPUT = 0x00;
    public final static int OUTPUT = 0x01;
    public final static int ANALOG = 0x02;
    public final static int PWM = 0x03;
    public final static int SERVO = 0x04;

    public final static String UNAVAILABLE_STRING = "unavailable";
    public final static String INPUT_STRING = "input";
    public final static String OUTPUT_STRING = "output";
    public final static String ANALOG_STRING = "analog";
    public final static String PWM_STRING = "pwm";
    public final static String SERVO_STRING = "servo";

    public static HashMap<String, Integer> gpio_mode_base = new HashMap<>();
    static{
        gpio_mode_base.put(UNAVAILABLE_STRING, UNAVAILABLE);
        gpio_mode_base.put(INPUT_STRING, INPUT);
        gpio_mode_base.put(OUTPUT_STRING, OUTPUT);
        gpio_mode_base.put(ANALOG_STRING, ANALOG);
        gpio_mode_base.put(PWM_STRING, PWM);
        gpio_mode_base.put(SERVO_STRING, SERVO);
    }

    /**
     * Handles the Alarm MAp direction device configuration to library configuration.
     * @param gpio_mode_relation the HashMap which links the library keyword to the value
     * @return the maps with match the values direction device configuration to library configuration.
     */
    public static HashMap<Integer, Integer> returModeValueMapDeviceToLib
            (HashMap<String, Integer> gpio_mode_relation){
        HashMap<Integer, Integer> gpio_mode = new HashMap<>();
        for (String key: gpio_mode_relation.keySet()){
            gpio_mode.put(gpio_mode_relation.get(key), gpio_mode_base.get(key));
        }
        return gpio_mode;
    }

    /**
     * Handles the Alarm MAp direction library configuration to device configuration.
     * @param gpio_mode_relation the HashMap which links the library keyword to the value
     * @return the maps with match the values direction library configuration to device configuration.
     */
    public static HashMap<Integer, Integer> returModeValueMapLibToDevice
            (HashMap<String, Integer> gpio_mode_relation){
        HashMap<Integer, Integer> gpio_mode = new HashMap<>();
        for (String key: gpio_mode_relation.keySet()){
            gpio_mode.put(gpio_mode_base.get(key), gpio_mode_relation.get(key));
        }
        return gpio_mode;
    }

}
