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
import java.util.List;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.complements.ArrayListAnySize;
import it.unibs.sandroide.lib.data.BLEDataHandlingComplements;
import it.unibs.sandroide.lib.data.BLEDeviceData;
import it.unibs.sandroide.lib.data.ParsingComplements;

/**
 * Class for handling the {@link BLEDeviceData}
 */
public class BLEDeviceDataCluster {
    private final static String TAG = "BLEDeviceDataCluster";

    private List<BLEDeviceData> bleDeviceDataList;
    private List<BLEDeviceData> determinantBleDeviceDataList;
    private List<List<BLEDeviceData>> dependentBleDeviceDataList;
    private String data_type;
    private String id;
    private String data_handling;
    private float data_handlingValue;
    private BLEDeviceDataListener bleDeviceDataListener;
    private int semanticIdInt;
    private char semanticIdChar;
    private boolean hasSemanticIdInt;
    private int length;
    private boolean hasDataDependency;

    /**
     * Constructor
     * @param data_type define the typeof the data(optional)
     * @param id identifier of the {@link BLEDeviceDataCluster}
     * @param data_handling how to handle the incoming data (e.g. value: takes the value as it is;
     *                      onset: trigger the data when the value change from 0 to 1)
     * @param data_handlingValue value used for same data handling (e.g. onset data handling is triggered
     *                           when the value is set equal to data_handlingValue)
     * @param bleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     * @param determinantBleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                     which affect the handling of dependentBleDeviceData
     * @param dependentBleDeviceDataList list of list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                   whose handling is affected by the value of determinantBleDeviceData. The first
     *                                   list level is sorted to link the list of dependentBleDeviceData with
     *                                   the related determinantBleDeviceData
     * @param semanticIdInt if the data handling is semantic (is defined the {@link DeviceDataParser} owner
     *                         of the {@link BLEDeviceDataCluster}) this value identifies if use this
     *                         {@link BLEDeviceDataCluster} to parse the message
     * @param hasSemanticIdInt true if the semanticId is in uint8 format
     * @param length the length in bytes of the {@link BLEDeviceDataCluster} in the message
     */
    BLEDeviceDataCluster
            (String data_type, String id, String data_handling, float data_handlingValue,
             List<BLEDeviceData> bleDeviceDataList, List<BLEDeviceData> determinantBleDeviceDataList,
             List<List<BLEDeviceData>> dependentBleDeviceDataList, int semanticIdInt, boolean hasSemanticIdInt,
             int length){

        this.data_type=data_type;
        this.id=id;
        this.data_handling=data_handling;
        this.data_handlingValue=data_handlingValue;
        this.bleDeviceDataList=bleDeviceDataList;
        this.determinantBleDeviceDataList=determinantBleDeviceDataList;
        this.dependentBleDeviceDataList=dependentBleDeviceDataList;
        this.semanticIdInt=semanticIdInt;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.length=length;
        if ((determinantBleDeviceDataList==null)||(dependentBleDeviceDataList==null))
            hasDataDependency=false;
        else
            hasDataDependency=true;
    }

    /**
     * Constructor
     * @param data_type define the typeof the data(optional)
     * @param id identifier of the {@link BLEDeviceDataCluster}
     * @param data_handling how to handle the incoming data (e.g. value: takes the value as it is;
     *                      onset: trigger the data when the value change from 0 to 1)
     * @param data_handlingValue value used for same data handling (e.g. onset data handling is triggered
     *                           when the value is set equal to data_handlingValue)
     * @param bleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     * @param determinantBleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                     which affect the handling of dependentBleDeviceData
     * @param dependentBleDeviceDataList list of list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                   whose handling is affected by the value of determinantBleDeviceData. The first
     *                                   list level is sorted to link the list of dependentBleDeviceData with
     *                                   the related determinantBleDeviceData
     * @param semanticIdChar if the data handling is semantic (is defined the {@link DeviceDataParser} owner
     *                         of the {@link BLEDeviceDataCluster}) this value identifies if use this
     *                         {@link BLEDeviceDataCluster} to parse the message
     * @param hasSemanticIdInt true if the semanticId is in uint8 format
     * @param length the length in bytes of the {@link BLEDeviceDataCluster} in the message
     */
    BLEDeviceDataCluster
            (String data_type, String id, String data_handling, float data_handlingValue,
             List<BLEDeviceData> bleDeviceDataList,  List<BLEDeviceData> determinantBleDeviceDataList,
             List<List<BLEDeviceData>> dependentBleDeviceDataList, char semanticIdChar, boolean hasSemanticIdInt,
             int length){

        this.data_type=data_type;
        this.id=id;
        this.data_handling=data_handling;
        this.data_handlingValue=data_handlingValue;
        this.bleDeviceDataList=bleDeviceDataList;
        this.determinantBleDeviceDataList=determinantBleDeviceDataList;
        this.dependentBleDeviceDataList=dependentBleDeviceDataList;
        this.semanticIdChar=semanticIdChar;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.length=length;
        if ((determinantBleDeviceDataList==null)||(dependentBleDeviceDataList==null))
            hasDataDependency=false;
        else
            hasDataDependency=true;
    }

    /**
     * Constructor
     * @param data_type define the typeof the data(optional)
     * @param id identifier of the {@link BLEDeviceDataCluster}
     * @param data_handling how to handle the incoming data (e.g. value: takes the value as it is;
     *                      onset: trigger the data when the value change from 0 to 1)
     * @param data_handlingValue value used for same data handling (e.g. onset data handling is triggered
     *                           when the value is set equal to data_handlingValue)
     * @param bleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     * @param determinantBleDeviceDataList list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                     which affect the handling of dependentBleDeviceData
     * @param dependentBleDeviceDataList list of list if {@link BLEDeviceData} owned by the {@link BLEDeviceDataCluster}
     *                                   whose handling is affected by the value of determinantBleDeviceData. The first
     *                                   list level is sorted to link the list of dependentBleDeviceData with
     *                                   the related determinantBleDeviceData
     * @param hasSemanticIdInt true if the semanticId is in uint8 format
     * @param length the length in bytes of the {@link BLEDeviceDataCluster} in the message
     */
    BLEDeviceDataCluster
            (String data_type, String id, String data_handling, float data_handlingValue,
             List<BLEDeviceData> bleDeviceDataList,  List<BLEDeviceData> determinantBleDeviceDataList,
             List<List<BLEDeviceData>> dependentBleDeviceDataList, boolean hasSemanticIdInt,
             int length){

        this.data_type=data_type;
        this.id=id;
        this.data_handling=data_handling;
        this.data_handlingValue=data_handlingValue;
        this.bleDeviceDataList=bleDeviceDataList;
        this.determinantBleDeviceDataList=determinantBleDeviceDataList;
        this.dependentBleDeviceDataList=dependentBleDeviceDataList;
        this.hasSemanticIdInt=hasSemanticIdInt;
        this.length=length;
        if ((determinantBleDeviceDataList==null)||(dependentBleDeviceDataList==null))
            hasDataDependency=false;
        else
            hasDataDependency=true;
    }

    public List<BLEDeviceData> getBleDeviceDataList() {
        return bleDeviceDataList;
    }

    public String getData_type() {
        return data_type;
    }

    public String getId() {
        return id;
    }

    public void setBleDeviceDataListener(BLEDeviceDataListener bleDeviceDataListener){
        this.bleDeviceDataListener=bleDeviceDataListener;
    }

    /**
     * triggered when the {@link BLEDeviceData}s of the {@link BLEDeviceDataCluster} are read
     */
    public void triggerBleDeviceDataListener(){
        //TODO: insert enable if update enables the triggering
        Log.d(TAG, "bledevdatalis: "+bleDeviceDataListener);
        if (bleDeviceDataListener!=null)
            bleDeviceDataListener.onBLEDeviceDataRead(this);
    }

    public int getSemanticIdInt() {
        return semanticIdInt;
    }

    public char getSemanticIdChar() {
        return semanticIdChar;
    }

    /**
     * @return the length of the {@link BLEDeviceDataCluster}. Length is considered as the maximum stopOffset index
     * among the cluster's data +1.
     */
    //TODO: to grant variable length for dependentData Cluster length should be retrieved dynamically by readDataCluster
    public int getLength() {
        return length;
    }

    /**
     * Read the incoming message starting from the offset. The method handles the determinant
     * dependent issue
     * @param bytes the message to read
     * @param absolute_offset the offset to add at the {@link BLEDeviceDataCluster} indexes
     */
    public void readDataCluster(byte[] bytes, int absolute_offset){
        Log.d(TAG, "__Dcluster: "+this);
        if (hasDataDependency){
            for(int i=0;i<determinantBleDeviceDataList.size();i++){
                Log.d(TAG, "__DdetData: "+determinantBleDeviceDataList.get(i).getId()+
                        ", abs: "+absolute_offset);
                determinantBleDeviceDataList.get(i).setValue
                        (bytes, absolute_offset);
                //if (dependentBleDeviceDataList.size()>=i+1){
                if (dependentBleDeviceDataList.size()>i){
                    if (dependentBleDeviceDataList.get(i)!=null){
                        Object key= BLEDataHandlingComplements.getHandlerKey
                                (determinantBleDeviceDataList.get(i).getValue(),
                                        determinantBleDeviceDataList.get(i).getFormat());
                        for (BLEDeviceData bleDeviceData:dependentBleDeviceDataList.get(i)){
                            Log.d(TAG, "__DdepData: "+bleDeviceData);
                            Log.d(TAG, "__DdetData: "+bleDeviceData.getId()+
                                    ", abs: "+absolute_offset);
                            bleDeviceData.setValue
                                    (bytes, absolute_offset, key);
                        }
                    }
                }
            }
        } else{
            for(int i=0;i<bleDeviceDataList.size();i++){
                Log.d(TAG, "__DData: "+bleDeviceDataList.get(i));
                bleDeviceDataList.get(i).setValue
                        (bytes, absolute_offset);
            }
        }
        for(int i=0;i<bleDeviceDataList.size();i++){
            Log.d(TAG, "__DData: "+bleDeviceDataList.get(i));
        }
    }

    /**
     * Clones the {@link BLEDeviceDataCluster}
     */
    public BLEDeviceDataCluster clone(){
        List<BLEDeviceData> bleDeviceDataList= new ArrayList<>();
        for (int i=0;i<this.bleDeviceDataList.size();i++){
            bleDeviceDataList.add(this.bleDeviceDataList.get(i).clone());
        }

        /////Determinant and dependent Lists
        List<BLEDeviceData> determinantBleDeviceDataList= new ArrayList<>();
        List<BLEDeviceData> notDeterminantBleDeviceDataList=new ArrayList<>();
        ArrayListAnySize<List<BLEDeviceData>> dependentBleDeviceDataList= new ArrayListAnySize<>();

        Log.d(TAG, "Cluster: "+id);

        for (int i=0;i<bleDeviceDataList.size();i++){
            Log.d(TAG, "data: "+bleDeviceDataList.get(i).getId()+", map: "+bleDeviceDataList.get(i).dataHandlerHashMap);
            if(bleDeviceDataList.get(i).getDependency_id()==null)
                determinantBleDeviceDataList.add(bleDeviceDataList.get(i));
            else
                notDeterminantBleDeviceDataList.add(bleDeviceDataList.get(i));
        }

        if (determinantBleDeviceDataList.size()==bleDeviceDataList.size()){
            determinantBleDeviceDataList=null;
            dependentBleDeviceDataList=null;
        } else{

            for (int i=0;i<notDeterminantBleDeviceDataList.size();i++){
                for (int u=0; u<determinantBleDeviceDataList.size();u++){
                    if (notDeterminantBleDeviceDataList.get(i).getDependency_id()
                            .equals(determinantBleDeviceDataList.get(u).getId())){
                        if (dependentBleDeviceDataList.size()<u+1){
                            List<BLEDeviceData> list=new ArrayList<>();
                            list.add(notDeterminantBleDeviceDataList.get(i));
                            dependentBleDeviceDataList.add(u, list);
                        } else{
                            dependentBleDeviceDataList.get(u)
                                    .add(notDeterminantBleDeviceDataList.get(i));
                        }
                    }
                }
            }
        }
        /////

        if (hasSemanticIdInt)
            return new BLEDeviceDataCluster(data_type, id, data_handling,
                data_handlingValue, bleDeviceDataList, determinantBleDeviceDataList,
                    dependentBleDeviceDataList, semanticIdInt, hasSemanticIdInt,length);
        else
            return new BLEDeviceDataCluster(data_type, id, data_handling,
                    data_handlingValue, bleDeviceDataList, determinantBleDeviceDataList,
                    dependentBleDeviceDataList, semanticIdChar, hasSemanticIdInt, length);
    }

    public static class Builder{

        List<BLEDeviceData> bleDeviceDataList=new ArrayList<>();
        //List<String> determinantDeviceData=new ArrayList<>();
        String data_type;
        String id;
        String data_handling;
        float data_handlingValue;
        String semanticId;
        boolean hasSemanticIdInt;

        public BLEDeviceDataCluster build(){



            List<BLEDeviceData> determinantBleDeviceDataList= new ArrayList<>();
            List<BLEDeviceData> notDeterminantBleDeviceDataList=new ArrayList<>();
            ArrayListAnySize<List<BLEDeviceData>> dependentBleDeviceDataList= new ArrayListAnySize<>();

            Log.d(TAG, "Cluster: "+id);

            for (int i=0;i<this.bleDeviceDataList.size();i++){
                Log.d(TAG, "data: "+bleDeviceDataList.get(i).getId()+", map: "+bleDeviceDataList.get(i).dataHandlerHashMap);
                if(bleDeviceDataList.get(i).getDependency_id()==null)
                    determinantBleDeviceDataList.add(bleDeviceDataList.get(i));
                else
                    notDeterminantBleDeviceDataList.add(bleDeviceDataList.get(i));
            }

            if (determinantBleDeviceDataList.size()==bleDeviceDataList.size()){
                determinantBleDeviceDataList=null;
                dependentBleDeviceDataList=null;
            } else{

                for (int i=0;i<notDeterminantBleDeviceDataList.size();i++){
                    for (int u=0; u<determinantBleDeviceDataList.size();u++){
                        if (notDeterminantBleDeviceDataList.get(i).getDependency_id()
                                .equals(determinantBleDeviceDataList.get(u).getId())){
                            if (dependentBleDeviceDataList.size()<u+1){
                                List<BLEDeviceData> list=new ArrayList<>();
                                list.add(notDeterminantBleDeviceDataList.get(i));
                                dependentBleDeviceDataList.add(u, list);
                            } else{
                                dependentBleDeviceDataList.get(u)
                                        .add(notDeterminantBleDeviceDataList.get(i));
                            }
                        }
                    }
                }
            }





            //int minStartOffset=Integer.MAX_VALUE;
            int maxStopOffset=0;

            for(BLEDeviceData bleDeviceData:bleDeviceDataList){
//                if (minStartOffset>bleDeviceData.getStartOffset())
//                    minStartOffset=bleDeviceData.getStartOffset();
                if (maxStopOffset<bleDeviceData.getStopOffset())
                    maxStopOffset=bleDeviceData.getStopOffset();
            }

//            int length=maxStopOffset-minStartOffset+1;
            int length=maxStopOffset+1;

            if (hasSemanticIdInt){
                int parsing_id=Integer.parseInt(semanticId);
                return new BLEDeviceDataCluster
                        (data_type, id, data_handling, data_handlingValue, bleDeviceDataList,
                                determinantBleDeviceDataList, dependentBleDeviceDataList,
                                parsing_id, hasSemanticIdInt, length);
            } else if (semanticId!=null){
                return new BLEDeviceDataCluster
                        (data_type, id, data_handling, data_handlingValue, bleDeviceDataList,
                                determinantBleDeviceDataList, dependentBleDeviceDataList,
                                semanticId.charAt(0), hasSemanticIdInt, length);
            } else{
                return new BLEDeviceDataCluster
                        (data_type, id, data_handling, data_handlingValue, bleDeviceDataList,
                                determinantBleDeviceDataList, dependentBleDeviceDataList,
                                hasSemanticIdInt, length);
            }
        }

        public Builder setBleDeviceDataList(List<BLEDeviceData> bleDeviceDataList) {
            this.bleDeviceDataList = bleDeviceDataList;
            return this;
        }

        public Builder addBleDeviceData(BLEDeviceData bleDeviceData) {
            this.bleDeviceDataList.add(bleDeviceData);
            return this;
        }

        public Builder setData_type(String data_type) {
            this.data_type = data_type;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setData_handling(String data_handling) {
            this.data_handling = data_handling;
            return this;
        }

        public Builder setData_handlingValue(float data_handlingValue) {
            this.data_handlingValue = data_handlingValue;
            return this;
        }

        public Builder setSemanticId(String semanticId) {
            this.semanticId = semanticId;
            return this;
        }

        public Builder setHasSemanticIdInt(boolean hasSemanticIdInt) {
            this.hasSemanticIdInt = hasSemanticIdInt;
            return this;
        }

        /**
         * Clone a BLEDeviceDataCluster. All the parameter are copied, but the id and the semantic_id
         *
         * @param model
         *        a BLEDeviceDataCluster object used to clone the new BLEDeviceDataCluster
         *
         *
         */
        public Builder setParameterFromModel(BLEDeviceDataCluster model){
            bleDeviceDataList=model.bleDeviceDataList;
            data_type=model.data_type;
            data_handling=model.data_handling;
            data_handlingValue=model.data_handlingValue;
            hasSemanticIdInt=model.hasSemanticIdInt;
            return this;
        }
    }

    //region parser of ClusterModel
    public static List<BLEDeviceDataCluster> parse(Context context) {

        final int NONE_PARSING = 0;
        final int FL_DATACLUSTER_MODEL_PARSING = 2;
        final int SL_SUB_DATA_PARSING = 4;
        final int SL_DATA_HANDLING_PARSING = 5;
        final int TL_SENSOR_PARSING = 6;
        final int TL_DATA_HANDLING_PARSING = 7;
        final int TL_DATA_SPECIAL_PARSING = 8;
        final int TL_BIT_LOGIC_PARSING = 9;
        final int TL_DEPENDENCY_PARSING = 10;
        final int FOL_SUBCONFIG_PARSING = 11;
        final int FIL_DATA_HANDLING_PARSING = 12;
        final int FIL_DATA_SPECIAL_PARSING = 13;
        final int FIL_BIT_LOGIC_PARSING = 14;


        List<BLEDeviceDataCluster> bleDeviceDataClusters = new ArrayList<>();
        BLEDeviceDataCluster.Builder dataClusterBuilder = new BLEDeviceDataCluster.Builder();
        BLEDeviceData.Builder subDataBuilder = new BLEDeviceData.Builder();
        BLEDeviceData.DataHandlerBuilder dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();

        String text="";

        File file = new File(Environment.getExternalStorageDirectory(),"bledataclustermodels.xml");

        InputStream is= null;
        int parsingFirstLayerSection=NONE_PARSING;
        int parsingSecondLayerSection=NONE_PARSING;
        int parsingThirdLayerSection=NONE_PARSING;
        int parsingFourthLayerSection=NONE_PARSING;
        int parsingFifthLayerSection=NONE_PARSING;


        try {
            if (BLEContext.isPermissionsGranted()) {
                is = new FileInputStream(file);
                Log.d(TAG, is.toString());

                Log.v("WriteFile", "file created");

                XmlPullParserFactory factory = null;
                XmlPullParser parser = null;

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
                            if (parsingFirstLayerSection == NONE_PARSING) {
                                if (tagname.equalsIgnoreCase("dataClusterModel")) {
                                    dataClusterBuilder = new BLEDeviceDataCluster.Builder();
                                    parsingFirstLayerSection = FL_DATACLUSTER_MODEL_PARSING;
                                }
                            } else if (parsingFirstLayerSection == FL_DATACLUSTER_MODEL_PARSING) {
                                if (parsingSecondLayerSection == NONE_PARSING) {
                                    if (tagname.equalsIgnoreCase("data_handling")) {
                                        parsingSecondLayerSection = SL_DATA_HANDLING_PARSING;
                                    } else if (tagname.equalsIgnoreCase("subdata")) {
                                        dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();
                                        dataHandlerBuilder.setIsInput(true);
                                        subDataBuilder = new BLEDeviceData.Builder();
                                        parsingSecondLayerSection = SL_SUB_DATA_PARSING;
                                    }
                                } else if (parsingSecondLayerSection == SL_SUB_DATA_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("sensor")) {
                                            subDataBuilder.setData_type(ParsingComplements.DT_SENSOR_STRING);
                                            parsingThirdLayerSection = TL_SENSOR_PARSING;
                                        } else if (tagname.equalsIgnoreCase("data_handling")) {
                                            parsingThirdLayerSection = TL_DATA_HANDLING_PARSING;
                                        } else if (tagname.equalsIgnoreCase("special")) {
                                            parsingThirdLayerSection = TL_DATA_SPECIAL_PARSING;
                                        } else if (tagname.equalsIgnoreCase("bitlogic")) {
                                            parsingThirdLayerSection = TL_BIT_LOGIC_PARSING;
                                        } else if (tagname.equalsIgnoreCase("dependency")) {
                                            parsingThirdLayerSection = TL_DEPENDENCY_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection == TL_DEPENDENCY_PARSING) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("subconfig")) {
                                                dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();
                                                dataHandlerBuilder.setIsInput(true);
                                                parsingFourthLayerSection = FOL_SUBCONFIG_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection == FOL_SUBCONFIG_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("data_handling")) {
                                                    parsingFifthLayerSection = FIL_DATA_HANDLING_PARSING;
                                                } else if (tagname.equalsIgnoreCase("special")) {
                                                    parsingFifthLayerSection = FIL_DATA_SPECIAL_PARSING;
                                                } else if (tagname.equalsIgnoreCase("bitlogic")) {
                                                    parsingFifthLayerSection = FIL_BIT_LOGIC_PARSING;
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
                            if (parsingFirstLayerSection == FL_DATACLUSTER_MODEL_PARSING) {
                                if (parsingSecondLayerSection == NONE_PARSING) {
                                    if (tagname.equalsIgnoreCase("dataClusterModel")) {
                                        bleDeviceDataClusters.add(dataClusterBuilder.build());
                                        parsingFirstLayerSection = NONE_PARSING;
                                    }
                                    if (text != null) {
                                        if (tagname.equalsIgnoreCase("type")) {
                                            dataClusterBuilder.setData_type(text);
                                        } else if (tagname.equalsIgnoreCase("id")) {
                                            dataClusterBuilder.setId(text);
                                        }
                                    }
                                } else if (parsingSecondLayerSection == SL_DATA_HANDLING_PARSING) {
                                    if (tagname.equalsIgnoreCase("data_handling")) {
                                        parsingSecondLayerSection = NONE_PARSING;
                                    } else if (tagname.equalsIgnoreCase("type")) {
                                        dataClusterBuilder.setData_handling(text);
                                    } else if (tagname.equalsIgnoreCase("value")) {
                                        float value = Float.parseFloat(text);
                                        dataClusterBuilder.setData_handlingValue(value);
                                    }
                                } else if (parsingSecondLayerSection == SL_SUB_DATA_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("subdata")) {
                                            //if dependency_id==null data handler is defined directly in the subdata section...
                                            // need to insert position indication for the hidden handler
                                            if (subDataBuilder.dependency_id == null) {
                                                dataHandlerBuilder.setPosition(subDataBuilder.position);
                                                subDataBuilder.addDataHandlerBuilder(dataHandlerBuilder);
                                            }
    //                                        Log.d(TAG, "id subdata ThirdLayer, data: "+subDataBuilder.id);
    //                                        if (subDataBuilder.id.equals("value"));
    //                                            Log.d(TAG, "data pos: "+subDataBuilder.position);
                                            dataClusterBuilder.addBleDeviceData(subDataBuilder.build());
                                            parsingSecondLayerSection = NONE_PARSING;
                                        }
                                        if (text != null) {
                                            if (tagname.equalsIgnoreCase("position")) {
                                                subDataBuilder.setPosition(text);
                                                Log.d(TAG, "data pos is: " + text);
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
                                            } else if (tagname.equalsIgnoreCase("bytelogic")) {
                                                dataHandlerBuilder.setByteLogic(text);
                                            }
                                        }
                                    } else if (parsingThirdLayerSection == TL_DATA_HANDLING_PARSING) {
                                        if (tagname.equalsIgnoreCase("data_handling")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        } else if (tagname.equalsIgnoreCase("type")) {
                                            dataHandlerBuilder.setHandle_type(text);
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            dataHandlerBuilder.setHandle_value(text);
                                        }
                                    } else if (parsingThirdLayerSection == TL_SENSOR_PARSING) {
                                        if (tagname.equalsIgnoreCase("type")) {
                                            subDataBuilder.setSensor_type(text);
                                            Log.d(TAG, "type sens is: " + text);
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
                                        } else if (tagname.equalsIgnoreCase("sensor")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection == TL_DATA_SPECIAL_PARSING) {
                                        if (tagname.equalsIgnoreCase("special")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            dataHandlerBuilder.addSpecial(text);
                                        }
                                    } else if (parsingThirdLayerSection == TL_BIT_LOGIC_PARSING) {
                                        if (tagname.equalsIgnoreCase("bitlogic")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        } else if (tagname.equalsIgnoreCase("operation")) {
                                            dataHandlerBuilder.setLogicOperation(text);
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            dataHandlerBuilder.setLogicOperationValue(text);
                                        }
                                    }
                                    //////////////
                                    else if (parsingThirdLayerSection == TL_DEPENDENCY_PARSING) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("dependency")) {
                                                parsingThirdLayerSection = NONE_PARSING;
                                            } else if (tagname.equalsIgnoreCase("id")) {
                                                subDataBuilder.setDependency_id(text);
                                            } else if (tagname.equalsIgnoreCase("dependencyformat")) {
                                                subDataBuilder.setKey_format(text);
                                            }
                                        } else if (parsingFourthLayerSection == FOL_SUBCONFIG_PARSING) {
                                            /////////////////
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("subconfig")) {
                                                    subDataBuilder.addDataHandlerBuilder(dataHandlerBuilder);
                                                    parsingFourthLayerSection = NONE_PARSING;
                                                }
                                                if (text != null) {
                                                    if (tagname.equalsIgnoreCase("type")) {
                                                        dataHandlerBuilder.setData_type(text);
                                                    } else if (tagname.equalsIgnoreCase("format")) {
                                                        dataHandlerBuilder.setFormat(text);
                                                    } else if (tagname.equalsIgnoreCase("intercept")) {
                                                        dataHandlerBuilder.setIntercept(text);
                                                    } else if (tagname.equalsIgnoreCase("slope")) {
                                                        dataHandlerBuilder.setSlope(text);
                                                    } else if (tagname.equalsIgnoreCase("dependencyvalue")) {
                                                        dataHandlerBuilder.setKey_value(text);
                                                    } else if (tagname.equalsIgnoreCase("position")) {
                                                        dataHandlerBuilder.setPosition(text);
                                                    } else if (tagname.equalsIgnoreCase("bytelogic")) {
                                                        dataHandlerBuilder.setByteLogic(text);
                                                    }
                                                }
                                            } else if (parsingFifthLayerSection == FIL_DATA_HANDLING_PARSING) {
                                                if (tagname.equalsIgnoreCase("data_handling")) {
                                                    parsingFifthLayerSection = NONE_PARSING;
                                                } else if (tagname.equalsIgnoreCase("type")) {
                                                    dataHandlerBuilder.setHandle_type(text);
                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                    dataHandlerBuilder.setHandle_value(text);
                                                }
                                            } else if (parsingFifthLayerSection == FIL_DATA_SPECIAL_PARSING) {
                                                if (tagname.equalsIgnoreCase("special")) {
                                                    parsingFifthLayerSection = NONE_PARSING;
                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                    dataHandlerBuilder.addSpecial(text);
                                                }
                                            } else if (parsingFifthLayerSection == FIL_BIT_LOGIC_PARSING) {
                                                if (tagname.equalsIgnoreCase("bitlogic")) {
                                                    parsingFifthLayerSection = NONE_PARSING;
                                                } else if (tagname.equalsIgnoreCase("operation")) {
                                                    dataHandlerBuilder.setLogicOperation(text);
                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                    dataHandlerBuilder.setLogicOperationValue(text);
                                                }
                                            }
                                            /////////////////
                                        }
                                    }
                                }
                            }
                            text = null;
                            break;

                        default:
                            break;
                    }
                    eventType = parser.next();
                }
            }

        } catch (FileNotFoundException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            Log.d(TAG, "Text is: "+text);
            e.printStackTrace();
        } catch (IOException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
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

        return bleDeviceDataClusters;
    }
    //endregion

}
