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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import it.unibs.sandroide.lib.item.alarm.BLEAlarmComplements;
import it.unibs.sandroide.lib.item.generalIO.BLEGeneralIOComplements;

/**
 * Data incoming/outgoing from/to a paired remote device
 */
public class BLEDeviceData extends BLEData{


    private static final String TAG = "BLEDeviceData";
    public static final int UNIQUE_HANDLER_KEY= 0;



    private int format;
    private String id;
    int startOffset;
    int stopOffset;
    String dependency_id;
    //TODO: implement defaultValue
    float defaultValue=0;

    public int getStartOffset() {
        return startOffset;
    }

    public int getStopOffset() {
        return stopOffset;
    }

    public int getFormat() {
        return format;
    }

    public void setValueToDefault(){
        setValue(defaultValue);
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getDependency_id() {
        return dependency_id;
    }


    public void setValue(byte[] bytes, int offset, Object key){
        dataHandlerHashMap.get(key).handle(bytes, offset);
    }

    public void setValue(byte[] bytes, int offset){
        dataHandlerHashMap.get(UNIQUE_HANDLER_KEY).handle(bytes, offset);
    }

//    public void setValue(byte[] bytes, int offset, int length, Object key){
//        dataHandlerHashMap.get(key).handle(bytes, offset, length);
//    }
//
//    public void setValue(byte[] bytes, int offset, int length){
//        dataHandlerHashMap.get(UNIQUE_HANDLER_KEY).handle(bytes, offset, length);
//    }

    public void setOutputValue(float value){
        //super.setValue(dataHandlerHashMap.get(UNIQUE_HANDLER_KEY).handleFloat(value));
        dataHandlerHashMap.get(UNIQUE_HANDLER_KEY).handleFloat(value);
    }

    /**
     * Constructor
     * @param data_type the type of the {@link BLEData} (the available type is defined by the library)
     * @param format the format of the data (see {@link BLEDataConversionComplements})
     * @param id identifier of the {@link BLEDeviceData}
     * @param startOffset the start offset of the bytes composing the value of the
     *                      {@link BLEDeviceData}
     * @param  startOffset the stop offset of the bytes composing the value of the
     *                      {@link BLEDeviceData}
     * @param defaultValue the default value of the {@link BLEDeviceData}
     *                     (mainly used for outgoing messages)
     * @param dependency_id if this {@link BLEDeviceData} is dependent from another one, the
     *                      dependency_id is the identifier of the determinant {@link BLEDeviceData}
     */
    protected BLEDeviceData
            (int data_type, int format, String id,
             int startOffset, int stopOffset,
             float defaultValue, String dependency_id)
    {
        super(data_type);
        this.format=format;
        this.id=id;
        this.startOffset=startOffset;
        this.stopOffset=stopOffset;
        this.defaultValue=defaultValue;
        this.dependency_id=dependency_id;
    }

    /**
     * Clones the {@link BLEDeviceData}
     */
    public BLEDeviceData clone(){
        //dataMap could be shared between the data_type cloned
        BLEDeviceData bleDeviceData=new BLEDeviceData
                (data_type, format, id,
                        startOffset, stopOffset, defaultValue, dependency_id);
        bleDeviceData.value=this.value;
        if (this.sensor!=null)
            bleDeviceData.sensor=this.sensor.clone(bleDeviceData);

        for (Object key: dataHandlerHashMap.keySet()){
            bleDeviceData.putDataHandler(key, dataHandlerHashMap.get(key).clone(bleDeviceData));
        }
        return bleDeviceData;
    }

    /**
     * Returns the {@link DataHandler} depending on the characteristic of the {@link BLEDeviceData}
     * exploiting the methods {@link BLEDataConversionComplements#intFormatToDataConversionInterface} and
     * {@link BLEDataHandlingComplements#intData_typeToDataHandleInterface}
     * @return {@link DataHandler} which handles the value of the {@link BLEDeviceData}
     */
    private DataHandler handleDefinition(int data_type, String handle_type, int format,
                                         int bit_logic_op, String bit_logic_op_value,
                                        HashMap<Object,Object> dataMap,
                                        float intercept, float slope, float handle_value,
                                         int startOffset, int stopOffset,
                                         ByteLogicHandler byteLogicHandler){

        DataConversionInterface dataConversionInterfaceFormat=
                BLEDataConversionComplements.intFormatToDataConversionInterface
                        (format, bit_logic_op, bit_logic_op_value, byteLogicHandler);

        DataHandleInterface dataHandleInterface=
                BLEDataHandlingComplements.intData_typeToDataHandleInterface(data_type, handle_type);

            // Log.d(TAG, "xx__ dhi: "+dataHandleInterface+", dt: "+data_type+", ht: "+handle_type);

        return new DataHandler(dataConversionInterfaceFormat, dataHandleInterface,
                dataMap, intercept, slope, handle_value, data_type, handle_type, format,
                bit_logic_op, bit_logic_op_value, startOffset, stopOffset, byteLogicHandler);


    }

    //HashMap for the data handlers of the data
    public HashMap<Object, DataHandler> dataHandlerHashMap=new HashMap<>();
    private void putDataHandler(Object key, DataHandler dataHandler){
        dataHandlerHashMap.put(key, dataHandler);
        Log.d(TAG, "map"+dataHandlerHashMap);
    }

    /**
     * Returns the HashMap which links the incoming/outgoing data value with the related library/remote
     * device value
     * @param data_type type of the data to get the right map
     * @param relation the HashMap which links the library keyword to the value (retrieved from the xml
     *                 file)
     * @param isInput true if the direction of the data flow is from the remote device to the library,
     *                false otherwise
     * @return the HashMAp linking the incoming/outgoing data value with the related library/remote value
     */
    private static HashMap<Object, Object> getDataMap
            (int data_type, HashMap<String, ?> relation, boolean isInput){

        switch (data_type){
            case ParsingComplements.DT_GPIO_MODE:
                HashMap<String, Integer> gpio_mode_relation = new HashMap<>();
                for (String key: relation.keySet())
                    gpio_mode_relation.put(key, (Integer)relation.get(key));
                if (isInput)
                    return new HashMap<Object, Object>
                    (BLEGeneralIOComplements.returModeValueMapDeviceToLib(gpio_mode_relation)) ;
                else
                    return new HashMap<Object, Object>
                            (BLEGeneralIOComplements.returModeValueMapLibToDevice(gpio_mode_relation)) ;

            case ParsingComplements.DT_ALARM_LEVEL:
                HashMap<String, Integer> alarm_level_relation = new HashMap<>();
                for (String key: relation.keySet())
                    alarm_level_relation.put(key, (Integer)relation.get(key));
                if (isInput)
                    return new HashMap<Object, Object>
                            (BLEAlarmComplements.returAlarmLevelMapDeviceToLib(alarm_level_relation)) ;
                else
                    return new HashMap<Object, Object>
                            (BLEAlarmComplements.returAlarmLevelMapLibToDevice(alarm_level_relation)) ;

            default:
                return null;

        }

    }
    /**
     * Returns the default data depending on the data type
     */
    public static float getDataTypeDefaultDefaultValue(int type){
            switch (type){

                case ParsingComplements.DT_FW_VESION:
                case ParsingComplements.DT_BATTERY:
                case ParsingComplements.DT_SENSOR:
                    return 0;
                case ParsingComplements.DT_GPIO_MODE:
                    return BLEGeneralIOComplements.INPUT;
                case ParsingComplements.DT_ALARM_LEVEL:
                    return BLEAlarmComplements.ALARM_LOW_LEVEL;

                default:
                    return 0;
            }
    }

    public static float getDataValueFloatFromFormat(int format, String value){
            switch (format){

                case BLEDataConversionComplements.FORMAT_UINT8:
                case BLEDataConversionComplements.FORMAT_UINT16:
                case BLEDataConversionComplements.FORMAT_UINT32:
                case BLEDataConversionComplements.FORMAT_SINT8:
                case BLEDataConversionComplements.FORMAT_SINT16:
                case BLEDataConversionComplements.FORMAT_SINT32:
                case BLEDataConversionComplements.FORMAT_SFLOAT:
                case BLEDataConversionComplements.FORMAT_FLOAT:
                    return Float.parseFloat(value);

                case BLEDataConversionComplements.FORMAT_CHAR8:
                case BLEDataConversionComplements.FORMAT_CHAR16:
                    return (float) value.charAt(0);

                case BLEDataConversionComplements.FORMAT_LONG:
                    return -1;
                default:
                    //TODO exception...
                    return -1;
            }
    }

    /**
     * Class for handing the incoming data
     */
    private class DataHandler{

        private DataConversionInterface dataConversionInterface;
        private DataHandleInterface dataHandleInterface;
        private HashMap<Object,Object> dataMap;
        private float intercept;
        private float slope;
        private float handle_value;
        private int data_type;
        String handle_type;
        int format;
        Object key_value;
        int logic_operation;
        String logicOperationValue;
        int startOffset;
        int stopOffset;
        int length;
        ByteLogicHandler byteLogicHandler;

        /**
         * Constructor
         * @param dataConversionInterface {@link DataConversionInterface} for handling the conversion
         *                                                               of the incoming/outgoing data
         * @param dataHandleInterface {@link DataHandleInterface} for handling the incoming/outgoing data
         * @param dataMap the HashMAp linking the incoming/outgoing data value with the related
         *                library/remote device (if needed, for non-linear conversion)
         * @param intercept intercept for setting the value to library/remote device format (for linear conversion)
         * @param slope slope for setting the value to library/remote device format (for linear conversion)
         * @param handle_value value used for some handling mode (e.g. onset)
         * @param data_type the type of the data
         * @param handle_type the type of the data handling
         * @param format the format of the data
         * @param logic_operation the bit_logic operation (if needed,
         *                        see {@link BLEDataConversionComplements#intFormatToDataConversionInterface})
         * @param logicOperationValue value to use for the bit_logic operation
         * @param startOffset start Offset index of the bytes to handle in the incoming/outgoing
         *                    message
         * @param stopOffset stop Offset index of the bytes to handle in the incoming/outgoing
         *                    message
         * @param byteLogicHandler {@link ByteLogicHandler} for make the byte_logic operation (if needed)
         *
         */
        DataHandler(DataConversionInterface dataConversionInterface,
                    DataHandleInterface dataHandleInterface, HashMap<Object,Object> dataMap,
                    float intercept, float slope, float handle_value,
                    int data_type, String handle_type, int format, int logic_operation,
                            String logicOperationValue, int startOffset, int stopOffset,
                    ByteLogicHandler byteLogicHandler) {
            this.dataConversionInterface=dataConversionInterface;
            this.dataHandleInterface=dataHandleInterface;
            this.handle_value=handle_value;
            this.slope=slope;
            this.intercept=intercept;
            this.dataMap=dataMap;
            this.data_type=data_type;
            this.handle_type=handle_type;
            this.format=format;
            this.logic_operation=logic_operation;
            this.logicOperationValue=logicOperationValue;
            this.startOffset=startOffset;
            this.stopOffset=stopOffset;
            this.length=stopOffset-startOffset+1;
            this.byteLogicHandler=byteLogicHandler;
        }

        public void setKey_value(Object key_value){
            this.key_value=key_value;
        }

        public Object getKey_value() {
            return key_value;
        }

        /**
         * handles the incoming message based on the setting declared on the description file
         * (data format (e.g. UINT8, UINT16...), data type (e.g. GPIO_MODE, SENSOR), handle type
         * (e.g. onSet, scaled...)) setting directly the {@link BLEData#value} of of the {@link BLEDeviceData} owner
         * of this {@link DataHandler}. In case the {@link BLEDeviceData} is dependent by another one
         * this method change the {@link BLEData#data_type} of the {@link BLEDeviceData} owner, with the
         * value of the {@link DataHandler#data_type} of this {@link DataHandler}.
         * @param bytes the incoming message
         * @param absolute_offset the offset in the incoming message for bytes of interest.
         */
        public void handle(byte[] bytes, int absolute_offset){
            Log.d(TAG,"HandleType: "+handle_type);
            int offset=startOffset+absolute_offset;
            BLEDeviceData.this.data_type=DataHandler.this.data_type;
            BLEDeviceData.this.value=dataHandleInterface.handle
                    (dataConversionInterface.convert(bytes, offset, length),dataMap, intercept,
                            slope, handle_value);
            //BLEDeviceData.this.value = ByteBuffer.wrap(bytes,offset,length).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }

        /**
         * handles the incoming message based on the setting declared on the description file
         * (data format (e.g. UINT8, UINT16...), data type (e.g. GPIO_MODE, SENSOR), handle type
         * (e.g. onSet, scaled...)) setting directly the {@link BLEData#value} of of the {@link BLEDeviceData} owner
         * of this {@link DataHandler}. In case the {@link BLEDeviceData} is dependent by another one
         * this method change the {@link BLEData#data_type} of the {@link BLEDeviceData} owner, with the
         * value of the {@link DataHandler#data_type} of this {@link DataHandler}.
         * @param value the value to be handled to set the {@link BLEData#value}
         */
        public void handleFloat(float value){
            Log.d(TAG,"HandleType: "+handle_type);
            BLEDeviceData.this.data_type=DataHandler.this.data_type;
            BLEDeviceData.this.value=dataHandleInterface.handle(value,
                    dataMap, intercept,
                    slope, handle_value);
        }

        /**
         * Clones the {@link DataHandler}
         */
        public DataHandler clone(BLEDeviceData bleDeviceData){
            ByteLogicHandler byteLogicHandler=null;
            if (this.byteLogicHandler!=null)
                byteLogicHandler=this.byteLogicHandler.clone();
            return bleDeviceData.handleDefinition(data_type, handle_type, format,
                    logic_operation, logicOperationValue,
            dataMap, intercept, slope, handle_value, startOffset, stopOffset,
                    byteLogicHandler);
        }
    }

    public static class Builder extends BLEData.Builder{

        String format;
        public String id;
        public String position;
        //String intercept;
        //String slope;
        //String handle_type;
        //String handle_value;
        //List<String> specials=new ArrayList<>();
        String defaultValue;
        List<DataHandlerBuilder> dataHandlerBuilders=new ArrayList<>();
        String key_format;
        public String dependency_id;

        @Override
        public BLEDeviceData build(){


           // Log.d(TAG, "x_ id: "+id);

            int startOffset=0;
            int stopOffset=0;
            if (position==null){
                //TODO: exception
                Log.d(TAG, "pos==null");
            }
            String[] pos = position.split("-");
            if (pos.length!=2){
                //TODO: exception
                Log.d(TAG, "pos.len!=2");
            }
            try {
                startOffset = Integer.parseInt(pos[0]);
                stopOffset = Integer.parseInt(pos[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                //TODO: exception
            }

//            float handle_value;
//            if (this.handle_value!=null)
//                handle_value=Float.parseFloat(this.handle_value);
//            else
//                handle_value=0;
            int data_typeInt=ParsingComplements.getDataTypeIntFromString(data_type);

            int formatInt=BLEDataConversionComplements.getDataFormatIntFromString(format);

            float defaultValueFloat;
            if (defaultValue==null)
                defaultValueFloat=getDataTypeDefaultDefaultValue(data_typeInt);
            else
                defaultValueFloat=getDataValueFloatFromFormat(formatInt, defaultValue);

//            float interceptFloat=0;
//            if (intercept!=null)
//                interceptFloat=ParsingComplements.floatFromStringOrZero(intercept);
//            float slopeFloat=1;
//            if (slope!=null)
//                slopeFloat=ParsingComplements.floatFromStringOrZero(slope);


            //DataHandler dataHandler=handleDefinition(data_typeInt, handle_type, formatInt);


            BLEDeviceData bleDeviceData=new BLEDeviceData
                    (data_typeInt, formatInt, id,
                            startOffset, stopOffset, defaultValueFloat, dependency_id);
            if (data_typeInt== ParsingComplements.DT_SENSOR)
                bleDeviceData.new Sensor(sensor_type,
                        description,
                        accuracy,
                        drift,
                        measurementRange,
                        measurementFrequency,
                        measurementLatency,
                        precision,
                        resolution,
                        responseTime,
                        selectivity,
                        detectionLimit,
                        condition,
                        sampleRate,
                        unit);


//            if (dataHandlerBuilders.size()==0)
//                Log.d(TAG, "x_ id: "+id);
            for (DataHandlerBuilder dataHandlerBuilder:dataHandlerBuilders){
                DataHandler dataHandler=dataHandlerBuilder.build(bleDeviceData);
                //set the key, if the data is not a dependent data the key is set to UNIQUE_HANDLER_KEY
                if (key_format!=null)
                    dataHandler.setKey_value(BLEDataHandlingComplements.getHandlerKey
                            (dataHandlerBuilder.key_value, key_format));
                else
                    dataHandler.setKey_value(UNIQUE_HANDLER_KEY);
                //Log.d(TAG, "x_ id: "+id+", key_v: "+dataHandler.getKey_value()+", dh: "+dataHandler);
                bleDeviceData.putDataHandler(dataHandler.getKey_value(), dataHandler);
            }

            return bleDeviceData;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

//        public Builder setIntercept(String intercept) {
//            this.intercept = intercept;
//            return this;
//        }
//
//        public Builder setSlope(String slope) {
//            this.slope = slope;
//            return this;
//        }
//
//        public Builder setHandle_type(String handle_type) {
//            this.handle_type = handle_type;
//            return this;
//        }
//
//        public Builder setHandle_value(String handle_value) {
//            this.handle_value = handle_value;
//            return this;
//        }

        public Builder setPosition(String position) {
            this.position = position;
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setDataHandlerBuilders(List<DataHandlerBuilder> dataHandlerBuilders) {
            this.dataHandlerBuilders = dataHandlerBuilders;
            return this;
        }

        public Builder addDataHandlerBuilder(DataHandlerBuilder dataHandlerBuilder) {
            this.dataHandlerBuilders.add(dataHandlerBuilder);
            return this;
        }

        public Builder setKey_format(String key_format) {
            this.key_format = key_format;
            return this;
        }

        public Builder setDependency_id(String dependency_id) {
            this.dependency_id = dependency_id;
            return this;
        }

    }

    public static class DataHandlerBuilder{

        String intercept;
        String slope;
        String handle_value;
        String handle_type;
        List<String> specials=new ArrayList<>();
        String format;
        String data_type;
        String key_value;
        String logicOperation;
        String logicOperationValue;
        String position;
        String byteLogic;
        boolean isInput=true;

        private DataHandler build(BLEDeviceData bleDeviceData){


            /////////
            int startOffset=0;
            int stopOffset=0;
            if (position==null){
                //TODO: exception
                Log.d(TAG, "pos==null");
            }
            String[] pos = position.split("-");
            if (pos.length!=2){
                //TODO: exception
                Log.d(TAG, "pos.len!=2");
            }
            try {
                startOffset = Integer.parseInt(pos[0]);
                stopOffset = Integer.parseInt(pos[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                //TODO: exception
            }
            /////////


            float handle_value;
            if (this.handle_value!=null)
                handle_value=Float.parseFloat(this.handle_value);
            else
                handle_value=0;

            float interceptFloat=0;
            if (intercept!=null)
                interceptFloat=ParsingComplements.floatFromStringOrZero(intercept);
            float slopeFloat=1;
            if (slope!=null)
                slopeFloat=ParsingComplements.floatFromStringOrZero(slope);


            int data_typeInt=ParsingComplements.getDataTypeIntFromString(data_type);

            int formatInt=BLEDataConversionComplements.getDataFormatIntFromString(format);
            HashMap<Object, Object> dataMap=getDataMap(data_typeInt, BLEDataHandlingComplements
                    .getSpecialMap(formatInt, specials), isInput);

            int logic_operationInt=BLEDataConversionComplements.getBitLogicOperationIntFromString(logicOperation);
            ByteLogicHandler byteLogicHandler=null;
            if (byteLogic!=null)
                byteLogicHandler=new ByteLogicHandler(byteLogic);

            return bleDeviceData.handleDefinition(data_typeInt, handle_type, formatInt,
                    logic_operationInt, logicOperationValue,
                    dataMap, interceptFloat, slopeFloat, handle_value, startOffset, stopOffset,
                    byteLogicHandler);
        }

        public DataHandlerBuilder setSpecials(List<String> special) {
            this.specials = special;
            return this;
        }

        public DataHandlerBuilder addSpecial(String special) {
            this.specials.add(special);
            return this;
        }

        public DataHandlerBuilder setIntercept(String intercept) {
            this.intercept = intercept;
            return this;
        }

        public DataHandlerBuilder setSlope(String slope) {
            this.slope = slope;
            return this;
        }

        public DataHandlerBuilder setHandle_value(String handle_value) {
            this.handle_value = handle_value;
            return this;
        }

        public DataHandlerBuilder setHandle_type(String handle_type) {
            this.handle_type = handle_type;
            return this;
        }

        public DataHandlerBuilder setFormat(String format) {
            this.format = format;
            return this;
        }

        public DataHandlerBuilder setData_type(String data_type) {
            this.data_type = data_type;
            return this;
        }

        public DataHandlerBuilder setKey_value(String key_value) {
            this.key_value = key_value;
            return this;
        }

        public DataHandlerBuilder setLogicOperation(String logicOperation) {
            this.logicOperation = logicOperation;
            return this;
        }

        public DataHandlerBuilder setLogicOperationValue(String logicOperationValue) {
            this.logicOperationValue = logicOperationValue;
            return this;
        }

        public DataHandlerBuilder setPosition(String position) {
            this.position = position;
            return this;
        }

        public DataHandlerBuilder setByteLogic(String byteLogic) {
            this.byteLogic = byteLogic;
            return this;
        }

        public DataHandlerBuilder setIsInput(boolean input) {
            isInput = input;
            return this;
        }
    }

}
