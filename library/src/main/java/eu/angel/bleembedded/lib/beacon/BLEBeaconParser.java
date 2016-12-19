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
package eu.angel.bleembedded.lib.beacon;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.BeaconParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.complements.QuickSorter;
import eu.angel.bleembedded.lib.data.BLEBeaconData;
import eu.angel.bleembedded.lib.data.ParsingComplements;

/**
 * Class for wrapping {@link BeaconParser} simplifying the parser description.
 */
public class BLEBeaconParser {

    public static final String ALTBEACON_LAYOUT = BeaconParser.ALTBEACON_LAYOUT;
    public static final String EDDYSTONE_TLM_LAYOUT = BeaconParser.EDDYSTONE_TLM_LAYOUT;
    public static final String EDDYSTONE_UID_LAYOUT = BeaconParser.EDDYSTONE_UID_LAYOUT;
    public static final String EDDYSTONE_URL_LAYOUT = BeaconParser.EDDYSTONE_URL_LAYOUT;
    public static final String URI_BEACON_LAYOUT = BeaconParser.URI_BEACON_LAYOUT;
    public static final String I_BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String NEARABLE_ESTIMOTE_LAYOUT =
        "m:1-2=0101,i:3-11,d:12-12,d:13-13,d:14-15,d:16-16,d:17-17,d:18-18,d:19-19,d:20-20,p:21-21";


    public static final String ALTBEACON_LAYOUT_NAME = "ALTBEACON";
    public static final String EDDYSTONE_TLM_LAYOUT_NAME = "EDDYSTONE_TLM";
    public static final String EDDYSTONE_UID_LAYOUT_NAME = "EDDYSTONE_UID";
    public static final String EDDYSTONE_URL_LAYOUT_NAME = "EDDYSTONE_URL";
    public static final String URI_BEACON_LAYOUT_NAME = "URI_BEACON";
    public static final String I_BEACON_LAYOUT_NAME = "I_BEACON";
    public static final String NEARABLE_ESTIMOTE_NAME = "NEARABLE_ESTIMOTE";

    static HashMap<String, String> nameToParser = new HashMap<>();
    static HashMap<String, String> parserToName = new HashMap<>();

    static {

        nameToParser.put(ALTBEACON_LAYOUT_NAME,ALTBEACON_LAYOUT);
        nameToParser.put(EDDYSTONE_TLM_LAYOUT_NAME,EDDYSTONE_TLM_LAYOUT);
        nameToParser.put(EDDYSTONE_UID_LAYOUT_NAME,EDDYSTONE_UID_LAYOUT);
        nameToParser.put(EDDYSTONE_URL_LAYOUT_NAME,EDDYSTONE_URL_LAYOUT);
        nameToParser.put(URI_BEACON_LAYOUT_NAME,URI_BEACON_LAYOUT);
        nameToParser.put(I_BEACON_LAYOUT_NAME,I_BEACON_LAYOUT);
        nameToParser.put(NEARABLE_ESTIMOTE_NAME,NEARABLE_ESTIMOTE_LAYOUT);

        parserToName.put(ALTBEACON_LAYOUT,ALTBEACON_LAYOUT_NAME);
        parserToName.put(EDDYSTONE_TLM_LAYOUT,EDDYSTONE_TLM_LAYOUT_NAME);
        parserToName.put(EDDYSTONE_UID_LAYOUT,EDDYSTONE_UID_LAYOUT_NAME);
        parserToName.put(EDDYSTONE_URL_LAYOUT,EDDYSTONE_URL_LAYOUT_NAME);
        parserToName.put(URI_BEACON_LAYOUT,URI_BEACON_LAYOUT_NAME);
        parserToName.put(I_BEACON_LAYOUT,I_BEACON_LAYOUT_NAME);
        parserToName.put(NEARABLE_ESTIMOTE_LAYOUT,NEARABLE_ESTIMOTE_NAME);

    }

    /**
     * Get all the parser stored on the xml file.
     * @return the {@link BLEBeaconParser} stored.
     */
    public static List<BLEBeaconParser> getAllParsers()
    {
        List<BLEBeaconParser> bleBeaconParsers=XML_handler.parseBLEParsers(BLEContext.context);
        return bleBeaconParsers;
    }


    //private static List<BLEBeaconParser> allBleBeaconParsers;

    private String parser_layout;
    private String parser_identifier;
    private List<BLEBeaconData> data;

    private boolean has_extra_layout=false;
    private String extra_layout_parser_layout;
    private List<BLEBeaconData> extra_layout_data;

    private static final String matching_bytes_field_id="m";
    private static final String serviceUUID_field_id="s";
    private static final String id_field_id="i";
    private static final String power_field_id="p";
    private static final String data_field_id="d";
    private static final String extra_layout_field_id="x";
    private static final String comma=",";
    private static final String colon=":";
    private static final String equal="=";

    /**
     * Construtor.
     * @param parser_identifier id of the Parser
     * @param data list of  {@link BLEBeaconData} furnished by the beacons
     *             parsed by this actual parser
     * @param parser_layout Altbeacon-style parser layout
     * @param has_extra_layout whether the parser includes extra layout
     * @param extra_layout_data Altbeacon-style parser layout of the extra section
     */
    private BLEBeaconParser(String parser_identifier,
                            List<BLEBeaconData> data,
                            String parser_layout, boolean has_extra_layout,
                            List<BLEBeaconData> extra_layout_data,
                            String extra_layout_parser_layout)
    {
        this.parser_identifier=parser_identifier;
        this.data=data;
        this.parser_layout=parser_layout;
        this.has_extra_layout=has_extra_layout;
        this.extra_layout_data=extra_layout_data;
        this.extra_layout_parser_layout=extra_layout_parser_layout;
    }

    public String getParser_identifier() {
        return parser_identifier;
    }

    public String getParser_layout() {
        return parser_layout;
    }

    @Nullable
    public BeaconParser getBeaconParser(){
        if (parser_layout== null)
            return null;
        BeaconParser beaconParser=new BeaconParser(parser_identifier)
                .setBeaconLayout(parser_layout);
        return beaconParser;
    }

    public List<BLEBeaconData> getData(){
        return Collections.unmodifiableList(this.data);
    }

    public boolean hasExtra_layout() {
        return has_extra_layout;
    }

    public String getExtra_layoutParser_layout() {
        return extra_layout_parser_layout;
    }

    public List<BLEBeaconData> getExtra_layoutData(){
        return Collections.unmodifiableList(this.extra_layout_data);
    }

    @Nullable
    public BeaconParser getExtra_layout_BeaconParser(){
        if (parser_layout== null)
            return null;
        BeaconParser beaconParser=new BeaconParser(parser_identifier)
                .setBeaconLayout(extra_layout_parser_layout);
        return beaconParser;
    }


    public static class Builder  {

        private String parser_layout;
        private boolean has_extra_layout=false;
        private String parser_identifier;
        private String matching_bytes;
        private String matching_bytes_position;
        private String serviceUUID;
        private String serviceUUID_position;
        private String id1_position;
        private String id2_position;
        private String id3_position;
        private String power_position;
        private String power_offset;
        private List<String> data_position=new ArrayList<>();
        private List<BLEBeaconData> data=new ArrayList<>();

        private String extra_layout_parser_layout;
        private List<String> extra_layout_data_position=new ArrayList<>();
        private List<BLEBeaconData> extra_layout_data=new ArrayList<>();
        private String extra_layout_matching_bytes;
        private String extra_layout_matching_bytes_position;
        private String extra_layout_serviceUUID_position;

        public BLEBeaconParser build() {
            if (parser_layout==null)
                parser_layout=getSortedParser(matching_bytes,
                        matching_bytes_position,
                        serviceUUID,
                        serviceUUID_position,
                        id1_position,
                        id2_position,
                        id3_position,
                        power_position,
                        power_offset,
                        data_position, false);
            if((extra_layout_parser_layout==null)&&(has_extra_layout)){
                String extra_layout_matching_bytes_aux=null;
                if (extra_layout_matching_bytes!=null)
                    extra_layout_matching_bytes_aux=extra_layout_matching_bytes;
                else
                    extra_layout_matching_bytes_aux=matching_bytes;
                String serviceUUID_aux=null;
                if (serviceUUID!=null){
                    serviceUUID_aux=serviceUUID;
                }
                extra_layout_parser_layout=getSortedParser(extra_layout_matching_bytes_aux,
                        extra_layout_matching_bytes_position,
                        serviceUUID_aux,
                        extra_layout_serviceUUID_position,
                        null,
                        null,
                        null,
                        null,
                        null,
                        extra_layout_data_position, has_extra_layout);
            }
            return new BLEBeaconParser(parser_identifier,
                    data, parser_layout, has_extra_layout,
                    extra_layout_data, extra_layout_parser_layout);
        }

        public Builder setParser_layout(String parser_layout) {
            this.parser_layout = parser_layout;
            return this;
        }

        public Builder setParser_identifier(String parser_identifier) {
            this.parser_identifier = parser_identifier;
            return this;
        }

        public Builder setMatching_bytes(String matching_bytes) {
            this.matching_bytes = matching_bytes;
            return this;
        }

        public Builder setMatching_bytes_position(String matching_bytes_position) {
            this.matching_bytes_position = matching_bytes_position;
            return this;
        }

        public Builder setServiceUUID(String serviceUUID) {
            this.serviceUUID = serviceUUID;
            return this;
        }

        public Builder setServiceUUID_position(String serviceUUID_position) {
            this.serviceUUID_position = serviceUUID_position;
            return this;
        }

        public Builder setId1_position(String id1_position) {
            this.id1_position = id1_position;
            return this;
        }

        public Builder setId2_position(String id2_position) {
            this.id2_position = id2_position;
            return this;
        }

        public Builder setId3_position(String id3_position) {
            this.id3_position = id3_position;
            return this;
        }

        public Builder setPower_position(String power_position) {
            this.power_position = power_position;
            return this;
        }

        public Builder setPower_offset(String power_offset) {
            this.power_offset = power_offset;
            return this;
        }

        public Builder setData_position(List<String> data_position) {
            this.data_position = data_position;
            return this;
        }

        public Builder addData_position(String data_position) {
            this.data_position.add(data_position);
            return this;
        }

        public Builder setExtraLayoutBeacon()
        {
            has_extra_layout=true;
            return this;
        }

        public Builder setExtra_layout_serviceUUID_position(String extra_layout_serviceUUID_position) {
            this.extra_layout_serviceUUID_position = extra_layout_serviceUUID_position;
            return this;
        }

        public Builder setData(List<BLEBeaconData> data) {
            this.data = data;
            return this;
        }

        public Builder addData(BLEBeaconData data) {
            this.data.add(data);
            return this;
        }

        public Builder setExtra_layout_parser_layout(String extra_layout_parser_layout) {
            this.extra_layout_parser_layout = extra_layout_parser_layout;
            return this;
        }

        public Builder addExtraLayoutData_position(String data_position) {
            this.extra_layout_data_position.add(data_position);
            return this;
        }

        public Builder setExtraLayoutData(List<BLEBeaconData> data) {
            this.extra_layout_data = data;
            return this;
        }

        public Builder addExtraLayoutData(BLEBeaconData data) {
            this.extra_layout_data.add(data);
            return this;
        }

        public Builder setExtra_layout_matching_bytes(String extra_layout_matching_bytes) {
            this.extra_layout_matching_bytes = extra_layout_matching_bytes;
            return this;
        }

        public Builder setExtra_layout_matching_bytes_position
                (String extra_layout_matching_bytes_position) {
            this.extra_layout_matching_bytes_position = extra_layout_matching_bytes_position;
            return this;
        }
        /**
         * Sorts the sections of the parser. The parameters are the description of the parser.
         * They describe the parser in Altbeacon style manner.
         *
         */
        @Nullable
        private String getSortedParser(String matching_bytes,
                                       String matching_bytes_position,
                                       String serviceUUID,
                                       String serviceUUID_position,
                                       String id1_position,
                                       String id2_position,
                                       String id3_position,
                                       String power_position,
                                       String power_offset,
                                       List<String> data_position,
                                       boolean extra_layout)
        {
            List<Integer> pos=new ArrayList<>();
            List<Integer[]> posStartStop=new ArrayList<>();
            List<String> positionsStartStopString = new ArrayList<>();
            List<String> fields_ids = new ArrayList<>();
            Integer[] aux;

            if (serviceUUID_position!=null)
            {
                aux = getNumberFromPosition(serviceUUID_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(serviceUUID_position);
                fields_ids.add(serviceUUID_field_id);
            }
            if (matching_bytes_position!=null)
            {
                aux = getNumberFromPosition(matching_bytes_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(matching_bytes_position);
                fields_ids.add(matching_bytes_field_id);
            }
            if (id1_position!=null)
            {
                aux = getNumberFromPosition(id1_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(id1_position);
                fields_ids.add(id_field_id);
            }
            if (id2_position!=null)
            {
                aux = getNumberFromPosition(id2_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(id2_position);
                fields_ids.add(id_field_id);
            }
            if (id3_position!=null)
            {
                aux = getNumberFromPosition(id3_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(id3_position);
                fields_ids.add(id_field_id);
            }
            if (power_position!=null)
            {
                aux = getNumberFromPosition(power_position);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(power_position);
                fields_ids.add(power_field_id);
            }
            for (String s:data_position)
            {
                aux = getNumberFromPosition(s);
                if (aux[0]==-1)
                    return null;
                pos.add(aux[0]);
                posStartStop.add(aux);
                positionsStartStopString.add(s);
                fields_ids.add(data_field_id);
            }
            int l = pos.size();
            int[] sortedPosArray =  new int[l];
            for (int i=0;i<l;i++)
                sortedPosArray[i]=pos.get(i);

            QuickSorter sorter = new QuickSorter();
            sorter.sort(sortedPosArray);

            int[] indexSortedpos = new int[l];

            index_cycle:
            for (int i=0;i<l;i++)
            {
                for (int u=0;u<l;u++)
                {
                    if (pos.get(u)==sortedPosArray[i]){
                        indexSortedpos[i]=u;
                        continue index_cycle;}
                }
            }

            StringBuilder parser=new StringBuilder();
            if (extra_layout)
                parser.append(extra_layout_field_id).append(comma);
            int index;
            for (int i=0;i<l;i++)
            {

                if (i!=0)
                    parser.append(comma);

                index=indexSortedpos[i];
                switch(fields_ids.get(index))
                {
                    case matching_bytes_field_id:
                        parser.append(matching_bytes_field_id).append(colon)
                                .append(positionsStartStopString.get(index))
                                .append(equal).append(matching_bytes);
                        break;

                    case serviceUUID_field_id:
                        parser.append(serviceUUID_field_id).append(colon)
                                .append(positionsStartStopString.get(index))
                                .append(equal).append(serviceUUID);
                        break;

                    case id_field_id:
                        parser.append(id_field_id).append(colon)
                                .append(positionsStartStopString.get(index));
                        break;

                    case power_field_id:
                        parser.append(power_field_id).append(colon)
                                .append(positionsStartStopString.get(index));
                        if(power_offset!=null)
                            parser.append(colon)
                                    .append(power_offset);
                        break;

                    case data_field_id:
                        parser.append(data_field_id).append(colon)
                                .append(positionsStartStopString.get(index));
                        break;
                }
            }

            return parser.toString();

        }

        /**
         * Parses the {@link String} representing the position of the parser sections.
         * @param position
         */
        private Integer[] getNumberFromPosition(String position)
        {
            int pos_divider=position.indexOf("-");
            int start= 0;
            int stop= 0;
            try {
                start = Integer.parseInt(position.substring(0, pos_divider));
                stop = Integer.parseInt(position.substring(pos_divider+1));
                return new Integer[] {start, stop};
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return new Integer[] {-1};
            }
        }
    }


    /**
     * Class for parsing xml
     */
    public static class XML_handler {

        private static final String TAG = "BBP_XML_handler";


        //region parsing constants sections
        final static int NONE_PARSING = 0;
        final static int FL_PARSER_PARSING = 1;
        final static int TL_DATA_PARSING = 2;
        final static int TL_MATCHINGBYTES_PARSING = 3;
        final static int TL_SERVICEUUID_PARSING = 4;
        final static int TL_BEACONID_PARSING = 5;
        final static int TL_POWER_PARSING = 6;
        final static int FOL_SENSOR_PARSING = 7;
        final static int SL_EXTRA_LAYOUT = 8;
        final static int SL_MAIN_LAYOUT = 9;
        //endregion

        //region parser
        public static List<BLEBeaconParser> parseBLEParsers(Context context) {

            int beacon_id_counter=0;

            List<BLEBeaconParser> bleBeaconParsers = new ArrayList<>();
            BLEBeaconParser.Builder parserBuilder = new BLEBeaconParser.Builder();
            BLEBeaconData.Builder dataBuilder = new BLEBeaconData.Builder();

            String text="";

            File file = new File(Environment.getExternalStorageDirectory(), "bleparsers.xml");

//            Uri uri = Uri.parse
//                    //("content://com.angelo.bleembeddedflasher.fileprovider/bleparsers/bleparsers.xml");
//                            ("content://eu.angel.bleembedded.beacontest.fileprovider/bleparsers/bleparsers.xml");

//            Uri uri= FileProvider.getUriForFile(context,
//                    "eu.angel.bleembedded.beacontest.fileprovider", file);
            InputStream is= null;
            int parsingFirstLayerSection=NONE_PARSING;
            int parsingThirdLayerSection=NONE_PARSING;
            int parsingFourthLayerSection=NONE_PARSING;
            int parsingSecondLayerSection=NONE_PARSING;


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
                                    beacon_id_counter=0;
                                    parsingFirstLayerSection=FL_PARSER_PARSING;
                                    parsingThirdLayerSection=NONE_PARSING;
                                    parsingFourthLayerSection=NONE_PARSING;
                                    parserBuilder=new BLEBeaconParser.Builder();
                                }
                            }else if (parsingFirstLayerSection==FL_PARSER_PARSING)
                            {
                                if (parsingSecondLayerSection==NONE_PARSING)
                                {
                                    if (tagname.equalsIgnoreCase("main_layout")){
                                        parsingThirdLayerSection=NONE_PARSING;
                                        parsingSecondLayerSection= SL_MAIN_LAYOUT;
                                    }
                                    else if (tagname.equalsIgnoreCase("extra_layout")){
                                        parserBuilder.setExtraLayoutBeacon();
                                        parsingThirdLayerSection=NONE_PARSING;
                                        parsingSecondLayerSection= SL_EXTRA_LAYOUT;
                                    }
                                } else if (parsingSecondLayerSection== SL_MAIN_LAYOUT){
                                    if (parsingThirdLayerSection==NONE_PARSING){
                                        if (tagname.equalsIgnoreCase("matching_bytes")) {
                                            parsingThirdLayerSection = TL_MATCHINGBYTES_PARSING;
                                        } else if (tagname.equalsIgnoreCase("service_uuid")) {
                                            parsingThirdLayerSection = TL_SERVICEUUID_PARSING;
                                        } else if (tagname.equalsIgnoreCase("beacon_id")) {
                                            parsingThirdLayerSection = TL_BEACONID_PARSING;
                                        } else if (tagname.equalsIgnoreCase("power")) {
                                            parsingThirdLayerSection = TL_POWER_PARSING;
                                        } else if (tagname.equalsIgnoreCase("data")) {
                                            dataBuilder=new BLEBeaconData.Builder();
                                            parsingThirdLayerSection = TL_DATA_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_DATA_PARSING){
                                        if (tagname.equalsIgnoreCase("sensor")) {
                                            dataBuilder.setData_type(ParsingComplements.DT_SENSOR_STRING);
                                            parsingFourthLayerSection= FOL_SENSOR_PARSING;
                                        }
                                    }
                                } else if (parsingSecondLayerSection== SL_EXTRA_LAYOUT){
                                    if (parsingThirdLayerSection==NONE_PARSING){
                                        if (tagname.equalsIgnoreCase("matching_bytes")) {
                                            parsingThirdLayerSection = TL_MATCHINGBYTES_PARSING;
                                        } else if (tagname.equalsIgnoreCase("service_uuid")) {
                                            parsingThirdLayerSection = TL_SERVICEUUID_PARSING;
                                        } else if (tagname.equalsIgnoreCase("data")) {
                                            dataBuilder=new BLEBeaconData.Builder();
                                            parsingThirdLayerSection = TL_DATA_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_DATA_PARSING){
                                        if (tagname.equalsIgnoreCase("sensor")) {
                                            dataBuilder.setData_type(ParsingComplements.DT_SENSOR_STRING);
                                            parsingFourthLayerSection= FOL_SENSOR_PARSING;
                                        }
                                    }
                                }
                            }
                            break;

                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if (parsingFirstLayerSection==FL_PARSER_PARSING) {
                                if (parsingSecondLayerSection==NONE_PARSING) {
                                    if (parsingThirdLayerSection==NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("parser")) {
                                            parsingFirstLayerSection=NONE_PARSING;
                                            bleBeaconParsers.add(parserBuilder.build());
                                            //Log.d(TAG, "parser_id is: "+text);
                                        }
                                    }
                                } else if (parsingSecondLayerSection== SL_MAIN_LAYOUT) {
                                    if (parsingThirdLayerSection==NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("main_layout")) {
                                            parsingSecondLayerSection=NONE_PARSING;
                                        }
                                        if (text!=null){
                                            if (tagname.equalsIgnoreCase("parser_id")) {
                                                parserBuilder.setParser_identifier(text);
                                                Log.d(TAG, "parser_id is: " + text);
                                            } else if (tagname.equalsIgnoreCase("parser_layout")) {
                                                parserBuilder.setParser_layout(text);
                                                Log.d(TAG, "parser_layout is: " + text);
                                            }
                                        }
                                    } else if (parsingThirdLayerSection== TL_MATCHINGBYTES_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            parserBuilder.setMatching_bytes_position(text);
                                            Log.d(TAG, "matching_bytes pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            parserBuilder.setMatching_bytes(text);
                                            Log.d(TAG, "matching_bytes value is: "+text);
                                        } else if (tagname.equalsIgnoreCase("matching_bytes")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_SERVICEUUID_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            parserBuilder.setServiceUUID_position(text);
                                            Log.d(TAG, "serviceuuid pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            parserBuilder.setServiceUUID(text);
                                            Log.d(TAG, "serviceuuid value is: "+text);
                                        } else if (tagname.equalsIgnoreCase("service_uuid")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_BEACONID_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            if (beacon_id_counter==0){
                                                parserBuilder.setId1_position(text);
                                                beacon_id_counter++;
                                            } else if (beacon_id_counter==1){
                                                parserBuilder.setId2_position(text);
                                                beacon_id_counter++;
                                            } else if (beacon_id_counter==2) {
                                                parserBuilder.setId3_position(text);
                                                beacon_id_counter++;
                                            }
                                            Log.d(TAG, "beacon_id pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("beacon_id")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_POWER_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            parserBuilder.setPower_position(text);
                                            Log.d(TAG, "power pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("offset")) {
                                            parserBuilder.setPower_offset(text);
                                            Log.d(TAG, "power offset is: "+text);
                                        } else if (tagname.equalsIgnoreCase("power")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_DATA_PARSING){
                                        if (parsingFourthLayerSection==NONE_PARSING){
                                            if (tagname.equalsIgnoreCase("position")) {
                                                parserBuilder.addData_position(text);
                                                Log.d(TAG, "data pos is: "+text);
                                            } else if (tagname.equalsIgnoreCase("type")) {
                                                dataBuilder.setData_type(text);
                                            } else if (tagname.equalsIgnoreCase("data")) {
                                                parserBuilder.addData(dataBuilder.build());
                                                parsingThirdLayerSection = NONE_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection== FOL_SENSOR_PARSING){
                                            if (tagname.equalsIgnoreCase("type")) {
                                                dataBuilder.setSensor_type(text);
                                                Log.d(TAG, "type sens is: "+text);
                                            } else if (tagname.equalsIgnoreCase("description")) {
                                                dataBuilder.setSensorDescription(text);
                                            } else if (tagname.equalsIgnoreCase("accuracy")) {
                                                dataBuilder.setSensorAccuracy(text);
                                            } else if (tagname.equalsIgnoreCase("drift")) {
                                                dataBuilder.setSensorDrift(text);
                                            } else if (tagname.equalsIgnoreCase("measurementRange")) {
                                                dataBuilder.setSensorMeasurementRange(text);
                                            } else if (tagname.equalsIgnoreCase("measurementFrequency")) {
                                                dataBuilder.setSensorMeasurementFrequency(text);
                                            } else if (tagname.equalsIgnoreCase("measurementLatency")) {
                                                dataBuilder.setSensorMeasurementLatency(text);
                                            } else if (tagname.equalsIgnoreCase("precision")) {
                                                dataBuilder.setSensorPrecision(text);
                                            } else if (tagname.equalsIgnoreCase("resolution")) {
                                                dataBuilder.setSensorResolution(text);
                                            } else if (tagname.equalsIgnoreCase("responseTime")) {
                                                dataBuilder.setSensorResponseTime(text);
                                            } else if (tagname.equalsIgnoreCase("selectivity")) {
                                                dataBuilder.setSensorSelectivity(text);
                                            } else if (tagname.equalsIgnoreCase("detectionLimit")) {
                                                dataBuilder.setSensorDetectionLimit(text);
                                            } else if (tagname.equalsIgnoreCase("sampleRate")) {
                                                dataBuilder.setSensorSampleRate(text);
                                            } else if (tagname.equalsIgnoreCase("condition")) {
                                                dataBuilder.setSensorCondition(text);
                                            } else if (tagname.equalsIgnoreCase("unit")) {
                                                dataBuilder.setSensorUnit(text);
                                            } else if (tagname.equalsIgnoreCase("sensor")) {
                                                parsingFourthLayerSection=NONE_PARSING;
                                            }
                                        }
                                    }
                                } else if (parsingSecondLayerSection== SL_EXTRA_LAYOUT) {
                                    if (parsingThirdLayerSection==NONE_PARSING) {
                                        if (tagname.equalsIgnoreCase("extra_layout")) {
                                            parsingSecondLayerSection=NONE_PARSING;
                                        }
                                        if (text!=null){
                                            if (tagname.equalsIgnoreCase("parser_layout")) {
                                                parserBuilder.setExtra_layout_parser_layout(text);
                                                Log.d(TAG, "Extra_layout_parser_layout is: " + text);
                                            }
                                        }
                                    } else if (parsingThirdLayerSection== TL_MATCHINGBYTES_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            parserBuilder
                                                    .setExtra_layout_matching_bytes_position(text);
                                            Log.d(TAG, "ex_l_matching_bytes pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("value")) {
                                            parserBuilder.setExtra_layout_matching_bytes(text);
                                            Log.d(TAG, "ex_l_matching_bytes value is: "+text);
                                        } else if (tagname.equalsIgnoreCase("matching_bytes")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_SERVICEUUID_PARSING){
                                        if (tagname.equalsIgnoreCase("position")) {
                                            parserBuilder.setExtra_layout_serviceUUID_position(text);
                                            Log.d(TAG, "serviceuuid pos is: "+text);
                                        } else if (tagname.equalsIgnoreCase("service_uuid")) {
                                            parsingThirdLayerSection = NONE_PARSING;
                                        }
                                    } else if (parsingThirdLayerSection== TL_DATA_PARSING){
                                        if (parsingFourthLayerSection==NONE_PARSING){
                                            if (tagname.equalsIgnoreCase("position")) {
                                                parserBuilder.addExtraLayoutData_position(text);
                                                Log.d(TAG, "data pos is: "+text);
                                            } else if (tagname.equalsIgnoreCase("type")) {
                                                dataBuilder.setData_type(text);
                                            } else if (tagname.equalsIgnoreCase("data")) {
                                                parserBuilder.addExtraLayoutData(dataBuilder.build());
                                                parsingThirdLayerSection = NONE_PARSING;
                                            }
                                        } else if (parsingFourthLayerSection== FOL_SENSOR_PARSING){
                                            if (tagname.equalsIgnoreCase("type")) {
                                                dataBuilder.setSensor_type(text);
                                                Log.d(TAG, "type sens is: "+text);
                                            } else if (tagname.equalsIgnoreCase("description")) {
                                                dataBuilder.setSensorDescription(text);
                                            } else if (tagname.equalsIgnoreCase("accuracy")) {
                                                dataBuilder.setSensorAccuracy(text);
                                            } else if (tagname.equalsIgnoreCase("drift")) {
                                                dataBuilder.setSensorDrift(text);
                                            } else if (tagname.equalsIgnoreCase("measurementRange")) {
                                                dataBuilder.setSensorMeasurementRange(text);
                                            } else if (tagname.equalsIgnoreCase("measurementFrequency")) {
                                                dataBuilder.setSensorMeasurementFrequency(text);
                                            } else if (tagname.equalsIgnoreCase("measurementLatency")) {
                                                dataBuilder.setSensorMeasurementLatency(text);
                                            } else if (tagname.equalsIgnoreCase("precision")) {
                                                dataBuilder.setSensorPrecision(text);
                                            } else if (tagname.equalsIgnoreCase("resolution")) {
                                                dataBuilder.setSensorResolution(text);
                                            } else if (tagname.equalsIgnoreCase("responseTime")) {
                                                dataBuilder.setSensorResponseTime(text);
                                            } else if (tagname.equalsIgnoreCase("selectivity")) {
                                                dataBuilder.setSensorSelectivity(text);
                                            } else if (tagname.equalsIgnoreCase("detectionLimit")) {
                                                dataBuilder.setSensorDetectionLimit(text);
                                            } else if (tagname.equalsIgnoreCase("sampleRate")) {
                                                dataBuilder.setSensorSampleRate(text);
                                            } else if (tagname.equalsIgnoreCase("condition")) {
                                                dataBuilder.setSensorCondition(text);
                                            } else if (tagname.equalsIgnoreCase("unit")) {
                                                dataBuilder.setSensorUnit(text);
                                            } else if (tagname.equalsIgnoreCase("sensor")) {
                                                parsingFourthLayerSection=NONE_PARSING;
                                            }
                                        }
                                    }
                                }
                            }else if(parsingFirstLayerSection==NONE_PARSING)
                            {
                                if (tagname.equalsIgnoreCase("parsers")) {
                                    Log.d(TAG, "end xml");
                                    break xml_scan_while;
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

            return bleBeaconParsers;
        }
        //endregion
    }

}
