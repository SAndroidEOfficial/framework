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
package it.unibs.sandroide.lib.device.read;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import it.unibs.sandroide.lib.complements.ArrayListAnySize;
import it.unibs.sandroide.lib.data.BLEDataConversionComplements;
import it.unibs.sandroide.lib.data.ParsingComplements;

public class DeviceDataParser {

    private static final String TAG = "DeviceDataParser";

    private String parser_id;
    //TODO: why not an array?
    /**
     * dataModel is sorted related to the Clusters parsing characteristic:
     * <p>-if the Cluster is parsed semantically by int ids the Clusters are ordered by their own ids.
     * In the current version of the library if the Cluster are parsed semantically by int ids they shall
     * be homogeneous</p>
     * <p>-in all the other parsing methods the dataModel each Cluster is sorted by the stopOffset index maximum
     * among its Data</p>
     */
    private List<BLEDeviceDataCluster> dataModel;
    private int composition_type;
    int semanticIdInt;
    char semanticIdChar;
    boolean hasSemanticIdInt;
    int parsing_type;
    //the parsing format can be only UINT8 or CHAR8
    int parsing_format;
    private int semantic_startOffset;
    private int semantic_stopOffset;



    //TODO: add length control for fixed length messages

    /**
     * updates the value and characteristics of the Data of the Cluster, analyzing the byte array
     * of the incoming message
     * @param bleDeviceDataClusters the cluster which will be updated
     * @param bytes the byte array of the incoming message
     * @param absolute_offset the absolute offset from which the relative offset of the Cluster start
     *                        to analyze the byte array ()
     * @return the length of the data analyzed by the bleDeviceDataClusters (relative, without the absolute_offset)
     *
     * @see DeviceDataParserCollector#updateDataFromByteArray(java.util.List, byte[])
     */
    public int updateDataFromByteArray
            (List<BLEDeviceDataCluster> bleDeviceDataClusters, byte[] bytes, int absolute_offset,
             List<BLEDeviceDataCluster> bleDeviceDataClustersToBeTriggered)
    {
        int totalLength=0;
        Log.d(TAG, "parsing_type: "+parsing_type);
        if (parsing_type== ParsingComplements.PARSING_POSITION){
            if (bleDeviceDataClusters.size()==dataModel.size())
            {
                for (int i=0;i<bleDeviceDataClusters.size();i++)
                {
                    bleDeviceDataClusters.get(i)
                            .readDataCluster(bytes, absolute_offset);

                    bleDeviceDataClustersToBeTriggered.add(bleDeviceDataClusters.get(i));
                }
            }
            totalLength=bleDeviceDataClusters.get(bleDeviceDataClusters.size()-1).getLength();
        } else if (parsing_type==ParsingComplements.PARSING_SEMANTIC){
            //TODO: maybe handle multiple semantic parsing
            Log.d(TAG, "parsing_format: "+parsing_format);
            if (parsing_format==BLEDataConversionComplements.FORMAT_CHAR8){
                Log.d(TAG, "abs offs: "+absolute_offset);
                Log.d(TAG, "semantic start offs: "+semantic_startOffset);
                char test=(char) (bytes[semantic_startOffset+absolute_offset] & 0xFF);
                for (int i=0;i<bleDeviceDataClusters.size();i++){
                    if (bleDeviceDataClusters.get(i).getSemanticIdChar()==test){

                        bleDeviceDataClusters.get(i)
                                .readDataCluster(bytes, absolute_offset+semantic_startOffset+1);

                        totalLength+=bleDeviceDataClusters.get(i).getLength();
                        bleDeviceDataClustersToBeTriggered.add(bleDeviceDataClusters.get(i));
                        if (composition_type==ParsingComplements.SINGLE)
                            break;
                    }
                }
            } else if (parsing_format== BLEDataConversionComplements.FORMAT_UINT8){
                Log.d(TAG, "abs offs: "+absolute_offset);
                Log.d(TAG, "semantic start offs: "+semantic_startOffset);
                int index=(int)bytes[semantic_startOffset+absolute_offset];
                Log.d(TAG, "index: "+index);

                bleDeviceDataClusters.get(index)
                        .readDataCluster(bytes, absolute_offset+semantic_startOffset+1);

                bleDeviceDataClustersToBeTriggered.add(bleDeviceDataClusters.get(index));
                totalLength=bleDeviceDataClusters.get(index).getLength()+semantic_startOffset+1;
            }
        }
        Log.d(TAG, "tot l: "+totalLength);
        return totalLength;
    }

    /**
     * Constructor
     * @param parser_id id of the parser
     * @param dataModel list of {@link BLEDeviceDataCluster}s which own the data in the message
     * @param composition_type whether the message is composed by one (single) or
     *                         more {@link BLEDeviceDataCluster}s (multiple)
     * @param semanticIdChar the id which matches with this {@link DeviceDataParser}
     * @param hasSemanticIdInt true if the semantic id is uint8 format
     * @param parsing_type the type of parsing. The cluster can be single or multiple (if the
     *                      {@link DeviceDataParserCollector}) is multiple
     *                      {@link DeviceDataParser} can't be multiple
     * @param parsing_format if the parsing_type is multiple the format of the
     *                       semanticId can be char8 or uint8
     * @param semantic_startOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             start offset
     * @param semantic_stopOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             stop offset
     *
     */
    private DeviceDataParser(String parser_id,
                            List<BLEDeviceDataCluster> dataModel, int composition_type,
                             char semanticIdChar, boolean hasSemanticIdInt, int parsing_type,
                             int parsing_format, int semantic_startOffset, int semantic_stopOffset){
        this.parser_id=parser_id;
        this.dataModel=dataModel;
        this.semanticIdChar=semanticIdChar;
        this.parsing_type=parsing_type;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.parsing_format=parsing_format;
        this.composition_type=composition_type;
        this.semantic_startOffset=semantic_startOffset;
        this.semantic_stopOffset=semantic_stopOffset;
    }

    /**
     * Constructor
     * @param parser_id id of the parser
     * @param dataModel list of {@link BLEDeviceDataCluster}s which own the data in the message
     * @param composition_type whether the message is composed by one (single) or
     *                         more {@link BLEDeviceDataCluster}s (multiple)
     * @param semanticIdInt the id which matches with this {@link DeviceDataParser}
     * @param hasSemanticIdInt true if the semantic id is uint8 format
     * @param parsing_type the type of parsing. The cluster can be single or multiple (if the
     *                      {@link DeviceDataParserCollector}) is multiple
     *                      {@link DeviceDataParser} can't be multiple
     * @param parsing_format if the parsing_type is multiple the format of the
     *                       semanticId can be char8 or uint8
     * @param semantic_startOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             start offset
     * @param semantic_stopOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             stop offset
     *
     */
    private DeviceDataParser(String parser_id,
                             List<BLEDeviceDataCluster> dataModel, int composition_type,
                             int semanticIdInt, boolean hasSemanticIdInt, int parsing_type,
                             int parsing_format, int semantic_startOffset, int semantic_stopOffset){
        this.parser_id=parser_id;
        this.dataModel=dataModel;
        this.semanticIdInt=semanticIdInt;
        this.parsing_type=parsing_type;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.parsing_format=parsing_format;
        this.composition_type=composition_type;
        this.semantic_startOffset=semantic_startOffset;
        this.semantic_stopOffset=semantic_stopOffset;
    }

    /**
     * Constructor
     * @param parser_id id of the parser
     * @param dataModel list of {@link BLEDeviceDataCluster}s which own the data in the message
     * @param composition_type whether the message is composed by one (single) or
     *                         more {@link BLEDeviceDataCluster}s (multiple)
     * @param hasSemanticIdInt true if the semantic id is uint8 format
     * @param parsing_type the type of parsing. The cluster can be single or multiple (if the
     *                      {@link DeviceDataParserCollector}) is multiple
     *                      {@link DeviceDataParser} can't be multiple
     * @param parsing_format if the parsing_type is multiple the format of the
     *                       semanticId can be char8 or uint8
     * @param semantic_startOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             start offset
     * @param semantic_stopOffset if the {@link DeviceDataParser} type is multiple point out the
     *                             stop offset
     *
     */
    private DeviceDataParser(String parser_id,
                             List<BLEDeviceDataCluster> dataModel, int composition_type,
                             boolean hasSemanticIdInt, int parsing_type,
                             int parsing_format, int semantic_startOffset, int semantic_stopOffset){
        this.parser_id=parser_id;
        this.dataModel=dataModel;
        this.parsing_type=parsing_type;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.parsing_format=parsing_format;
        this.composition_type=composition_type;
        this.semantic_startOffset=semantic_startOffset;
        this.semantic_stopOffset=semantic_stopOffset;
    }


    public String getParser_id() {
        return parser_id;
    }

    public List<BLEDeviceDataCluster> getData(){
        return Collections.unmodifiableList(this.dataModel);
    }

    public char getSemanticIdChar() {
        return semanticIdChar;
    }

    public int getSemanticIdInt() {
        return semanticIdInt;
    }

    public void setParserLayout(String parserLayout){
    }

//    public DeviceDataParser clone(){
//        List<BLEDeviceData> bleDeviceDatas=new ArrayList<>();
//        for (BLEDeviceData bleDeviceData:data)
//            bleDeviceDatas.add(bleDeviceData.clone());
//        return new DeviceDataParser(parser_id, bleDeviceDatas, dataStartOffset, dataStopOffset);
//    }

    /**
     * Clones the {@link BLEDeviceDataCluster}s of the {@link DeviceDataParser}
     * @return list of cloned {@link BLEDeviceDataCluster}s
     *
     */
    public List<BLEDeviceDataCluster> cloneDataModel(){
        List<BLEDeviceDataCluster> bleDeviceDataClusters = new ArrayList<>();
        for (int i=0;i<dataModel.size();i++){
            //if the Parsing is semantic integer i could have null cluster in the List, nevertheless
            //I must keep the order of the list with the null objects inside
            if (dataModel.get(i)!=null)
                bleDeviceDataClusters.add(dataModel.get(i).clone());
            else
                bleDeviceDataClusters.add(null);
        }
        return bleDeviceDataClusters;
    }


    public static class Builder{

        private String parser_id;
        private List<BLEDeviceDataCluster> dataModel=new ArrayList<>();
        String semanticId;
        boolean hasSemanticIdInt;
        String parsing_type;
        private String parsing_format;
        private String composition_type;
        private String semantic_position;

        public DeviceDataParser build(){

            //TODO: gestira la futura exception.. deve avere un valore solo se il parsing type è semantic
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
            List<BLEDeviceDataCluster> dataModelAux;
            //if (hasSemanticIdInt){
            if (parsFormat==BLEDataConversionComplements.FORMAT_UINT8){
                dataModelAux=sortAndPlaceDataModelByIdInt(dataModel);
            } else {
                dataModelAux=sortDataModelByEndOffset(dataModel);
            }

            if (hasSemanticIdInt){
                int parsing_id=Integer.parseInt(semanticId);
                return new DeviceDataParser(parser_id, dataModelAux,
                        ParsingComplements.getCompositionTypeIntFromString(composition_type),
                        parsing_id, hasSemanticIdInt,
                        parsType, parsFormat, startOffset, stopOffset);
            } else if (semanticId!=null){
                return new DeviceDataParser(parser_id, dataModelAux,
                        ParsingComplements.getCompositionTypeIntFromString(composition_type),
                        semanticId.charAt(0),
                        hasSemanticIdInt,
                        parsType, parsFormat, startOffset, stopOffset);
            } else {
                dataModelAux=sortDataModelByEndOffset(dataModel);
                return new DeviceDataParser(parser_id, dataModelAux,
                        ParsingComplements.getCompositionTypeIntFromString(composition_type),
                        hasSemanticIdInt,
                        parsType, parsFormat, startOffset, stopOffset);
            }

        }

        public Builder setParser_id(String parser_layout) {
            this.parser_id = parser_layout;
            return this;
        }

        public Builder setDataModel(List<BLEDeviceDataCluster> data) {
            this.dataModel = data;
            return this;
        }

        public Builder addDataModel(BLEDeviceDataCluster data) {
            this.dataModel.add(data);
            return this;
        }

        public Builder setSemanticId(String semanticId) {
            this.semanticId = semanticId;
            return this;
        }

//        public Builder setMySemanticFormat(String mySemanticFormat) {
//            this.mySemanticFormat = mySemanticFormat;
//            return this;
//        }

        public Builder setParsing_type(String parsing_type) {
            this.parsing_type = parsing_type;
            return this;
        }

        public Builder setParsing_format(String parsing_format) {
            this.parsing_format = parsing_format;
            return this;
        }

        public Builder setHasSemanticIdInt(boolean hasSemanticIdInt) {
            this.hasSemanticIdInt = hasSemanticIdInt;
            return this;
        }

        public Builder setComposition_type(String composition_type) {
            this.composition_type = composition_type;
            return this;
        }

        private List<BLEDeviceDataCluster> sortAndPlaceDataModelByIdInt
                (List<BLEDeviceDataCluster> bleDeviceDataClusters){
            List<BLEDeviceDataCluster> dataModelAux=new ArrayListAnySize();
            for (BLEDeviceDataCluster bleDeviceDataCluster:bleDeviceDataClusters){
                dataModelAux.add(bleDeviceDataCluster.getSemanticIdInt(), bleDeviceDataCluster);
            }
            return dataModelAux;
        }

        private List<BLEDeviceDataCluster> sortDataModelByEndOffset
                (List<BLEDeviceDataCluster> bleDeviceDataClusters){
            List<BLEDeviceDataCluster> dataModelAux=new ArrayList<>();
            List<BLEDeviceDataCluster> dataModelSupport=new ArrayList<>(bleDeviceDataClusters);
            int[] arrayEndOffset=new int[dataModelSupport.size()];
            for (int i=0;i<dataModelSupport.size();i++)
                arrayEndOffset[i]=dataModelSupport.get(i).getLength();
            Arrays.sort(arrayEndOffset);
            for (int i=0; i<arrayEndOffset.length;i++){
                for (int j=0;j<dataModelSupport.size();j++){
                    if (dataModelSupport.get(j).getLength()==arrayEndOffset[i]){
                        dataModelAux.add(dataModelSupport.get(j));
                        dataModelSupport.remove(j);
                        continue;
                    }
                }
            }
            return dataModelAux;
        }

        public Builder setSemantic_position(String semantic_position) {
            this.semantic_position = semantic_position;
            return this;
        }
    }

}
