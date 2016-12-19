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
package eu.angel.bleembedded.lib.data;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Support class for handling data
 */
public class BLEDataHandlingComplements {

    private static final String TAG="BLEDataHandlingC";

    //region handletype
    public final static String NONE_HANDLE="none";
    //FIXME: better handling onset...do not send callback if onset action is not performed
    public final static String ONSET_HANDLE="onset";
    public final static String SCALED_HANDLE="scaled";
    public final static String AND_HANDLE="and";
    public final static String UNUSED_HANDLE="unused";
    //endregion

    /**
     * returns the {@link DataHandleInterface} related to the type of the data and the defined handling
     * @param data_type the type of the data to be handled
     * @param handle_type how to handle the incoming data
     * @return {@link DataHandleInterface} used to handle the incoming data
     */
    public static DataHandleInterface intData_typeToDataHandleInterface(int data_type,String handle_type){


        if (data_type!=-1){
            switch(data_type){

                case ParsingComplements.DT_TIME_STAMP:
                case ParsingComplements.DT_GPIO_DIO:
                case ParsingComplements.DT_GPIO_DI:
                case ParsingComplements.DT_GPIO_DO:
                case ParsingComplements.DT_GPIO_PWM:
                case ParsingComplements.DT_GPIO_SERVO:
                case ParsingComplements.DT_GPIO_AI:
                case ParsingComplements.DT_FW_VESION:
                case ParsingComplements.DT_SENSOR:
                    if (handle_type!=null){
                        switch(handle_type){

                            //TODO: unused should do nothing
                            case UNUSED_HANDLE:
                            case NONE_HANDLE:
                                //noneDataHandleInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        return value;
                                    }
                                };

                            case ONSET_HANDLE:
                                //onsetDataHandleInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        //float res=dataConversionInterface.convert(bytes, offset, length);
                                        if (value==handle_value)
                                            return 1;
                                        else
                                            return 0;
                                    }
                                };

                            case SCALED_HANDLE:
                                //scaledDataHandleInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        return (value-intercept)*slope;
                                    }
                                };

                            default:
                                //noneDataHandleInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        return value;
                                    }
                                };
                        }
                    } else
                        //noneDataHandleInterface
                        return new DataHandleInterface() {
                            @Override
                            public float handle(float value, HashMap<Object,Object> dataMap,
                                                float intercept, float slope,
                                                float handle_value) {
                                return value;
                            }
                        };

                case ParsingComplements.DT_ALARM_LEVEL:
                case ParsingComplements.DT_GPIO_MODE:
                    if (handle_type!=null){
                        switch(handle_type){

                            //TODO: unused should do nothing
                            case UNUSED_HANDLE:
                                //noneDataHandleInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        return value;
                                    }
                                };

                            case NONE_HANDLE:
                                //noneDataHandleMappedInterface
                                return new DataHandleInterface() {
                                @Override
                                public float handle(float value, HashMap<Object,Object> dataMap,
                                                    float intercept, float slope,
                                                    float handle_value) {
                                    Log.d(TAG, "value: "+value+", dataMAp: "+dataMap);
                                    return (int)dataMap.get((int)value);
                                }
                            };

                            case ONSET_HANDLE:
                                //onsetDataHandleMappedInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value)  {
                                        //float res=dataConversionInterface.convert(bytes, offset, length);
                                        if ((int)dataMap.get((int)value)==handle_value)
                                            return 1;
                                        else
                                            return 0;
                                    }
                                };

                            case SCALED_HANDLE:
                                //scaledDataHandleMappedInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {

                                        return (((int)dataMap.get((int)value))-intercept)*slope;
                                    }
                                };

                            default:
                                //noneDataHandleMappedInterface
                                return new DataHandleInterface() {
                                    @Override
                                    public float handle(float value, HashMap<Object,Object> dataMap,
                                                        float intercept, float slope,
                                                        float handle_value) {
                                        Log.d(TAG, "value: "+value+", dataMAp: "+dataMap);
                                        return (int)dataMap.get((int)value);
                                    }
                                };
                        }
                    } else
                    //noneDataHandleMappedInterface
                        return new DataHandleInterface() {
                            @Override
                            public float handle(float value, HashMap<Object,Object> dataMap,
                                                float intercept, float slope,
                                                float handle_value) {
                                Log.d(TAG, "value: "+value+", dataMAp: "+dataMap);
                                return (int)dataMap.get((int)value);
                            }
                        };

                default:
                    //TODO throw exception mismatched data_type
                    return null;
            }
        }
        else{
            //TODO throw exception null data_type
            return null;
        }

    }

    /**
     * returns the HashMap which links the value incoming/outgoing with the related conversion
     * needed for the library/remote device (it'sused for non linear conversion of the data)
     * @param format the format of the data to convert
     * @param specials list of string reported on the xml file (the format of the special is
     *                 <key_word>:<value>) used to make the mapping
     */
    public static HashMap<String, Object> getSpecialMap(int format, List<String> specials){

        switch (format){

            case BLEDataConversionComplements.FORMAT_SINT16:
            case BLEDataConversionComplements.FORMAT_UINT16:
            case BLEDataConversionComplements.FORMAT_SINT8:
            case BLEDataConversionComplements.FORMAT_UINT8:
                HashMap<String, Integer> retInt=new HashMap<>();
                for (String special:specials){
                    String[] s=special.split(":");
                    int value=Integer.parseInt(s[1]);
                    retInt.put(s[0], value);
                }
                return new HashMap<String, Object>(retInt);


            case BLEDataConversionComplements.FORMAT_CHAR8:
            case BLEDataConversionComplements.FORMAT_CHAR16:
                HashMap<String, Character> retChar=new HashMap<>();
                for (String special:specials){
                    String[] s=special.split(":");
                    if (s[1].length()!=1){
                        //TODO:exception
                    }
                    char value=s[1].charAt(0);
                    retChar.put(s[0], value);
                }
                return new HashMap<String, Object>(retChar);

            default:
                //TODO: exception
                return null;
        }

    }

    /**
     * returns the key to retrieve the handler from the HashMap (the HashMap of
     * {@link DataHandleInterface} is required for example when a {@link BLEDeviceData}
     * depends from another one for the kind of handling). If the format of the key is char8
     * the key should be composed by a single char, whereas if it is uint8 the String has to be converted
     * @param key the key to retrieve the {@link DataHandleInterface} from its HashMap
     */
    public static Object getHandlerKey(String key, String key_format){
        int format=BLEDataConversionComplements.getDataFormatIntFromString(key_format);

        switch (format){
            case BLEDataConversionComplements.FORMAT_UINT8:
                return Integer.parseInt(key);

            case BLEDataConversionComplements.FORMAT_CHAR8:
                return key.charAt(0);

            default:
                //TODO: throw no compatible handle key format
                return null;
        }

    }

    /**
     * returns the key to retrieve the handler from the HashMap (the HashMap of
     * {@link DataHandleInterface} is required for example when a {@link BLEDeviceData}
     * depends from another one for the kind of handling). If the format of the key is char8
     * the key should be composed by a single char, whereas if it is uint8 the String has to be converted
     * @param key the key to retrieve the {@link DataHandleInterface} from its HashMap
     */
    public static Object getHandlerKey(float key, int format){

        switch (format){
            case BLEDataConversionComplements.FORMAT_UINT8:
                return (int) key;

            case BLEDataConversionComplements.FORMAT_CHAR8:
                return (char) key;

            default:
                //TODO: throw no compatible handle key format
                return null;
        }

    }
}
