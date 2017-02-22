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
package it.unibs.sandroide.lib.data;


import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Complements for parsing
 */
public class ParsingComplements {

    //region superDataParser type
    public final static String MULTIPLE_STRING="multiple";
    public final static String SINGLE_STRING="single";

    public final static int MULTIPLE=0;
    public final static int SINGLE=1;
    //endregion

    //region parsingType
    public final static String PARSING_POSITION_STRING="position";
    public final static String PARSING_SEMANTIC_STRING="semantic";

    public final static int PARSING_POSITION=0;
    public final static int PARSING_SEMANTIC=1;
    //endregion


    public static int getCompositionTypeIntFromString(String type){
        if (type!=null){
        switch (type){

            case SINGLE_STRING:
                return SINGLE;
            case MULTIPLE_STRING:
                return MULTIPLE;
            default:
                return SINGLE;
        }} else
            return SINGLE;
    }

    public static int getParsingTypeIntFromString(String type){
        if (type!=null){
        switch (type){

            case PARSING_POSITION_STRING:
                return PARSING_POSITION;
            case PARSING_SEMANTIC_STRING:
                return PARSING_SEMANTIC;
            default:
                return PARSING_POSITION;
        }} else
            return PARSING_POSITION;
    }


    //Warning the DataType has to be added also in intData_typeToDataHandleInterface of BLEDataHandlingComplements
    //class in order to choose the handle
    public static final String DT_FW_VESION_STRING = "fw";
    public static final String DT_BATTERY_STRING = "b";
    public static final String DT_SENSOR_STRING = "s";
    public static final String DT_ALARM_LEVEL_STRING = "alarm_level";
    public static final String DT_GPIO_MODE_STRING = "gpio_mode";
    public static final String DT_GPIO_DI_STRING = "gpio_di";
    public static final String DT_GPIO_DO_STRING = "gpio_do";
    public static final String DT_GPIO_PWM_STRING = "gpio_pwm";
    public static final String DT_GPIO_SERVO_STRING = "gpio_servo";
    public static final String DT_GPIO_AI_STRING = "gpio_ai";
    public static final String DT_GPIO_DIO_STRING = "gpio_dio";
    public static final String DT_TIME_STAMP_STRING = "time_stamp";

    public static final int DT_FW_VESION= 0;
    public static final int DT_BATTERY = 1;
    public static final int DT_SENSOR = 2;
    public static final int DT_ALARM_LEVEL = 3;
    public static final int DT_GPIO_MODE = 4;
    public static final int DT_GPIO_DI = 5;
    public static final int DT_GPIO_DO = 6;
    public static final int DT_GPIO_PWM = 7;
    public static final int DT_GPIO_SERVO = 8;
    public static final int DT_GPIO_AI = 9;
    public static final int DT_GPIO_DIO = 10;
    public static final int DT_TIME_STAMP = 11;

    public static int getDataTypeIntFromString(String type){
        if (type!=null){
            switch (type){

                case DT_FW_VESION_STRING:
                    return DT_FW_VESION;
                case DT_BATTERY_STRING:
                    return DT_BATTERY;
                case DT_SENSOR_STRING:
                    return DT_SENSOR;
                case DT_GPIO_MODE_STRING:
                    return DT_GPIO_MODE;
                case DT_GPIO_DI_STRING:
                    return DT_GPIO_DI;
                case DT_GPIO_DO_STRING:
                    return DT_GPIO_DO;
                case DT_GPIO_PWM_STRING:
                    return DT_GPIO_PWM;
                case DT_GPIO_SERVO_STRING:
                    return DT_GPIO_SERVO;
                case DT_GPIO_AI_STRING:
                    return DT_GPIO_AI;
                case DT_GPIO_DIO_STRING:
                    return DT_GPIO_DIO;
                case DT_ALARM_LEVEL_STRING:
                    return DT_ALARM_LEVEL;
                case DT_TIME_STAMP_STRING:
                    return DT_TIME_STAMP;

                default:
                    return DT_SENSOR;
            }
        }else
            return DT_SENSOR;
    }

    public static Float floatFromStringOrZero(String s){
        Float val = Float.valueOf(0);
        try{
            val = Float.valueOf(s);
        } catch(NumberFormatException ex){
            DecimalFormat df = new DecimalFormat();
            Number n = null;
            try{
                n = df.parse(s);
            } catch(ParseException ex2){
            }
            if(n != null)
                val = n.floatValue();
        }
        return val;
    }
}
