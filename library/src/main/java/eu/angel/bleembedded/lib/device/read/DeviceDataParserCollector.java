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

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.angel.bleembedded.lib.data.BLEDataConversionComplements;
import eu.angel.bleembedded.lib.data.BLEDeviceData;
import eu.angel.bleembedded.lib.data.ParsingComplements;

/**
 * Collector of {@link DeviceDataParser}s related to a unique message parsing. Makes available
 * the description to actuate the message parsing
 */
public class DeviceDataParserCollector {
    public final static String TAG="DeviceDataParserC";



    private String id;
    private int composition_type;
    private int parsing_type;
    //the parsing format can be only UINT8 or CHAR8
    //FIXME: only CHAR8 parsing_form is handled
    private int parsing_format;
    private int semantic_startOffset;
    private int semantic_stopOffset;

    private DeviceDataParser[] deviceDataParsers;

    /**
     * {@link DeviceDataParserCollector} constructor
     * @param id of the {@link DeviceDataParserCollector}
     * @param composition_type whether the message is composed by a single part or it is composed
     *                         by more parsable parts
     * @param parsing_type type of parsing (semantic or position)
     * @param parsing_format for semantic parsing (char8 or uint8)
     * @param semantic_startOffset for semantic parsing the start offset of the message
     * @param deviceDataParsers {@link DeviceDataParser}s array. If the parsing is semantic the
     *                          {@link DeviceDataParserCollector} may own more than one
     *                          {@link DeviceDataParser}s
     *
     *
     */
    DeviceDataParserCollector(String id, int composition_type, int parsing_type, int parsing_format,
                              int semantic_startOffset, int semantic_stopOffset,
                              DeviceDataParser[] deviceDataParsers){
        this.id=id;
        this.composition_type=composition_type;
        this.parsing_type = parsing_type;
        this.parsing_format=parsing_format;
        this.deviceDataParsers=deviceDataParsers;
        this.semantic_startOffset=semantic_startOffset;
        this.semantic_stopOffset=semantic_stopOffset;
    }

    public String getId() {
        return id;
    }

    public int getComposition_type() {
        return composition_type;
    }

    public int getParsing_type() {
        return parsing_type;
    }

    public int getParsing_format() {
        return parsing_format;
    }

    public DeviceDataParser[] getDeviceDataParsers() {
        return deviceDataParsers;
    }

    public List<List<BLEDeviceDataCluster>> cloneListOfDeviceDataClusters(){
        List<List<BLEDeviceDataCluster>> bLEDeviceDataClustersList= new ArrayList<>();
        for (DeviceDataParser deviceDataParser:deviceDataParsers){
            bLEDeviceDataClustersList.add(deviceDataParser.cloneDataModel());
        }
        return bLEDeviceDataClustersList;
    }

    /**
     * This method analyzes the byte array received. The method handles the different parsing type:
     * <dl><dt>- {@link eu.angel.bleembedded.lib.data.ParsingComplements#PARSING_POSITION}</dt> <dd>holds the
     * {@link DeviceDataParser} array at an unique element.</dd>
     * <p></p><dt>- {@link ParsingComplements#PARSING_SEMANTIC}</dt> <dd>allows the message to be variable following
     * the semantic features defined in the xml files descriptors. In order to handle the variable length
     * of this kind of parsing the method use an absolute_offset to help spatial successive parsings
     * on the same byte array. The parsing of the data is always considered starting the byte very after
     * the semantic_id byte. Semantic Parsing has two sub parsing kind: <dl><dt>- {@link BLEDataConversionComplements#FORMAT_CHAR8}</dt> <dd>which tests
     * the parser semantic_id with a char. The {@link BLEDeviceDataCluster} Lists int the List
     * <t>bleDeviceDataClustersList</t> are ordered by the maximum stopOffset of the {@link BLEDeviceData}
     * of the {@link BLEDeviceDataCluster}.</dd>
     * <dt>- {@link BLEDataConversionComplements#FORMAT_UINT8}</dt> <dd>which tests the parser semantic_id with an integer.
     * The {@link BLEDeviceDataCluster} Lists int the List <t>bleDeviceDataClustersList</t> are
     * ordered and located by the semantic_id of the {@link BLEDeviceDataCluster} itself.</dd>
     * </dd></dl>
     *
     *
     *
     * @param bleDeviceDataClustersList list of {@link BLEDeviceDataCluster} which will be updated.
     * @param bytes the byte array to analyze
     */
    public void updateDataFromByteArray
        (List<List<BLEDeviceDataCluster>> bleDeviceDataClustersList, byte[] bytes)
    {
        //TODO: throw exception whether the parser and message length doesn't match
        Log.d(TAG, "bytes[] length: "+bytes.length+ ", bytes[]: "+ Arrays.toString(bytes));
        int absolute_offset=0;
        List<BLEDeviceDataCluster> bleDeviceDataClustersToBeTriggered=new ArrayList<>();
        if (parsing_type== ParsingComplements.PARSING_POSITION) {
            deviceDataParsers[0]
                    .updateDataFromByteArray(bleDeviceDataClustersList.get(0), bytes, absolute_offset,
                            bleDeviceDataClustersToBeTriggered);
        } else if (parsing_type==ParsingComplements.PARSING_SEMANTIC){
            //TODO: test length check
            message_cycle:
            while(absolute_offset<bytes.length)
            {
                Log.d(TAG, "semantic_startOffset: "+semantic_startOffset+", abs offs: "+absolute_offset);
                if (parsing_format== BLEDataConversionComplements.FORMAT_CHAR8){
                    char test=(char) (bytes[semantic_startOffset+absolute_offset] & 0xFF);
                    Log.d(TAG, "test char: "+test);
                    absolute_offset+=semantic_startOffset+1;
                    for (int i=0;i<deviceDataParsers.length;i++){
                        if (deviceDataParsers[i].getSemanticIdChar()==test){
                            absolute_offset+=deviceDataParsers[i]
                                    .updateDataFromByteArray
                                            (bleDeviceDataClustersList.get(i), bytes, absolute_offset,
                                                    bleDeviceDataClustersToBeTriggered);
                            if (composition_type==ParsingComplements.SINGLE)
                                break message_cycle;
                            else
                                continue message_cycle;
                        }
                    }
                    Log.d(TAG, "unknown semantic id");
                    return;
                }else if (parsing_format== BLEDataConversionComplements.FORMAT_UINT8){
                    int test=(int) (bytes[semantic_startOffset+absolute_offset] & 0xFF);
                    Log.d(TAG, "test int: "+test);
                    absolute_offset+=semantic_startOffset+1;
                    for (int i=0;i<deviceDataParsers.length;i++){
                        if (deviceDataParsers[i].getSemanticIdInt()==test){
                            absolute_offset+=deviceDataParsers[i]
                                    .updateDataFromByteArray
                                            (bleDeviceDataClustersList.get(i), bytes, absolute_offset,
                                                    bleDeviceDataClustersToBeTriggered);
                            if (composition_type==ParsingComplements.SINGLE)
                                break message_cycle;
                            else
                                continue message_cycle;
                        }
                    }
                    Log.d(TAG, "unknown semantic id");
                    return;
                }
                Log.d(TAG, "not char semantic format");
                return;
            }

        }
        for (BLEDeviceDataCluster bleDeviceDataCluster:bleDeviceDataClustersToBeTriggered){
            Log.d(TAG, "triggered: "+bleDeviceDataCluster);
            bleDeviceDataCluster.triggerBleDeviceDataListener();}
    }

    public static class Builder{

        private String id;
        private String composition_type;
        private String parsing_type;
        private String parsing_format;
        private String semantic_position;
        private List<DeviceDataParser> deviceDataParsers=new ArrayList<>();

        public DeviceDataParserCollector build(){

            DeviceDataParser[] array=  new DeviceDataParser[deviceDataParsers.size()];
            for (int i=0;i< deviceDataParsers.size();i++){
                array[i]=deviceDataParsers.get(i);
            }

            //TODO: gestira la futura exception.. deve avere un valore solo se il parsing type è semantic<<<
            int parsFormat=BLEDataConversionComplements.getDataFormatIntFromString(parsing_format);
            int parsType=ParsingComplements.getParsingTypeIntFromString(parsing_type);

            int startOffset=0;
            int stopOffset=0;
            if (parsType==ParsingComplements.PARSING_SEMANTIC){
                String[] pos = semantic_position.split("-");
                if (pos==null){
                    //TODO: exception
                }
                if (pos.length!=2){
                    //TODO: exception
                }
                try {
                    startOffset = Integer.parseInt(pos[0]);
                    stopOffset = Integer.parseInt(pos[1]);
                    //TODO: throw exception if the length of the semantic field is longer than 1 B
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    //TODO: exception
                }
            }

            return new DeviceDataParserCollector(id,
                    ParsingComplements.getCompositionTypeIntFromString(composition_type),
                    parsType, parsFormat, startOffset, stopOffset, array);
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setComposition_type(String composition_type) {
            this.composition_type = composition_type;
            return this;
        }

        public Builder setParsing_type(String parsing_type) {
            this.parsing_type = parsing_type;
            return this;
        }

        public Builder setParsing_format(String parsing_format) {
            this.parsing_format = parsing_format;
            return this;
        }

        public Builder setDeviceDataParsers(List<DeviceDataParser> deviceDataParsers) {
            this.deviceDataParsers = deviceDataParsers;
            return this;
        }

        public Builder addDeviceDataParser(DeviceDataParser deviceDataParser) {
            this.deviceDataParsers.add(deviceDataParser);
            return this;
        }

        public Builder setSemantic_position(String semantic_position) {
            this.semantic_position = semantic_position;
            return this;
        }
    }


    //region parser of Parser
    public static List<DeviceDataParserCollector> parse(Context context) {

        final int NONE_PARSING = 0;
        final int FL_PARSER_COLLECTOR_PARSING = 1;
        final int SL_MULTIPLE_PARSING_PARSING = 2;
        final int TL_SUBPARSER_PARSING = 3;
        final int FOL_DATACLUSTER_PARSING = 4;
        final int FIL_SUB_DATA_PARSING = 5;
        final int FIL_DATA_HANDLING_PARSING = 6;
        final int SIL_SENSOR_PARSING = 7;
        final int SIL_DATA_HANDLING_PARSING = 8;
        final int SIL_DATA_SPECIAL_PARSING = 9;


        List<DeviceDataParser.Builder> deviceDataParserBuilders = new ArrayList<>();
        DeviceDataParser.Builder parserBuilder = new DeviceDataParser.Builder();
        BLEDeviceDataCluster.Builder dataClusterBuilder = new BLEDeviceDataCluster.Builder();
        List<BLEDeviceDataCluster.Builder> dataClusterBuilders = new ArrayList<>();
        BLEDeviceData.Builder subDataBuilder = new BLEDeviceData.Builder();
        BLEDeviceData.DataHandlerBuilder dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();

        List<BLEDeviceDataCluster> bleDeviceDataClusterModels=BLEDeviceDataCluster.parse(context);
        BLEDeviceDataCluster bleDeviceDataClusterModel=null;
        DeviceDataParserCollector.Builder deviceDataParserCollectorBuilder= new Builder();
        List<DeviceDataParserCollector> deviceDataParserCollectors=new ArrayList<>();
        String semanticFormatParsers="";
        String semanticFormatClusters="";

        String text="";

        //File file = new File(context.getFilesDir(), "bledeviceparsers.xml");

//            Uri uri = Uri.parse
//                    //("content://com.angelo.bleembeddedflasher.fileprovider/bleparsers/bleparsers.xml");
//                            ("content://eu.angel.bleembedded.beacontest.fileprovider/bleparsers/bleparsers.xml");

//            Uri uri= FileProvider.getUriForFile(context,
//                    "eu.angel.bleembedded.beacontest.fileprovider", file);

        //For debug purposes
        File file = new File(Environment.getExternalStorageDirectory(),"bledeviceparsers.xml");

        InputStream is= null;
        int parsingFirstLayerSection=NONE_PARSING;
        int parsingSecondLayerSection=NONE_PARSING;
        int parsingThirdLayerSection=NONE_PARSING;
        int parsingFourthLayerSection=NONE_PARSING;
        int parsingFifthLayerSection=NONE_PARSING;
        int parsingSixthLayerSection=NONE_PARSING;


        try {
            //is = context.getContentResolver().openInputStream(uri);
            is = new FileInputStream(file);
            Log.d(TAG, is.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.v("WriteFile","file created");

        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            xml_scan_while:
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parsingFirstLayerSection==NONE_PARSING)
                        {
                            if (tagname.equalsIgnoreCase("parser")) {
                                parsingFirstLayerSection=FL_PARSER_COLLECTOR_PARSING;
                                deviceDataParserCollectorBuilder=new Builder();
                                deviceDataParserBuilders=new ArrayList<>();
                            }
                        }else if (parsingFirstLayerSection==FL_PARSER_COLLECTOR_PARSING)
                        {
                            if (parsingSecondLayerSection==NONE_PARSING) {
                                if (tagname.equalsIgnoreCase("parsing")){
                                    semanticFormatParsers="";
                                    parsingSecondLayerSection=SL_MULTIPLE_PARSING_PARSING;
                                }
                            } else if (parsingSecondLayerSection==SL_MULTIPLE_PARSING_PARSING){
                                if (parsingThirdLayerSection==NONE_PARSING){
                                    if (tagname.equalsIgnoreCase("sub_parser")){
                                        dataClusterBuilders = new ArrayList<>();
                                        semanticFormatClusters="";
                                        parsingThirdLayerSection=TL_SUBPARSER_PARSING;
                                        parserBuilder=new DeviceDataParser.Builder();
                                    }
                                }
                                else if (parsingThirdLayerSection==TL_SUBPARSER_PARSING){
                                    if (parsingFourthLayerSection==NONE_PARSING){
                                        if (tagname.equalsIgnoreCase("dataCluster")){
                                            bleDeviceDataClusterModel=null;
                                            dataClusterBuilder=new BLEDeviceDataCluster.Builder();
                                            parsingFourthLayerSection=FOL_DATACLUSTER_PARSING;
                                        }
                                    } else if (parsingFourthLayerSection==FOL_DATACLUSTER_PARSING){
                                        if (parsingFifthLayerSection==NONE_PARSING){
                                            if (tagname.equalsIgnoreCase("data_handling")){
                                                parsingFifthLayerSection=FIL_DATA_HANDLING_PARSING;
                                            } else if (tagname.equalsIgnoreCase("subdata")){
                                                dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();
                                                subDataBuilder=new BLEDeviceData.Builder();
                                                parsingFifthLayerSection=FIL_SUB_DATA_PARSING;
                                            }
                                        } else if (parsingFifthLayerSection==FIL_SUB_DATA_PARSING){
                                            if (tagname.equalsIgnoreCase("sensor")) {
                                                subDataBuilder.setData_type(ParsingComplements.DT_SENSOR_STRING);
                                                parsingSixthLayerSection=SIL_SENSOR_PARSING;
                                            } else if (tagname.equalsIgnoreCase("data_handling")){
                                                parsingSixthLayerSection=SIL_DATA_HANDLING_PARSING;
                                            } else if (tagname.equalsIgnoreCase("special")){
                                                parsingSixthLayerSection=SIL_DATA_SPECIAL_PARSING;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if(parsingFirstLayerSection==NONE_PARSING)
                        {
                            if (tagname.equalsIgnoreCase("parsers")) {
                                Log.d(TAG, "end xml");
                                break xml_scan_while;
                            }
                        } else if(parsingFirstLayerSection==FL_PARSER_COLLECTOR_PARSING){
                            if (parsingSecondLayerSection==NONE_PARSING) {
                                if (tagname.equalsIgnoreCase("parser")) {
                                    boolean hasSemanticIdInt=false;
                                    if (semanticFormatParsers!=null){
                                    if (semanticFormatParsers.equalsIgnoreCase(BLEDataConversionComplements.UINT8_STRING))
                                        hasSemanticIdInt=true;}
                                    for (DeviceDataParser.Builder builder:deviceDataParserBuilders)
                                        deviceDataParserCollectorBuilder
                                                .addDeviceDataParser
                                                        (builder.setHasSemanticIdInt(hasSemanticIdInt)
                                                                .build());
                                    deviceDataParserCollectors.add(deviceDataParserCollectorBuilder.build());
                                    parsingFirstLayerSection=NONE_PARSING;
                                } else if (tagname.equalsIgnoreCase("parser_id")) {
                                    deviceDataParserCollectorBuilder.setId(text);
                                } else if (tagname.equalsIgnoreCase("composition_type")) {
                                    deviceDataParserCollectorBuilder.setComposition_type(text);
                                } else if (tagname.equalsIgnoreCase("parsing")) {
                                    parsingSecondLayerSection=NONE_PARSING;
                                }
                            } else if (parsingSecondLayerSection==SL_MULTIPLE_PARSING_PARSING) {
                                if (parsingThirdLayerSection==NONE_PARSING) {
                                    if (tagname.equalsIgnoreCase("parsing")) {
                                        parsingSecondLayerSection=NONE_PARSING;
                                    }else if (tagname.equalsIgnoreCase("parsing_type")) {
                                        deviceDataParserCollectorBuilder.setParsing_type(text);
                                    } else if (tagname.equalsIgnoreCase("format")) {
                                        semanticFormatParsers=text;
                                        deviceDataParserCollectorBuilder.setParsing_format(text);
                                    } else if (tagname.equalsIgnoreCase("semantic_position")) {
                                        deviceDataParserCollectorBuilder.setSemantic_position(text);
                                    }
                                } else if (parsingThirdLayerSection==TL_SUBPARSER_PARSING) {
                                    if (parsingFourthLayerSection==NONE_PARSING) {
                                        if (parsingFifthLayerSection==NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("sub_parser")) {
                                                parsingThirdLayerSection=NONE_PARSING;
                                                boolean hasSemanticIdInt=false;
                                                if (semanticFormatClusters!=null){
                                                    if (semanticFormatClusters.equalsIgnoreCase(BLEDataConversionComplements.UINT8_STRING))
                                                        hasSemanticIdInt=true;}
                                                for (BLEDeviceDataCluster.Builder bleDeviceDataClusterBuilder:dataClusterBuilders)
                                                    parserBuilder
                                                            .addDataModel(bleDeviceDataClusterBuilder
                                                                            .setHasSemanticIdInt(hasSemanticIdInt)
                                                                            .build());
                                                deviceDataParserBuilders.add(parserBuilder);
                                            } else if (tagname.equalsIgnoreCase("parser_id")) {
                                                parserBuilder.setParser_id(text);
                                            } else if (tagname.equalsIgnoreCase("semantic_id")) {
                                                parserBuilder.setSemanticId(text);
                                            } else if (tagname.equalsIgnoreCase("parsing_type")) {
                                                parserBuilder.setParsing_type(text);
                                            } else if (tagname.equalsIgnoreCase("format")) {
                                                semanticFormatClusters=text;
                                                parserBuilder.setParsing_format(text);
                                            } else if (tagname.equalsIgnoreCase("composition_type")) {
                                                parserBuilder.setComposition_type(text);
                                            } else if (tagname.equalsIgnoreCase("semantic_position")) {
                                                parserBuilder.setSemantic_position(text);
                                            }
                                        }
                                    } else if (parsingFourthLayerSection==FOL_DATACLUSTER_PARSING) {
                                        if (parsingFifthLayerSection==NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("dataCluster")) {
                                                //parserBuilder.addDataModel(dataClusterBuilder.build());
                                                if (bleDeviceDataClusterModel!=null)
                                                    dataClusterBuilder.setParameterFromModel(bleDeviceDataClusterModel);
                                                dataClusterBuilders.add(dataClusterBuilder);
                                                parsingFourthLayerSection=NONE_PARSING;
                                            }
                                            if (text!=null){
                                                if (tagname.equalsIgnoreCase("type")) {
                                                    dataClusterBuilder.setData_type(text);
                                                } else if (tagname.equalsIgnoreCase("id")) {
                                                    dataClusterBuilder.setId(text);
                                                } else if (tagname.equalsIgnoreCase("semantic_id")) {
                                                    dataClusterBuilder.setSemanticId(text);
                                                } else if (tagname.equalsIgnoreCase("dataClusterModel")){
                                                    for (BLEDeviceDataCluster bleDeviceDataCluster: bleDeviceDataClusterModels) {
                                                        if (bleDeviceDataCluster.getId().equalsIgnoreCase(text)){
                                                            bleDeviceDataClusterModel=bleDeviceDataCluster;
                                                            break;
                                                        }
                                                    }
                                                    if (bleDeviceDataClusterModel==null){
                                                        //TODO: exception model doesn't match any
                                                    }
                                                }
                                            }
                                        } else if (parsingFifthLayerSection==FIL_DATA_HANDLING_PARSING) {
                                            if (tagname.equalsIgnoreCase("data_handling")) {
                                                parsingFifthLayerSection=NONE_PARSING;
                                            } else if (tagname.equalsIgnoreCase("type")) {
                                                dataClusterBuilder.setData_handling(text);
                                            } else if (tagname.equalsIgnoreCase("value")) {
                                                float value=Float.parseFloat(text);
                                                dataClusterBuilder.setData_handlingValue(value);
                                            }
                                        }
                                        /*
                                        else if (parsingFifthLayerSection==FIL_SUB_DATA_PARSING){
                                            if (parsingSixthLayerSection==NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("subdata")) {
                                                    //if dependency_id==null data handler is defined directly in the subdata section...
                                                    // need to insert position indication for the hidden handler
                                                    if (subDataBuilder.dependency_id==null){
                                                        dataHandlerBuilder.setPosition(subDataBuilder.position);
                                                        subDataBuilder.addDataHandlerBuilder(dataHandlerBuilder);
                                                    }
                                                    Log.d(TAG, "id subdata SixthLayer");
                                                    dataClusterBuilder.addBleDeviceData(subDataBuilder.build());
                                                    parsingFifthLayerSection=NONE_PARSING;
                                                }
                                                if (text!=null){
                                                    if (tagname.equalsIgnoreCase("position")) {
                                                        subDataBuilder.setPosition(text);
                                                        Log.d(TAG, "data pos is: "+text);
                                                    } else if (tagname.equalsIgnoreCase("type")) {
                                                        subDataBuilder.setData_type(text);
                                                        dataHandlerBuilder.setData_type(text);
                                                    } else if (tagname.equalsIgnoreCase("format")) {
                                                        subDataBuilder.setFormat(text);
                                                        dataHandlerBuilder.setFormat(text);
                                                    } else if (tagname.equalsIgnoreCase("id")) {
                                                        subDataBuilder.setId(text);
                                                    } else if (tagname.equalsIgnoreCase("intercept")) {
                                                        dataHandlerBuilder.setIntercept(text);
                                                    } else if (tagname.equalsIgnoreCase("slope")) {
                                                        dataHandlerBuilder.setSlope(text);
                                                    }
                                                }
                                            } else if (parsingSixthLayerSection==SIL_DATA_HANDLING_PARSING){
                                                if (tagname.equalsIgnoreCase("data_handling")) {
                                                    parsingSixthLayerSection=NONE_PARSING;
                                                }else if (tagname.equalsIgnoreCase("type")) {
                                                    dataHandlerBuilder.setHandle_type(text);
                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                    dataHandlerBuilder.setHandle_value(text);
                                                }
                                            } else if (parsingSixthLayerSection==SIL_DATA_SPECIAL_PARSING){
                                                if (tagname.equalsIgnoreCase("special")) {
                                                    parsingSixthLayerSection=NONE_PARSING;
                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                    dataHandlerBuilder.addSpecial(text);
                                                }
                                            } else if (parsingSixthLayerSection==SIL_SENSOR_PARSING){
                                                if (tagname.equalsIgnoreCase("sensor")) {
                                                    parsingSixthLayerSection=NONE_PARSING;
                                                }else if (tagname.equalsIgnoreCase("type")) {
                                                    subDataBuilder.setSensor_type(text);
                                                    Log.d(TAG, "type sens is: "+text);
                                                } else if (tagname.equalsIgnoreCase("description")) {
                                                    subDataBuilder.setSensorDescription(text);
                                                } else if (tagname.equalsIgnoreCase("accuracy")) {
                                                    subDataBuilder.setSensorAccuracy(text);
                                                } else if (tagname.equalsIgnoreCase("drift")) {
                                                    subDataBuilder.setSensorDrift(text);
                                                } else if (tagname.equalsIgnoreCase("measurementRange")) {
                                                    subDataBuilder.setSensorMeasurementRange(text);
                                                } else if (tagname.equalsIgnoreCase("measurementFrequency")) {
                                                    subDataBuilder.setSensorMeasurementFrequency(text);
                                                } else if (tagname.equalsIgnoreCase("measurementLatency")) {
                                                    subDataBuilder.setSensorMeasurementLatency(text);
                                                } else if (tagname.equalsIgnoreCase("precision")) {
                                                    subDataBuilder.setSensorPrecision(text);
                                                } else if (tagname.equalsIgnoreCase("resolution")) {
                                                    subDataBuilder.setSensorResolution(text);
                                                } else if (tagname.equalsIgnoreCase("responseTime")) {
                                                    subDataBuilder.setSensorResponseTime(text);
                                                } else if (tagname.equalsIgnoreCase("selectivity")) {
                                                    subDataBuilder.setSensorSelectivity(text);
                                                } else if (tagname.equalsIgnoreCase("detectionLimit")) {
                                                    subDataBuilder.setSensorDetectionLimit(text);
                                                } else if (tagname.equalsIgnoreCase("sampleRate")) {
                                                    subDataBuilder.setSensorSampleRate(text);
                                                } else if (tagname.equalsIgnoreCase("condition")) {
                                                    subDataBuilder.setSensorCondition(text);
                                                } else if (tagname.equalsIgnoreCase("unit")) {
                                                    subDataBuilder.setSensorUnit(text);
                                                }
                                            }
                                        }*/
                                    }
                                }
                            }

                        }
                        text=null;
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.d(TAG, "Text is: "+text);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is!=null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            is=null;
        }

        return deviceDataParserCollectors;
    }
    //endregion



}
