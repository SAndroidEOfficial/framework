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
package eu.angel.bleembedded.lib.device;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.activities.SandroideBaseActivity;
import eu.angel.bleembedded.lib.data.BLEDeviceData;
import eu.angel.bleembedded.lib.device.action.BLEAction;
import eu.angel.bleembedded.lib.device.action.BLEInit;
import eu.angel.bleembedded.lib.device.action.BLEMessageModel;
import eu.angel.bleembedded.lib.device.action.BLESequenceElement;
import eu.angel.bleembedded.lib.device.action.BLEWritableCharacteristic;
import eu.angel.bleembedded.lib.device.read.BLEReadableCharacteristic;
import eu.angel.bleembedded.lib.device.read.DeviceDataParser;
import eu.angel.bleembedded.lib.device.read.DeviceDataParserCollector;

/**
 * Summarizes the descriptions of the device to make available the handling of different devices
 * using the same functions
 */
public class DevicesDescriptorNew {

    private final static String TAG= "DevicesDescriptorNew";

    //TODO: load these ones from xml
    //region generic section
    public final static String DEVICE_NAME_SERVICE_UUID_STRING = "00001800-0000-1000-8000-00805f9b34fb";
    public final static String DEVICE_NAME_ATTRIBUTE_UUID_STRING = "00002a00-0000-1000-8000-00805f9b34fb";
    public final static String CLIENT_CHARACTERISTIC_CONFIG_UUID_STRING = "00002902-0000-1000-8000-00805f9b34fb";
    public final static String SERIAL_NUMBER_UUID_STRING = "00002A25-0000-1000-8000-00805f9b34fb";


    public final static UUID DEVICE_NAME_SERVICE_UUID = UUID.fromString(DEVICE_NAME_SERVICE_UUID_STRING);
    public final static UUID DEVICE_NAME_ATTRIBUTE_UUID = UUID.fromString(DEVICE_NAME_ATTRIBUTE_UUID_STRING);
    public final static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID_STRING);
    public static final UUID SERIAL_NUMBER_UUID = UUID.fromString(SERIAL_NUMBER_UUID_STRING);


    //endregion



    private final String deviceType;
    private final String nickName;

    private final List<BLEReadableCharacteristic> bleReadableCharacteristics;
    //private final List<BLEWritableCharacteristic> bleWritableCharacteristics;

    private final List<BLEAction> bleActions;

    private final List<BLEInit> bleInits;

    //Map for string(service/characteristic name)  to UUID object
    private final HashMap<String, String> stToUUID;
    //Map for UUID   to objectstring(service/characteristic name)
    private final HashMap<String, String> UUIDTost;

    //Map of position of the Item in the RootItem (RootItem creates many Items)
    private final HashMap<String, Integer> devItemCardinality;
    //Map number of Items in RootItem (RootItem creates many Items), only if exists RootItem with more than 1 Item
    @Deprecated
    private final HashMap<String, Integer> devRawItemNumber;
    @Deprecated
    private final HashMap<String, String[]> mandatoryCharacteristicsForDevItem;
    //Map number of Items to Itemtype
    private final HashMap<String, String> devItemsToItemType;
    //Map the Items created by the RootItem (Raw) (RootItem creates many Items), only if exists RootItem with more than 1 Item
    private final HashMap<String, String[]> rawItemToItems;
    //Map the RootItem (Raw) for each Item (RootItem creates many Items), only if exists RootItem with more than 1 Item
    private final HashMap<String, String[]> itemRoots;

    //Map the Services and characteristics mandatory to retrieve each Item
    private final HashMap<String, HashMap<String, String[]>>
            mandatoryServicesCharacteristicsForDevItem;

    private final HashMap<String, LinkedHashMap <String, String[][]>>
            readableCharacteristicsAndClustersPlusGroupForDevItem;

    private final HashMap<String, String[]>
            actionsForDevItem;

    private final HashMap<String, String>
            initForDevItem;

    /**
     * Constructor of the {@link DevicesDescriptorNew} class.
     * @param deviceType type of the {@link DeviceControl}
     * @param bleReadableCharacteristics all the {@link BLEReadableCharacteristic}s exploited by
     *                                   the Item of the device
     * @param bleActions the {@link BLEAction}s exploited by all the Items of the device
     * @param bleInits the {@link BLEInit}s exploited by all the Items of the device
     * @param stToUUID HashMap which links names ({@link String}) of the services/characteristics with
     *                 the related UUIDx ({@link String})
     * @param UUIDTost HashMap which links UUIDx ({@link String}) of the services/characteristics with
     *                 the related names ({@link String})
     * @param devItemCardinality HashMAp which links the Items with their own cardinalities
     * @param devRawItemNumber not currently used
     * @param devItemsToItemType HashMAp which links the Items with their own types
     * @param rawItemToItems backward compatibility (should be removed)
     * @param itemRoots backward compatibility (should be removed)
     * @param mandatoryServicesCharacteristicsForDevItem HashMap matching the Items' names with the
     *                                                   related Services and Characteristics (names)
     * @param readableCharacteristicsAndClustersPlusGroupForDevItem HashMap matching the Items' names
     *                                                    with the related readableCharacterisitcs'names
     *                                                    matched with the group of the readableCharacteristic
     * @param actionsForDevItem HashMap matching the Items'names with their BleActions (name)
     * @param initForDevItem HashMap matching the Items'names with their BleInits (name)
     *
     */
    DevicesDescriptorNew (String deviceType, String nickName,
                          List<BLEReadableCharacteristic> bleReadableCharacteristics,
                          //List<BLEWritableCharacteristic> bleWritableCharacteristics,
                          List<BLEAction> bleActions,
                          List<BLEInit> bleInits,
                          HashMap<String, String> stToUUID,
                          HashMap<String, String> UUIDTost,
                          HashMap<String, Integer> devItemCardinality,
                          HashMap<String, Integer> devRawItemNumber,
                          HashMap<String, String[]> mandatoryCharacteristicsForDevItem,
                          HashMap<String, String> devItemsToItemType,
                          HashMap<String, String[]> rawItemToItems,
                          HashMap<String, String[]> itemRoots,
                          HashMap<String, HashMap<String, String[]>>
                                            mandatoryServicesCharacteristicsForDevItem,
                          HashMap<String, LinkedHashMap <String, String[][]>>
                                  readableCharacteristicsAndClustersPlusGroupForDevItem,
                          HashMap<String, String[]>
                                  actionsForDevItem,
                          HashMap<String, String>
                                  initForDevItem){
        this.deviceType=deviceType;
        this.nickName=nickName;
        this.bleReadableCharacteristics=bleReadableCharacteristics;
        //this.bleWritableCharacteristics=bleWritableCharacteristics;
        this.bleActions=bleActions;
        this.bleInits=bleInits;
        this.stToUUID=stToUUID;
        this.UUIDTost=UUIDTost;
        this.devItemCardinality=devItemCardinality;
        this.devRawItemNumber=devRawItemNumber;
        this.mandatoryCharacteristicsForDevItem =mandatoryCharacteristicsForDevItem;
        this.devItemsToItemType=devItemsToItemType;
        this.rawItemToItems=rawItemToItems;
        this.itemRoots=itemRoots;
        this.mandatoryServicesCharacteristicsForDevItem =mandatoryServicesCharacteristicsForDevItem;
        this.readableCharacteristicsAndClustersPlusGroupForDevItem = readableCharacteristicsAndClustersPlusGroupForDevItem;
        this.actionsForDevItem=actionsForDevItem;
        this.initForDevItem=initForDevItem;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getNickName() {
        return nickName;
    }

    public List<BLEReadableCharacteristic> getBleReadableCharacteristics() {
        return bleReadableCharacteristics;
    }

    public HashMap<String, String> getStToUUID() {
        return stToUUID;
    }

    public HashMap<String, String> getUUIDTost() {
        return UUIDTost;
    }

    public HashMap<String, Integer> getDevItemCardinality() {
        return devItemCardinality;
    }

    public HashMap<String, Integer> getDevRawItemNumber() {
        return devRawItemNumber;
    }

    public HashMap<String, String[]> getMandatoryCharacteristicsForDevItem() {
        return mandatoryCharacteristicsForDevItem;
    }

    public HashMap<String, String> getDevItemsToItemType() {
        return devItemsToItemType;
    }

    public HashMap<String, String[]> getRawItemToItems() {
        return rawItemToItems;
    }

    public HashMap<String, String[]> getItemRoots() {
        return itemRoots;
    }

    public HashMap<String, HashMap<String, String[]>> getMandatoryServicesCharacteristicsForDevItem() {
        return mandatoryServicesCharacteristicsForDevItem;
    }

    public HashMap<String, LinkedHashMap <String, String[][]>> getReadableCharacteristicsAndClustersPlusGroupForDevItem() {
        return readableCharacteristicsAndClustersPlusGroupForDevItem;
    }

    /**
     * Clones the {@link BLEReadableCharacteristic}s of the {@link DevicesDescriptorNew}. I this way
     * each device exclusively owns their {@link BLEReadableCharacteristic}
     */
    public List<BLEReadableCharacteristic> cloneReadableCharacteristic(){
        List<BLEReadableCharacteristic> bleReadableCharacteristics=new ArrayList<>();
        for (BLEReadableCharacteristic bleReadableCharacteristic:this.bleReadableCharacteristics)
            bleReadableCharacteristics.add(bleReadableCharacteristic.clone());
        return bleReadableCharacteristics;
    }

    /**
     * Clones the {@link BLEAction}s of the {@link DevicesDescriptorNew}. I this way
     * each device exclusively owns their {@link BLEAction}
     */
    public List<BLEAction> cloneBleActions(){
        List<BLEAction> bleActions=new ArrayList<>();
        for (BLEAction bleAction:this.bleActions)
            bleActions.add(bleAction.clone());
        return bleActions;
    }

    public HashMap<String, String[]> getActionsForDevItem() {
        return actionsForDevItem;
    }

    /**
     * Clones the {@link BLEInit}s of the {@link DevicesDescriptorNew}. I this way
     * each device exclusively owns their {@link BLEInit}
     */
    public List<BLEInit> cloneBleInits(){
        List<BLEInit> bleInits=new ArrayList<>();
        for (BLEInit bleInit:this.bleInits)
            bleInits.add(bleInit.clone());
        return bleInits;
    }

    public HashMap<String, String> getInitForDevItem() {
        return initForDevItem;
    }

    //    public List<BLEWritableCharacteristic> cloneWritableCharacteristic(){
//        List<BLEWritableCharacteristic> bleWritableCharacteristics=new ArrayList<>();
//        for (BLEWritableCharacteristic bleWritableCharacteristic:this.bleWritableCharacteristics)
//            bleWritableCharacteristics.add(bleWritableCharacteristic.clone());
//        return bleWritableCharacteristics;
//    }

    public static class Builder{

        private  String deviceType;
        private  String nickName;

        private List<BLEReadableCharacteristic> bleReadableCharacteristics=new ArrayList<>();
        //private List<BLEWritableCharacteristic> bleWritableCharacteristics= new ArrayList<>();

        private List<BLEAction> bleActions=new ArrayList<>();
        List<BLEInit> bleInits=new ArrayList<>();


        //keyfob section
        //Map for string(service/characteristic name)  to UUID object
        private HashMap<String, String> stToUUID = new HashMap<>();
        //Map for UUID   to objectstring(service/characteristic name)
        private HashMap<String, String> UUIDTost = new HashMap<>();

        //Map of position of the Item in the RootItem (RootItem creates many Items)
        private HashMap<String, Integer> devItemCardinality = new HashMap<>();
        //Map number of Items in RootItem (RootItem creates many Items), only if exists RootItem with more than 1 Item
        private HashMap<String, Integer> devRawItemNumber = new HashMap<>();
        @Deprecated
        private HashMap<String, String[]> mandatoryCharacteristicsForDevItem = new HashMap<>();
        //Map number of Items to Itemtype
        private HashMap<String, String> devItemsToItemType = new HashMap<>();
        //Map the Items created by the RootItem (Raw) (RootItem creates many Items), only if exists RootItem with more than 1 Item
        private HashMap<String, String[]> rawItemToItems = new HashMap<>();
        //Map the RootItem (Raw) for each Item (RootItem creates many Items), only if exists RootItem with more than 1 Item
        private HashMap<String, String[]> itemRoots = new HashMap<>();

        //Map the Services and characteristics mandatory to retrieve each Item
        private HashMap<String, HashMap<String, String[]>>
                mandatoryServicesCharacteristicsForDevItem = new HashMap<>();

        private HashMap<String, LinkedHashMap<String, String[][]>>
                readableCharacteristicsAndClustersPlusGroupForDevItem = new HashMap<>();

        private HashMap<String, String[]>
                actionsForDevItem = new HashMap<>();

        private HashMap<String, String>
                initForDevItem = new HashMap<>();


        public DevicesDescriptorNew build() {
//            String[] aux=new String[readableUUIDs.size()];
//            for (int i=0;i<readableUUIDs.size();i++)
//                aux[i]=readableUUIDs.get(i);
            return new DevicesDescriptorNew(deviceType, nickName,
                    bleReadableCharacteristics, bleActions, bleInits, stToUUID, UUIDTost,
                    devItemCardinality, devRawItemNumber, mandatoryCharacteristicsForDevItem,
                    devItemsToItemType, rawItemToItems, itemRoots,
                    mandatoryServicesCharacteristicsForDevItem,
                    readableCharacteristicsAndClustersPlusGroupForDevItem,
                    actionsForDevItem, initForDevItem);
        }

        public Builder setDeviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder setNickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder addBleReadableCharacteristic
                (BLEReadableCharacteristic bleReadableCharacteristic){
            this.bleReadableCharacteristics.add(bleReadableCharacteristic);
            return this;
        }
//
//        public Builder addBleWritableCharacteristic
//                (BLEWritableCharacteristic bleWritableCharacteristic){
//            this.bleWritableCharacteristics.add(bleWritableCharacteristic);
//            return this;
//        }

//        public Builder setBleWritableCharacteristics(List<BLEWritableCharacteristic> bleWritableCharacteristics) {
//            this.bleWritableCharacteristics = bleWritableCharacteristics;
//            return this;
//        }


        public Builder setBleActions(List<BLEAction> bleActions) {
            this.bleActions = bleActions;
            return this;
        }

        public Builder addBleAction(BLEAction bleAction) {
            this.bleActions.add(bleAction);
            return this;
        }

        public Builder setBleInits(List<BLEInit> bleInits) {
            this.bleInits = bleInits;
            return this;
        }

        public Builder addBleInit(BLEInit bleInit) {
            this.bleInits.add(bleInit);
            return this;
        }

        public Builder putStToUUID(String name, String uuid) {
            this.stToUUID.put(name, uuid);
            this.UUIDTost.put(uuid, name);
            return this;
        }

        public Builder putDevItemCardinality(String item, int cardinal) {
            this.devItemCardinality.put(item, cardinal);
            return this;
        }

        public Builder putDevRawItemNumber(String rawItem, int number){
            devRawItemNumber.put(rawItem, number);
            return this;
        }

        @Deprecated
        public Builder putMandatoryCharactForDevItem(String item, String[] charact){
            //TODO: eliminare o cambiare nome
            mandatoryCharacteristicsForDevItem.put(item, charact);
            return this;
        }

        public Builder putDevItemsToItemType(String devItem, String itemType){
            devItemsToItemType.put(devItem, itemType);
            return this;
        }

        public Builder putRawItemToItems(String rawItem, String[] items){
            rawItemToItems.put(rawItem, items);
            return this;
        }

        public Builder putItemRoots(String item, String[] roots){
            itemRoots.put(item, roots);
            return this;
        }

        public Builder putMandatoryServicesCharacteristicsForDevItem
                (String item, HashMap<String, String[]> serviceAndChars){
            mandatoryServicesCharacteristicsForDevItem.put(item, serviceAndChars);
            return this;
        }

        public Builder putReadableCharacteristicsAndClustersPlusGroupForDevItem
                (String item, LinkedHashMap <String, String[][]> characteristicsAndClusters)
        {
            readableCharacteristicsAndClustersPlusGroupForDevItem.put(item, characteristicsAndClusters);
            return this;
        }

        public Builder putActionsForDevItem
                (String item, String[] actions)
        {
            actionsForDevItem.put(item, actions);
            return this;
        }

        public Builder putInitForDevItem
                (String item, String initId)
        {
            initForDevItem.put(item, initId);
            return this;
        }

    }


    //region xml devices
    //TODO: do not keep all devicesDescriptors in memory
    public static List<DevicesDescriptorNew> parseDevices(Context context) {

        final int NONE_PARSING = 0;
        final int FL_DEVICE_PARSING = 1;
        final int SL_BLE_PARSING = 2;
        final int TL_SERVICE = 3;
        final int FOL_CHARACTERISTICS_PARSING = 4;
        final int FIL_CHARACTERISTIC_PARSING = 5;
        final int SIL_READABLE_PARSING = 6;
        final int SEL_PARSER_PARSING = 7;
        final int SL_ITEMS_PARSING = 8;
        final int TL_ROOTITEM_PARSING = 9;
        final int FOL_SUBITEMS_PARSING = 10;
        final int FIL_ITEM_PARSING = 11;
        final int SIL_SERVICE_PARSING = 12;
        final int SIL_READABLECHAR_PARSING = 13;
        final int SEL_DATA_CLUSTER_PARSING = 14;
        final int SIL_ACTION_PARSING = 15;
        final int SIL_INIT_PARSING = 16;

        final int SIL_WRITABLE_PARSING = 17;
        final int SEL_MODEL_PARSING = 18;
        final int EL_STATIC_PARSING = 19;
        final int EL_DATA_PARSING = 20;
        final int NL_SPECIAL_PARSING = 21;
        final int NL_DATA_HANDLING_PARSING = 22;
        final int NL_BIT_LOGIC_PARSING=23;



        final int SEL_SEQUENCE_PARSING = 24;
        final int EL_SUB_ACTION_PARSING = 25;
        final int NL_WRITABLE_CHAR_PARSING = 26;
        final int NL_READABLE_CHAR_PARSING = 27;

        final int FOL_INIT_MODELS_PARSING = 28;
        final int FIL_INIT_PARSING = 29;
        final int SIL_SEQUENCE_PARSING = 30;
        final int SEL_SUB_ACTION_PARSING = 31;
        final int EL_WRITABLE_CHAR_PARSING = 32;
        final int EL_READABLE_CHAR_PARSING = 33;





        //List<DeviceDataParser> deviceDataParsers=DeviceDataParser.parse(context);

        List<DeviceDataParserCollector> deviceDataParserCollectors=
                DeviceDataParserCollector.parse(context);

        List <DevicesDescriptorNew> devicesDescriptors= new ArrayList<>();

        DevicesDescriptorNew.Builder devicesDescriptorBuilder = new Builder();
        String[] serviceNameAndUuid = new String[2];
        String[] charNameAndUuid = new String[2];
        boolean readableChar=false;
        boolean writableChar=false;
        BLEReadableCharacteristic.Builder bleReadableCharacteristicBuilder=
                new BLEReadableCharacteristic.Builder();
        List<BLEReadableCharacteristic.Builder> bleReadableCharacteristicBuilders=new ArrayList<>();
        List<BLEReadableCharacteristic> bleReadableCharacteristics=new ArrayList<>();

        BLEWritableCharacteristic.Builder bleWritableCharacteristicBuilder=
                new BLEWritableCharacteristic.Builder();
        List<BLEWritableCharacteristic.Builder> bleWritableCharacteristicBuilders=new ArrayList<>();
        List<BLEWritableCharacteristic> bleWritableCharacteristics=new ArrayList<>();

        BLEMessageModel.Builder meassgeModelBuilder=new BLEMessageModel.Builder();
        //List<BLEMessageModel> meassgeModels=new ArrayList<>();
        BLEDeviceData.Builder bleDeviceDataBuilder=new BLEDeviceData.Builder();
        BLEDeviceData.DataHandlerBuilder dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();


        int cardinality=0;
        String itemName="";
        String rootItemName="";
        List<String> mandatoryChars=new ArrayList<>();
        String mandatoryService="";
        HashMap<String, String[]> mandatoryServAndChars =new  HashMap<>();
        List<String[]> clusters=new ArrayList<>();
        String readableCharName="";
        LinkedHashMap <String, String[][]> readableCharsAndClusters =new  LinkedHashMap <>();
        List<String> itemsInRootItem=new ArrayList<>();
        String[] aux;
        String[][] auxDouble;
        String[] clusterAndGroup=new String[2];

        ///action
        BLEAction.Builder bleActionBuilder=new BLEAction.Builder();
        List<BLEAction.Builder>  bleActionsBuilders=new ArrayList<>();
        List<String> bleActions=new ArrayList<>();
        ///init
        BLEInit.Builder bleInitBuilder=new BLEInit.Builder();
        List<BLEInit.Builder>  bleInitsBuilders=new ArrayList<>();

        String initId="";


        //List<BLEAction> bleActions=new ArrayList<>();
        //BLEWritableCharacteristic bleWritableChar=null;
        BLESequenceElement.Builder bleSequenceElementBuilder= new BLESequenceElement.Builder();
        //String model=null;
        //String post_delay=null;
        //String type=null;
        ////

        String text="";

        //For debug purposes
        File file = new File(Environment.getExternalStorageDirectory(),"devices.xml");

        InputStream is= null;
        int parsingFirstLayerSection=NONE_PARSING;
        int parsingSecondLayerSection=NONE_PARSING;
        int parsingThirdLayerSection=NONE_PARSING;
        int parsingFourthLayerSection=NONE_PARSING;
        int parsingFifthLayerSection=NONE_PARSING;
        int parsingSixthLayerSection=NONE_PARSING;
        int parsingSeventhLayerSection=NONE_PARSING;
        int parsingEighthLayerSection=NONE_PARSING;
        int parsingNinthLayerSection=NONE_PARSING;



        try {

            if (BLEContext.isPermissionsGranted()) {
                is = new FileInputStream(file);
                Log.d(TAG, is.toString());


                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);

                XmlPullParser parser = factory.newPullParser();
                parser.setInput(is, null);

                int eventType = parser.getEventType();
                xml_scan_while:
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = parser.getName();
                    //Log.d(TAG, "tagname: "+tagname);
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (parsingFirstLayerSection == NONE_PARSING) {
                                if (tagname.equalsIgnoreCase("device")) {
                                    devicesDescriptorBuilder = new Builder();
                                    bleWritableCharacteristics = new ArrayList<>();
                                    bleReadableCharacteristics = new ArrayList<>();
                                    parsingFirstLayerSection = FL_DEVICE_PARSING;
                                }
                            } else if (parsingFirstLayerSection == FL_DEVICE_PARSING) {
                                if (parsingSecondLayerSection == NONE_PARSING) {
                                    if (tagname.equalsIgnoreCase("ble")) {
                                        parsingSecondLayerSection = SL_BLE_PARSING;
                                    } else if (tagname.equalsIgnoreCase("items")) {
                                        bleInitsBuilders = new ArrayList<>();
                                        parsingSecondLayerSection = SL_ITEMS_PARSING;
                                    }
                                } else if (parsingSecondLayerSection == SL_BLE_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("service")) {
                                            bleReadableCharacteristicBuilders = new ArrayList<>();
                                            bleWritableCharacteristicBuilders = new ArrayList<>();
                                            serviceNameAndUuid = new String[]{"", ""};
                                            parsingThirdLayerSection = TL_SERVICE;
                                        }
                                    } else if (parsingThirdLayerSection == TL_SERVICE) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("characteristics")) {
                                                charNameAndUuid = new String[]{"", ""};
                                                parsingFourthLayerSection = FOL_CHARACTERISTICS_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection == FOL_CHARACTERISTICS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("characteristic")) {
                                                    readableChar = false;
                                                    writableChar = false;
                                                    parsingFifthLayerSection = FIL_CHARACTERISTIC_PARSING;
                                                }
                                            } else if (parsingFifthLayerSection == FIL_CHARACTERISTIC_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("readable")) {
                                                        bleReadableCharacteristicBuilder =
                                                                new BLEReadableCharacteristic.Builder();
                                                        parsingSixthLayerSection = SIL_READABLE_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("writable")) {
                                                        bleWritableCharacteristicBuilder =
                                                                new BLEWritableCharacteristic.Builder();
                                                        parsingSixthLayerSection = SIL_WRITABLE_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_READABLE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("parser")) {
                                                            parsingSeventhLayerSection = SEL_PARSER_PARSING;
                                                        }
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_WRITABLE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("model")) {
                                                            meassgeModelBuilder = new BLEMessageModel.Builder();
                                                            parsingSeventhLayerSection = SEL_MODEL_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_MODEL_PARSING) {
                                                        if (parsingEighthLayerSection == NONE_PARSING) {
                                                            if (tagname.equalsIgnoreCase("static")) {
                                                                bleDeviceDataBuilder = new BLEDeviceData.Builder();
                                                                parsingEighthLayerSection = EL_STATIC_PARSING;
                                                            } else if (tagname.equalsIgnoreCase("data")) {
                                                                dataHandlerBuilder = new BLEDeviceData.DataHandlerBuilder();
                                                                dataHandlerBuilder.setIsInput(false);
                                                                bleDeviceDataBuilder = new BLEDeviceData.Builder();
                                                                parsingEighthLayerSection = EL_DATA_PARSING;
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_DATA_PARSING) {
                                                            if (parsingNinthLayerSection == NONE_PARSING) {
                                                                if (tagname.equalsIgnoreCase("special")) {
                                                                    parsingNinthLayerSection = NL_SPECIAL_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("data_handling")) {
                                                                    parsingNinthLayerSection = NL_DATA_HANDLING_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("bitlogic")) {
                                                                    parsingNinthLayerSection = NL_BIT_LOGIC_PARSING;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (parsingSecondLayerSection == SL_ITEMS_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("rootitem")) {
                                            cardinality = 0;
                                            itemsInRootItem = new ArrayList<>();
                                            parsingThirdLayerSection = TL_ROOTITEM_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection == TL_ROOTITEM_PARSING) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("subitems")) {
                                                parsingFourthLayerSection = FOL_SUBITEMS_PARSING;
                                            } else if (tagname.equalsIgnoreCase("init_models")) {
                                                parsingFourthLayerSection = FOL_INIT_MODELS_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection == FOL_SUBITEMS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("item")) {
                                                    initId = "";
                                                    bleActionsBuilders = new ArrayList<>();
                                                    readableCharsAndClusters = new LinkedHashMap<>();
                                                    bleActions = new ArrayList<>();
                                                    parsingFifthLayerSection = FIL_ITEM_PARSING;
                                                }
                                            } else if (parsingFifthLayerSection == FIL_ITEM_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("service")) {
                                                        mandatoryChars = new ArrayList<>();
                                                        parsingSixthLayerSection = SIL_SERVICE_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                        clusters = new ArrayList<>();
                                                        parsingSixthLayerSection = SIL_READABLECHAR_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("action")) {
                                                        bleActionBuilder = new BLEAction.Builder();
                                                        parsingSixthLayerSection = SIL_ACTION_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("init")) {
                                                        //bleInitBuilder=new BLEInit.Builder();
                                                        parsingSixthLayerSection = SIL_INIT_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_ACTION_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("sequence")) {
                                                            parsingSeventhLayerSection = SEL_SEQUENCE_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_SEQUENCE_PARSING) {
                                                        if (parsingEighthLayerSection == NONE_PARSING) {
                                                            if (tagname.equalsIgnoreCase("sub_action")) {
                                                                parsingEighthLayerSection = EL_SUB_ACTION_PARSING;
                                                                bleSequenceElementBuilder = new BLESequenceElement.Builder();
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_SUB_ACTION_PARSING) {
                                                            if (tagname.equalsIgnoreCase("writableCharacteristic")) {
                                                                parsingNinthLayerSection = NL_WRITABLE_CHAR_PARSING;
                                                            } else if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                                parsingNinthLayerSection = NL_READABLE_CHAR_PARSING;
                                                            }
                                                        }
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_READABLECHAR_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("dataCluster")) {
                                                            clusterAndGroup = new String[2];
                                                            parsingSeventhLayerSection = SEL_DATA_CLUSTER_PARSING;
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (parsingFourthLayerSection == FOL_INIT_MODELS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("init")) {
                                                    bleInitBuilder = new BLEInit.Builder();
                                                    parsingFifthLayerSection = FIL_INIT_PARSING;
                                                }
                                            } else if (parsingFifthLayerSection == FIL_INIT_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("sequence")) {
                                                        parsingSixthLayerSection = SIL_SEQUENCE_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_SEQUENCE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("sub_action")) {
                                                            parsingSeventhLayerSection = SEL_SUB_ACTION_PARSING;
                                                            bleSequenceElementBuilder = new BLESequenceElement.Builder();
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_SUB_ACTION_PARSING) {
                                                        if (tagname.equalsIgnoreCase("writableCharacteristic")) {
                                                            parsingEighthLayerSection = EL_WRITABLE_CHAR_PARSING;
                                                        } else if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                            parsingEighthLayerSection = EL_READABLE_CHAR_PARSING;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;

                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            //Log.d(TAG, "text: "+text);
                            break;

                        case XmlPullParser.END_TAG:
                            if (parsingFirstLayerSection == NONE_PARSING) {
                                if (tagname.equalsIgnoreCase("devices"))
                                    break xml_scan_while;
                            } else if (parsingFirstLayerSection == FL_DEVICE_PARSING) {
                                if (parsingSecondLayerSection == NONE_PARSING) {
                                    if (tagname.equalsIgnoreCase("deviceType")) {
                                        devicesDescriptorBuilder.setDeviceType(text);
                                    } else if (tagname.equalsIgnoreCase("nickname")) {
                                        devicesDescriptorBuilder.setNickName(text);
                                    } else if (tagname.equalsIgnoreCase("device")) {
                                        devicesDescriptors.add(devicesDescriptorBuilder.build());
                                        parsingFirstLayerSection = NONE_PARSING;
                                    }
                                } else if (parsingSecondLayerSection == SL_BLE_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("ble")) {
                                            parsingSecondLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection == TL_SERVICE) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("name")) {
                                                serviceNameAndUuid[0] = text;
                                            } else if (tagname.equalsIgnoreCase("uuid")) {
                                                serviceNameAndUuid[1] = text;
                                            } else if (tagname.equalsIgnoreCase("service")) {
                                                for (BLEWritableCharacteristic.Builder builder
                                                        : bleWritableCharacteristicBuilders) {
                                                    builder.setService_name(serviceNameAndUuid[0]);
                                                    BLEWritableCharacteristic bleWritableCharacteristic =
                                                            builder.build();
                                                    //useful also for sequence section
                                                    if (bleWritableCharacteristic != null)
                                                        bleWritableCharacteristics.add(bleWritableCharacteristic);
                                                    //                                                    devicesDescriptorBuilder
                                                    //                                                            .setBleWritableCharacteristics
                                                    //                                                                    (bleWritableCharacteristics);
                                                }

                                                for (BLEReadableCharacteristic.Builder builder
                                                        : bleReadableCharacteristicBuilders) {
                                                    builder.setService_name(serviceNameAndUuid[0]);
                                                    BLEReadableCharacteristic bleReadableCharacteristic =
                                                            builder.build();
                                                    if (bleReadableCharacteristic != null) {
                                                        devicesDescriptorBuilder
                                                                .addBleReadableCharacteristic
                                                                        (bleReadableCharacteristic);
                                                        bleReadableCharacteristics.add(bleReadableCharacteristic);
                                                    }
                                                }

                                                devicesDescriptorBuilder.putStToUUID
                                                        (serviceNameAndUuid[0], serviceNameAndUuid[1]);
                                                parsingThirdLayerSection = NONE_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection == FOL_CHARACTERISTICS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("characteristics")) {
                                                    parsingFourthLayerSection = NONE_PARSING;
                                                }
                                            } else if (parsingFifthLayerSection == FIL_CHARACTERISTIC_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("name")) {
                                                        charNameAndUuid[0] = text;
                                                    } else if (tagname.equalsIgnoreCase("uuid")) {
                                                        charNameAndUuid[1] = text;
                                                    } else if (tagname.equalsIgnoreCase("characteristic")) {
                                                        devicesDescriptorBuilder.putStToUUID
                                                                (charNameAndUuid[0], charNameAndUuid[1]);
                                                        if (readableChar) {
                                                            bleReadableCharacteristicBuilder
                                                                    .setName(charNameAndUuid[0]);
                                                            bleReadableCharacteristicBuilder
                                                                    .setUUID(charNameAndUuid[1]);
                                                            bleReadableCharacteristicBuilders
                                                                    .add(bleReadableCharacteristicBuilder);
                                                            //                                                        BLEReadableCharacteristic bleReadableCharacteristic=
                                                            //                                                                bleReadableCharacteristicBuilder.build();
                                                            //                                                        if (bleReadableCharacteristic!=null){
                                                            //                                                            devicesDescriptorBuilder
                                                            //                                                                    .addBleReadableCharacteristic
                                                            //                                                                            (bleReadableCharacteristic);
                                                            //                                                        }
                                                        }
                                                        if (writableChar) {
                                                            bleWritableCharacteristicBuilder
                                                                    .setName(charNameAndUuid[0]);
                                                            bleWritableCharacteristicBuilder
                                                                    .setUuid(charNameAndUuid[1]);
                                                            bleWritableCharacteristicBuilders.add(bleWritableCharacteristicBuilder);
                                                            /*BLEWritableCharacteristic bleWritableCharacteristic=
                                                                    bleWritableCharacteristicBuilder.build();
                                                            if (bleWritableCharacteristic!=null)
                                                                devicesDescriptorBuilder
                                                                        .addBleWritableCharacteristic
                                                                                (bleWritableCharacteristic);*/
                                                        }
                                                        parsingFifthLayerSection = NONE_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_READABLE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("readable")) {
                                                            readableChar = true;
                                                            parsingSixthLayerSection = NONE_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_PARSER_PARSING) {
                                                        if (tagname.equalsIgnoreCase("parser_id")) {
                                                            //                                                        for (DeviceDataParser deviceDataParser:deviceDataParsers){
                                                            //                                                            if (deviceDataParser.getParser_id().equalsIgnoreCase(text)){
                                                            //                                                                bleReadableCharacteristicBuilder
                                                            //                                                                        .setParser(deviceDataParser);
                                                            //                                                                bleReadableCharacteristicBuilder
                                                            //                                                                        .setBleDeviceDataClusters(deviceDataParser.cloneDataModel());
                                                            //                                                                break;
                                                            //                                                            }
                                                            //                                                        }
                                                            for (DeviceDataParserCollector deviceDataParserCollector : deviceDataParserCollectors) {
                                                                if (deviceDataParserCollector.getId().equalsIgnoreCase(text)) {
                                                                    bleReadableCharacteristicBuilder
                                                                            .setDeviceDataParserCollector(deviceDataParserCollector);
                                                                    for (DeviceDataParser deviceDataParser : deviceDataParserCollector.getDeviceDataParsers()) {
                                                                        bleReadableCharacteristicBuilder
                                                                                .addBLEDeviceDataClusters(deviceDataParser.cloneDataModel());
                                                                    }
                                                                    break;
                                                                }
                                                            }
                                                        } else if (tagname.equalsIgnoreCase("rootitem")) {
                                                            bleReadableCharacteristicBuilder.setRootItem(text);
                                                        } else if (tagname.equalsIgnoreCase("parser")) {
                                                            parsingSeventhLayerSection = NONE_PARSING;
                                                        }
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_WRITABLE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("writable")) {
                                                            writableChar = true;
                                                            parsingSixthLayerSection = NONE_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_MODEL_PARSING) {
                                                        if (parsingEighthLayerSection == NONE_PARSING) {
                                                            if (tagname.equalsIgnoreCase("model")) {
                                                                parsingSeventhLayerSection = NONE_PARSING;
                                                                bleWritableCharacteristicBuilder
                                                                        .addBleMessageModel(meassgeModelBuilder.build());
                                                            } else if (tagname.equalsIgnoreCase("id")) {
                                                                meassgeModelBuilder.setId(text);
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_STATIC_PARSING) {
                                                            if (parsingNinthLayerSection == NONE_PARSING) {
                                                                if (tagname.equalsIgnoreCase("static")) {
                                                                    Log.d(TAG, "id model, static");
                                                                    meassgeModelBuilder
                                                                            .addBleDeviceStaticData(bleDeviceDataBuilder.build());
                                                                    parsingEighthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                                    bleDeviceDataBuilder.setDefaultValue(text);
                                                                } else if (tagname.equalsIgnoreCase("format")) {
                                                                    bleDeviceDataBuilder.setFormat(text);
                                                                } else if (tagname.equalsIgnoreCase("position")) {
                                                                    bleDeviceDataBuilder.setPosition(text);
                                                                } else if (tagname.equalsIgnoreCase("id")) {
                                                                    bleDeviceDataBuilder.setId(text);
                                                                }
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_DATA_PARSING) {
                                                            if (parsingNinthLayerSection == NONE_PARSING) {
                                                                if (tagname.equalsIgnoreCase("data")) {
                                                                    if (bleDeviceDataBuilder.dependency_id == null) {
                                                                        dataHandlerBuilder.setPosition
                                                                                (bleDeviceDataBuilder.position);
                                                                        bleDeviceDataBuilder
                                                                                .addDataHandlerBuilder(dataHandlerBuilder);
                                                                    }
                                                                    Log.d(TAG, "id model, data");
                                                                    meassgeModelBuilder
                                                                            .addBleDeviceData(bleDeviceDataBuilder.build());
                                                                    parsingEighthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("id")) {
                                                                    bleDeviceDataBuilder.setId(text);
                                                                } else if (tagname.equalsIgnoreCase("format")) {
                                                                    bleDeviceDataBuilder.setFormat(text);
                                                                    dataHandlerBuilder.setFormat(text);
                                                                } else if (tagname.equalsIgnoreCase("type")) {
                                                                    bleDeviceDataBuilder.setData_type(text);
                                                                    dataHandlerBuilder.setData_type(text);
                                                                } else if (tagname.equalsIgnoreCase("position")) {
                                                                    bleDeviceDataBuilder.setPosition(text);
                                                                } else if (tagname.equalsIgnoreCase("slope")) {
                                                                    dataHandlerBuilder.setSlope(text);
                                                                } else if (tagname.equalsIgnoreCase("intercept")) {
                                                                    dataHandlerBuilder.setIntercept(text);
                                                                } else if (tagname.equalsIgnoreCase("bytelogic")) {
                                                                    dataHandlerBuilder.setByteLogic(text);
                                                                }
                                                            } else if (parsingNinthLayerSection == NL_SPECIAL_PARSING) {
                                                                if (tagname.equalsIgnoreCase("special")) {
                                                                    parsingNinthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                                    dataHandlerBuilder.addSpecial(text);
                                                                }
                                                            } else if (parsingNinthLayerSection == NL_DATA_HANDLING_PARSING) {
                                                                if (tagname.equalsIgnoreCase("data_handling")) {
                                                                    parsingNinthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("type")) {
                                                                    dataHandlerBuilder.setHandle_type(text);
                                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                                    dataHandlerBuilder.setHandle_value(text);
                                                                }
                                                            } else if (parsingNinthLayerSection == NL_BIT_LOGIC_PARSING) {
                                                                if (tagname.equalsIgnoreCase("bitlogic")) {
                                                                    parsingNinthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("operation")) {
                                                                    dataHandlerBuilder.setLogicOperation(text);
                                                                } else if (tagname.equalsIgnoreCase("value")) {
                                                                    dataHandlerBuilder.setLogicOperationValue(text);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (parsingSecondLayerSection == SL_ITEMS_PARSING) {
                                    if (parsingThirdLayerSection == NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("items")) {
                                            for (BLEInit.Builder builder : bleInitsBuilders) {
                                                //builder.setItemId(itemName);
                                                devicesDescriptorBuilder.addBleInit
                                                        (builder.build());
                                            }
                                            parsingSecondLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection == TL_ROOTITEM_PARSING) {
                                        if (parsingFourthLayerSection == NONE_PARSING) {
                                            if (tagname.equalsIgnoreCase("name")) {
                                                rootItemName = text;
                                            } else if (tagname.equalsIgnoreCase("rootitem")) {
                                                devicesDescriptorBuilder.putDevRawItemNumber
                                                        (rootItemName, cardinality);
                                                aux = new String[itemsInRootItem.size()];
                                                for (int i = 0; i < itemsInRootItem.size(); i++)
                                                    aux[i] = itemsInRootItem.get(i);
                                                devicesDescriptorBuilder.putRawItemToItems
                                                        (rootItemName, aux);
                                                parsingThirdLayerSection = NONE_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection == FOL_SUBITEMS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("subitems")) {
                                                    parsingFourthLayerSection = NONE_PARSING;
                                                }
                                            } else if (parsingFifthLayerSection == FIL_ITEM_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("name")) {
                                                        itemName = text;
                                                        devicesDescriptorBuilder.putDevItemCardinality
                                                                (itemName, cardinality);
                                                        devicesDescriptorBuilder.putItemRoots
                                                                (itemName, new String[]{rootItemName});
                                                        cardinality++;
                                                        itemsInRootItem.add(itemName);
                                                    } else if (tagname.equalsIgnoreCase("bletype")) {
                                                        devicesDescriptorBuilder
                                                                .putDevItemsToItemType(itemName, text);
                                                    } else if (tagname.equalsIgnoreCase("item")) {

                                                        for (BLEAction.Builder builder : bleActionsBuilders) {
                                                            builder.setItemId(itemName);
                                                            devicesDescriptorBuilder.addBleAction
                                                                    (builder.build());
                                                        }

                                                        devicesDescriptorBuilder.putInitForDevItem
                                                                (itemName, initId);

                                                        //                                                    for (BLEInit.Builder builder:bleInitsBuilders){
                                                        //                                                        builder.setItemId(itemName);
                                                        //                                                        devicesDescriptorBuilder.addBleInit
                                                        //                                                                (builder.build());
                                                        //                                                    }

                                                        String[] aux_str = new String[bleActions.size()];
                                                        for (int i = 0; i < bleActions.size(); i++)
                                                            aux_str[i] = bleActions.get(i);
                                                        devicesDescriptorBuilder
                                                                .putReadableCharacteristicsAndClustersPlusGroupForDevItem
                                                                        (itemName, readableCharsAndClusters)
                                                                .putActionsForDevItem(itemName, aux_str);
                                                        parsingFifthLayerSection = NONE_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_SERVICE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("name")) {
                                                        mandatoryService = text;
                                                    } else if (tagname.equalsIgnoreCase("characteristic")) {
                                                        mandatoryChars.add(text);
                                                    } else if (tagname.equalsIgnoreCase("service")) {
                                                        aux = new String[mandatoryChars.size()];
                                                        for (int i = 0; i < mandatoryChars.size(); i++)
                                                            aux[i] = mandatoryChars.get(i);
                                                        mandatoryServAndChars = new HashMap<>();
                                                        mandatoryServAndChars.put(mandatoryService, aux);
                                                        devicesDescriptorBuilder
                                                                .putMandatoryServicesCharacteristicsForDevItem
                                                                        (itemName, mandatoryServAndChars);
                                                        devicesDescriptorBuilder
                                                                .putMandatoryCharactForDevItem
                                                                        (itemName, aux);
                                                        parsingSixthLayerSection = NONE_PARSING;
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_READABLECHAR_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("name")) {
                                                            readableCharName = text;
                                                        } else if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                            auxDouble = new String[clusters.size()][2];
                                                            for (int i = 0; i < clusters.size(); i++)
                                                                auxDouble[i] = clusters.get(i);
                                                            readableCharsAndClusters.put(readableCharName, auxDouble);
                                                            parsingSixthLayerSection = NONE_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_DATA_CLUSTER_PARSING) {
                                                        if (tagname.equalsIgnoreCase("dataCluster")) {
                                                            if ((clusterAndGroup[0] == null)) {
                                                                if (clusterAndGroup[1] == null) {
                                                                    clusterAndGroup[0] = text;
                                                                    clusterAndGroup[1] = text;
                                                                } else {
                                                                    //TODO: throw exception name of the cluster missing
                                                                }
                                                            } else if (clusterAndGroup[1] == null) {
                                                                clusterAndGroup[1] = clusterAndGroup[0];
                                                            }
                                                            clusters.add(clusterAndGroup);
                                                            parsingSeventhLayerSection = NONE_PARSING;
                                                        } else if (tagname.equalsIgnoreCase("name")) {
                                                            clusterAndGroup[0] = text;
                                                        } else if (tagname.equalsIgnoreCase("group")) {
                                                            clusterAndGroup[1] = text;
                                                        }
                                                    }

                                                } else if (parsingSixthLayerSection == SIL_ACTION_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("action")) {
                                                            bleActionsBuilders.add(bleActionBuilder);
                                                            parsingSixthLayerSection = NONE_PARSING;
                                                        } else if (tagname.equalsIgnoreCase("id")) {
                                                            bleActionBuilder.setId(text);
                                                            bleActions.add(text);
                                                        } else if (tagname.equalsIgnoreCase("post_delay")) {
                                                            bleActionBuilder.setPost_delay(text);
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_SEQUENCE_PARSING) {
                                                        if (parsingEighthLayerSection == NONE_PARSING) {
                                                            if (tagname.equalsIgnoreCase("sequence")) {
                                                                parsingSeventhLayerSection = NONE_PARSING;
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_SUB_ACTION_PARSING) {
                                                            if (parsingNinthLayerSection == NONE_PARSING) {
                                                                if (tagname.equalsIgnoreCase("sub_action")) {
                                                                    //TODO: harmonize xml label and class names
                                                                    bleActionBuilder.addBleSequenceElement
                                                                            (bleSequenceElementBuilder.build());
                                                                    parsingEighthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("type")) {
                                                                    bleSequenceElementBuilder.setType(text);
                                                                } else if (tagname.equalsIgnoreCase("post_delay")) {
                                                                    bleSequenceElementBuilder.setPost_delay(text);
                                                                }
                                                            } else if (parsingNinthLayerSection == NL_WRITABLE_CHAR_PARSING) {
                                                                if (tagname.equalsIgnoreCase("writableCharacteristic")) {
                                                                    parsingNinthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("name")) {
                                                                    for (BLEWritableCharacteristic bleWritableCharacteristic
                                                                            : bleWritableCharacteristics) {
                                                                        if (bleWritableCharacteristic.getName().equalsIgnoreCase(text)) {
                                                                            bleSequenceElementBuilder
                                                                                    .setBleWritableCharacteristic(bleWritableCharacteristic);
                                                                            break;
                                                                        }
                                                                    }
                                                                } else if (tagname.equalsIgnoreCase("model")) {
                                                                    bleSequenceElementBuilder.setModelWriteChar(text);
                                                                }
                                                            }
                                                            /////////////
                                                            else if (parsingNinthLayerSection == NL_READABLE_CHAR_PARSING) {
                                                                if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                                    parsingNinthLayerSection = NONE_PARSING;
                                                                } else if (tagname.equalsIgnoreCase("name")) {
                                                                    for (BLEReadableCharacteristic bleReadableCharacteristic :
                                                                            bleReadableCharacteristics) {
                                                                        if (bleReadableCharacteristic.getName().equalsIgnoreCase(text)) {
                                                                            bleSequenceElementBuilder
                                                                                    .setBleReadableCharacteristic(bleReadableCharacteristic);
                                                                            break;
                                                                        }
                                                                    }
                                                                } else if (tagname.equalsIgnoreCase("model")) {
                                                                    //TODO: model should be used for event related to the reception of specific message
                                                                    // bleSequenceElementBuilder.seModelReadChar(text);
                                                                }
                                                            }
                                                            /////////////////
                                                        }
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_INIT_PARSING) {
                                                    if (tagname.equalsIgnoreCase("init")) {
                                                        parsingSixthLayerSection = NONE_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("id")) {
                                                        initId = text;
                                                    }
                                                }
                                            }
                                        } else if (parsingFourthLayerSection == FOL_INIT_MODELS_PARSING) {
                                            if (parsingFifthLayerSection == NONE_PARSING) {
                                                if (tagname.equalsIgnoreCase("init_models")) {
                                                    parsingFourthLayerSection = NONE_PARSING;
                                                }
                                            }

                                            //////////
                                            else if (parsingFifthLayerSection == FIL_INIT_PARSING) {
                                                if (parsingSixthLayerSection == NONE_PARSING) {
                                                    if (tagname.equalsIgnoreCase("init")) {
                                                        bleInitsBuilders.add(bleInitBuilder);
                                                        parsingFifthLayerSection = NONE_PARSING;
                                                    } else if (tagname.equalsIgnoreCase("id")) {
                                                        bleInitBuilder.setId(text);
                                                    } else if (tagname.equalsIgnoreCase("post_delay")) {
                                                        bleInitBuilder.setPost_delay(text);
                                                    }
                                                } else if (parsingSixthLayerSection == SIL_SEQUENCE_PARSING) {
                                                    if (parsingSeventhLayerSection == NONE_PARSING) {
                                                        if (tagname.equalsIgnoreCase("sequence")) {
                                                            parsingSixthLayerSection = NONE_PARSING;
                                                        }
                                                    } else if (parsingSeventhLayerSection == SEL_SUB_ACTION_PARSING) {
                                                        if (parsingEighthLayerSection == NONE_PARSING) {
                                                            if (tagname.equalsIgnoreCase("sub_action")) {
                                                                //TODO: harmonize xml label and class names
                                                                bleInitBuilder.addBleSequenceElement
                                                                        (bleSequenceElementBuilder.build());
                                                                parsingSeventhLayerSection = NONE_PARSING;
                                                            } else if (tagname.equalsIgnoreCase("type")) {
                                                                bleSequenceElementBuilder.setType(text);
                                                            } else if (tagname.equalsIgnoreCase("post_delay")) {
                                                                bleSequenceElementBuilder.setPost_delay(text);
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_WRITABLE_CHAR_PARSING) {
                                                            if (tagname.equalsIgnoreCase("writableCharacteristic")) {
                                                                parsingEighthLayerSection = NONE_PARSING;
                                                            } else if (tagname.equalsIgnoreCase("name")) {
                                                                for (BLEWritableCharacteristic bleWritableCharacteristic
                                                                        : bleWritableCharacteristics) {
                                                                    if (bleWritableCharacteristic.getName().equalsIgnoreCase(text)) {
                                                                        bleSequenceElementBuilder
                                                                                .setBleWritableCharacteristic(bleWritableCharacteristic);
                                                                        break;
                                                                    }
                                                                }
                                                            } else if (tagname.equalsIgnoreCase("model")) {
                                                                bleSequenceElementBuilder.setModelWriteChar(text);
                                                            }
                                                        } else if (parsingEighthLayerSection == EL_READABLE_CHAR_PARSING) {
                                                            if (tagname.equalsIgnoreCase("readableCharacteristic")) {
                                                                parsingEighthLayerSection = NONE_PARSING;
                                                            } else if (tagname.equalsIgnoreCase("name")) {
                                                                for (BLEReadableCharacteristic bleReadableCharacteristic :
                                                                        bleReadableCharacteristics) {
                                                                    if (bleReadableCharacteristic.getName().equalsIgnoreCase(text)) {
                                                                        bleSequenceElementBuilder
                                                                                .setBleReadableCharacteristic(bleReadableCharacteristic);
                                                                        break;
                                                                    }
                                                                }
                                                            } else if (tagname.equalsIgnoreCase("model")) {
                                                                //TODO: model should be used for event related to the reception of specific message
                                                                // bleSequenceElementBuilder.seModelReadChar(text);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //////////

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

        return devicesDescriptors;
    }
    //endregion

    public static DevicesDescriptorNew getDeviceDescriptorByName(String name){
        DevicesDescriptorNew devicesDescriptorNew=null;
            List<DevicesDescriptorNew> devicesDescriptors=DevicesDescriptorNew
                    .parseDevices(BLEContext.context);
        for (DevicesDescriptorNew devicesDescriptor:devicesDescriptors){
            if (devicesDescriptor.getDeviceType().equalsIgnoreCase(name)){
                devicesDescriptorNew=devicesDescriptor;
                break;
            }
        }
        return devicesDescriptorNew;
    }

    public static DevicesDescriptorNew getDeviceDescriptorByName
            (String name, List<DevicesDescriptorNew> devicesDescriptors){
        DevicesDescriptorNew devicesDescriptorNew=null;
        if (devicesDescriptors==null)
            devicesDescriptors=DevicesDescriptorNew.parseDevices(BLEContext.context);
        for (DevicesDescriptorNew devicesDescriptor:devicesDescriptors){
            if (devicesDescriptor.getDeviceType().equalsIgnoreCase(name)){
                devicesDescriptorNew=devicesDescriptor;
                break;
            }
        }
        return devicesDescriptorNew;
    }

}
