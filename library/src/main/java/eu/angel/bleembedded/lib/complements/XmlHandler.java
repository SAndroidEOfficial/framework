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
package eu.angel.bleembedded.lib.complements;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;;
import android.util.Log;
import android.util.Xml;

import org.altbeacon.beacon.Identifier;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.item.Bleresource;
import eu.angel.bleembedded.lib.beacon.BLEBeacon;
import eu.angel.bleembedded.lib.beacon.BLEBeaconCluster;
import eu.angel.bleembedded.lib.beacon.BLEBeaconRegion;
import eu.angel.bleembedded.lib.beacon.BeaconComplements;

/**
 * Class for xmls handling (storage and parsing)
 */
//TODO: the xmls handling should be placed in the class of the object returned by the xml handling
public class XmlHandler {

    private static final String TAG = "XmlHandler";



    //region bleResources xml
    public static List<Bleresource> parseBLEResources(Context context) {

        List<Bleresource> bleresources = new ArrayList<>();
        //Bleresource bleresource=new Bleresource();
        Bleresource.Builder bleresourceBuilder=new Bleresource.Builder();
        String text="";
        Uri uri = Uri.parse("content://com.angelo.bleembeddedflasher.fileprovider/bleresources.xml");
        InputStream is= null;
        try {
            is = context.getContentResolver().openInputStream(uri);
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
                        if (tagname.equalsIgnoreCase("bleresource")) {
                            bleresourceBuilder = new Bleresource.Builder();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("bleresource")) {
                            bleresources.add(bleresourceBuilder.build());
                            Log.d(TAG, "bleresource is: ");
                        } else if (tagname.equalsIgnoreCase("devname")) {
                            bleresourceBuilder.setDevname(text);
                            Log.d(TAG, "devname is: "+text);
                        } else if (tagname.equalsIgnoreCase("devtype")) {
                            bleresourceBuilder.setDevtype(text);
                            Log.d(TAG, "devtype is: "+text);
                        } else if (tagname.equalsIgnoreCase("devversion")) {
                            bleresourceBuilder.setDevversion(text);
                            Log.d(TAG, "devversion is: "+text);
                        } else if (tagname.equalsIgnoreCase("devmacaddress")) {
                            bleresourceBuilder.setDevmacaddress(text);
                            Log.d(TAG, "devmacaddress is: "+text);
                        } else if (tagname.equalsIgnoreCase("devItem")) {
                            bleresourceBuilder.setDevItem(text);
                            Log.d(TAG, "devItem is: "+text);
                        } else if (tagname.equalsIgnoreCase("type")) {
                            bleresourceBuilder.setType(text);
                            Log.d(TAG, "type is: "+text);
                        } else if (tagname.equalsIgnoreCase("cardinality")) {
                            bleresourceBuilder.setCardinality(Integer.parseInt(text));
                            Log.d(TAG, "cardinality is: "+text);
                        } else if (tagname.equalsIgnoreCase("name")) {
                            bleresourceBuilder.setName(text);
                            Log.d(TAG, "name is: "+text);
                        } else if (tagname.equalsIgnoreCase("bleresources")) {
                            Log.d(TAG, "end xml");
                            break xml_scan_while;
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

        return bleresources;
    }


    public static void saveBleresources
            (Context context, List<Bleresource> bleresources) throws IOException, SAXException {

        //For debug purposes
//        File f = new File(Environment.getExternalStorageDirectory(),"bleresources.xml");
//        FileOutputStream myFile = new FileOutputStream(f);

        //TODO:should be handled by ContentProvider
        Uri uri = Uri.parse
                ("content://com.angelo.bleembeddedflasher.fileprovider/bleresources.xml");
        FileOutputStream myFile=
                (FileOutputStream) context.getContentResolver().openOutputStream(uri);
        //TODO: add subsection "device" and "Item" in "bleresource"
        try{

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();


            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument("UTF-8",true);

            xmlSerializer.startTag("", "bleresources");

            String aux;

            for (Bleresource bleresource:bleresources)
            {
                xmlSerializer.startTag("", "bleresource");

                aux=bleresource.getDevname();
                //TODO: bisognerebbe in caso di mancanza di campi essenziali non salvare e lanciare un errore
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "devname");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "devname");
                //}
                aux=bleresource.getDevtype();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "devtype");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "devtype");
                //}
                aux = bleresource.getDevversion();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "devversion");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "devversion");
                //}
                aux = bleresource.getDevmacaddress();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "devmacaddress");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "devmacaddress");
                //}
                aux = bleresource.getDevItem();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "devItem");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "devItem");
                //}
                aux=bleresource.getType();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "type");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "type");
                //}
                aux=Integer.toString(bleresource.getCardinality());
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "cardinality");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "cardinality");
                //}
                aux=bleresource.getName();
                //if (aux!=null) {
                if (aux==null)
                    aux="";
                    xmlSerializer.startTag("", "name");
                    xmlSerializer.text(aux);
                    xmlSerializer.endTag("", "name");
                //}
                xmlSerializer.endTag("","bleresource");

            }

            xmlSerializer.endTag("", "bleresources");
            xmlSerializer.endDocument();

            //writer.toString();
            myFile.write(writer.toString().getBytes());

        }
        catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
            throw new SAXException(e);

        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    public static void saveAndAppendBleresources
            (Context context, List<Bleresource> newBleresources) throws IOException, SAXException
    {
        List<Bleresource> bleresources=parseBLEResources(context);
        for (Bleresource bleresource:newBleresources)
            bleresources.add(bleresource);
        saveBleresources(context, bleresources);
    }

    public static void flushResources(Context context) throws IOException, SAXException
    {
        saveBleresources(context, new ArrayList<Bleresource>());
    }
    //endregion

    //region Cluster xml


    public static List<BLEBeaconCluster> parseBLEBeaconClusters(Context context) {

        final int NONE_PARSING = 0;
        final int BEACON_PARSING = 1;
        final int REGION_PARSING = 2;
        final int CLUSTER_PARSING = 3;

        List<BLEBeaconCluster> bleBeaconClusters = new ArrayList<>();
        //List<BLEBeacon> bleBeacons = new ArrayList<>();
        BLEBeacon.Builder beaconBuilder = new BLEBeacon.Builder();
        BLEBeaconRegion.Builder beaconRegionbuilder = new BLEBeaconRegion.Builder();
        List<BLEBeaconRegion> bleBeaconRegions = new ArrayList<>();
        String bleBeaconClusterUniqueId="";
        String text="";

        File file = new File(Environment.getExternalStorageDirectory(), "blebeaconclusters.xml");
//        Uri uri= FileProvider.getUriForFile(context,
//                "eu.angel.bleembedded.beacontest.blebeaconclusters", file);

//        Uri uri = Uri.parse
//                //("content://com.angelo.bleembeddedflasher.fileprovider/blebeaconclusters/blebeaconclusters.xml");
//        ("content://eu.angel.bleembedded.beacontest.fileprovider/blebeaconclusters/blebeaconclusters.xml");

        InputStream is= null;
        int parsingFirstLayerSection=NONE_PARSING;
        int parsingSecondLayerSection=NONE_PARSING;


        try {
            //is = context.getContentResolver().openInputStream(uri);
            is=new FileInputStream(file);
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
                            if (tagname.equalsIgnoreCase("blebeaconcluster")) {
                                parsingFirstLayerSection=CLUSTER_PARSING;
                            }
                        }else if (parsingFirstLayerSection==CLUSTER_PARSING)
                        {
                            if (parsingSecondLayerSection==NONE_PARSING)
                            {
                                if (tagname.equalsIgnoreCase("beacon")) {
                                    parsingSecondLayerSection=BEACON_PARSING;
                                    beaconBuilder = new BLEBeacon.Builder();
                                }else if (tagname.equalsIgnoreCase("region")) {
                                    parsingSecondLayerSection=REGION_PARSING;
                                }
                            }
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (parsingFirstLayerSection==CLUSTER_PARSING)
                        {
                            if (parsingSecondLayerSection==BEACON_PARSING){
                                if (tagname.equalsIgnoreCase("beacon")) {
                                    parsingSecondLayerSection=NONE_PARSING;
                                    //bleBeacons.add((BLEBeacon) beaconBuilder.build());
                                    bleBeaconRegions.add
                                            (new BLEBeaconRegion.Builder()
                                                    .setTheSingleBLEBeacon
                                                            (beaconBuilder.build())
                                                    .buildNotImplementingRegion());
                                    Log.d(TAG, "beacon is: "+text);
                                }
                                if (text!=null){
                                if (tagname.equalsIgnoreCase("model")) {
                                    beaconBuilder.setModel(BeaconComplements.getModelFromString(text));
                                    Log.d(TAG, "beaconmodel is: "+text);
                                } else if (tagname.equalsIgnoreCase("type")) {
                                    beaconBuilder.setType(BeaconComplements.getTypeFromString(text));
                                    Log.d(TAG, "beacontype is: "+text);
                                } else if (tagname.equalsIgnoreCase("parser")) {
                                    beaconBuilder.setParserIdentifier(text);
                                    Log.d(TAG, "beaconparser is: "+text);
                                } else if (tagname.equalsIgnoreCase("uid")) {
                                    beaconBuilder.setUid(text);
                                    Log.d(TAG, "beaconui is: "+text);
                                } else if (tagname.equalsIgnoreCase("id1")) {
                                    beaconBuilder.setDefinedId1(text);
                                    Log.d(TAG, "beaconid1 is: "+text);
                                } else if (tagname.equalsIgnoreCase("id2")) {
                                    beaconBuilder.setDefinedId2(text);
                                    Log.d(TAG, "beaconid2 is: "+text);
                                } else if (tagname.equalsIgnoreCase("id3")) {
                                    beaconBuilder.setDefinedId3(text);
                                    Log.d(TAG, "beaconid3 is: "+text);
                                } else if (tagname.equalsIgnoreCase("datadescription")) {
                                    beaconBuilder.addDataFieldDescription(text);
                                    Log.d(TAG, "beacondatadescription is: " + text);
                                }
                                }
                            }else if (parsingSecondLayerSection==REGION_PARSING){
                                if (tagname.equalsIgnoreCase("region")) {
                                    parsingSecondLayerSection=NONE_PARSING;
                                    bleBeaconRegions.add(beaconRegionbuilder.buildNotImplementingRegion());
                                    Log.d(TAG, "region is: "+text);
                                }
                                if (text!=null){
                                if (tagname.equalsIgnoreCase("parser")) {
                                    beaconRegionbuilder.addParser(text);
                                    Log.d(TAG, "regionparser is: "+text);
                                } else if (tagname.equalsIgnoreCase("uid")) {
                                    beaconRegionbuilder.setUid(text);
                                    Log.d(TAG, "regionuid is: "+text);
                                } else if (tagname.equalsIgnoreCase("id1")) {
                                    beaconRegionbuilder.setId1(text);
                                    Log.d(TAG, "regionid1 is: "+text);
                                } else if (tagname.equalsIgnoreCase("id2")) {
                                    beaconRegionbuilder.setId2(text);
                                    Log.d(TAG, "regionid2 is: "+text);
                                } else if (tagname.equalsIgnoreCase("id3")) {
                                    beaconRegionbuilder.setId3(text);
                                    Log.d(TAG, "regionid3 is: "+text);
                                }}
                            }else if (parsingSecondLayerSection==NONE_PARSING){
                                if (tagname.equalsIgnoreCase("uid")) {
                                    bleBeaconClusterUniqueId=text;
                                    Log.d(TAG, "bleBeaconClusterUniqueId is: "+text);
                                }else if (tagname.equalsIgnoreCase("blebeaconcluster")) {
                                    parsingFirstLayerSection=NONE_PARSING;
                                    bleBeaconClusters.add
                                            (new BLEBeaconCluster(bleBeaconClusterUniqueId,
                                                    bleBeaconRegions));
                                    Log.d(TAG, "blebeaconcluster added: bleRegions "
                                            +bleBeaconRegions);
                                    bleBeaconRegions=new ArrayList<>();
                                }
                            }
                        }else if(parsingFirstLayerSection==NONE_PARSING)
                        {
                            if (tagname.equalsIgnoreCase("blebeaconclusters")) {
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

        return bleBeaconClusters;
    }

    public static void saveBleBeaconClusters
            (Context context, List<BLEBeaconCluster> bleBeaconClusters)
            throws IOException, SAXException {

        //For debug purposes
//        File f = new File(Environment.getExternalStorageDirectory(),"blebeaconclusters.xml");
//        FileOutputStream myFile = new FileOutputStream(f);

        File file = new File(Environment.getExternalStorageDirectory(), "blebeaconclusters.xml");
//        Uri uri= FileProvider.getUriForFile(context,
//                "eu.angel.bleembedded.beacontest.blebeaconclusters", file);

//        Uri uri = Uri.parse
//                //("content://com.angelo.bleembeddedflasher.fileprovider/blebeaconclusters/blebeaconclusters.xml");
//                        ("content://eu.angel.bleembedded.beacontest.fileprovider/blebeaconclusters/blebeaconclusters.xml");
        FileOutputStream myFile= new FileOutputStream(file);
                //(FileOutputStream) context.getContentResolver().openOutputStream(uri);

        try{

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument("UTF-8",true);

            xmlSerializer.startTag("", "blebeaconclusters");
            BLEBeacon bleBeacon;
            String aux;
            List<String> auxList;

            for (BLEBeaconCluster bleBeaconCluster:bleBeaconClusters)
            {
                xmlSerializer.startTag("", "blebeaconcluster");

                aux=bleBeaconCluster.getUniqueId();
                if (aux==null)
                    aux="";
                xmlSerializer.startTag("", "uid");
                xmlSerializer.text(aux);
                xmlSerializer.endTag("", "uid");

                for (BLEBeaconRegion bleBeaconRegion:bleBeaconCluster.getBleBeaconRegions())
                {
                    if (bleBeaconRegion.isSingleBeaconRegion()) {
                        bleBeacon = bleBeaconRegion.getBleBeacon();
                        xmlSerializer.startTag("", "beacon");
                        aux = BeaconComplements.getStringFromModel(bleBeacon.getModel());
                        if (aux == null)
                            aux = "";
                        xmlSerializer.startTag("", "model");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "model");

                        aux = BeaconComplements.getStringFromType(bleBeacon.getType());
                        if (aux == null)
                            aux = "";
                        xmlSerializer.startTag("", "type");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "type");

                        aux = bleBeacon.getParserIdentifier();
                        if (aux == null)
                            aux = "";
                        xmlSerializer.startTag("", "parser");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "parser");

                        aux = bleBeacon.getUniqueId();
                        if (aux == null)
                            aux = "";
                        xmlSerializer.startTag("", "uid");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "uid");

                        List<Identifier> identifiers = bleBeacon.getIdentifiers();
                        if (identifiers != null) {
                            int idSize = identifiers.size();

                            //TODO: evaluate whether insert a void string or nothing at all
                            if (idSize > 0){
                            aux = bleBeacon.getId1().toString();
                            if (aux != null) {
                                xmlSerializer.startTag("", "id1");
                                xmlSerializer.text(aux);
                                xmlSerializer.endTag("", "id1");
                            }}

                            if (idSize > 1){
                            aux = bleBeacon.getId2().toString();
                            if (aux != null) {
                                xmlSerializer.startTag("", "id2");
                                xmlSerializer.text(aux);
                                xmlSerializer.endTag("", "id2");
                            }}

                            if (idSize > 2){
                            aux = bleBeacon.getId3().toString();
                            if (aux != null) {
                                xmlSerializer.startTag("", "id3");
                                xmlSerializer.text(aux);
                                xmlSerializer.endTag("", "id3");
                            }}
                        }
                        auxList=bleBeacon.getDataFieldDescriptions();

                        for (String dataFieldDescription:auxList)
                        {
                            if (dataFieldDescription==null)
                                dataFieldDescription="";
                            xmlSerializer.startTag("", "datadescription");
                            xmlSerializer.text(dataFieldDescription);
                            xmlSerializer.endTag("", "datadescription");
                        }
                        xmlSerializer.endTag("", "beacon");
                    }
                    else
                    {
                        xmlSerializer.startTag("", "region");
                        auxList=bleBeaconRegion.getRegionsParsers();

                        for (String regionParser:auxList)
                        {
                            if (regionParser==null)
                                regionParser="";
                            xmlSerializer.startTag("", "parser");
                            xmlSerializer.text(regionParser);
                            xmlSerializer.endTag("", "parser");
                        }

                        aux=bleBeaconRegion.getRegion().getUniqueId();
                        if (aux==null)
                            aux="";
                        xmlSerializer.startTag("", "uid");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "uid");

                        aux=bleBeaconRegion.getRegion().getId1().toString();
                        if (aux==null)
                            aux="";
                        xmlSerializer.startTag("", "id1");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "id1");

                        aux=bleBeaconRegion.getRegion().getId2().toString();
                        if (aux==null)
                            aux="";
                        xmlSerializer.startTag("", "id2");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "id2");

                        aux=bleBeaconRegion.getRegion().getId3().toString();
                        if (aux==null)
                            aux="";
                        xmlSerializer.startTag("", "id3");
                        xmlSerializer.text(aux);
                        xmlSerializer.endTag("", "id3");

                        xmlSerializer.endTag("", "region");
                    }
                }
                xmlSerializer.endTag("", "blebeaconcluster");
            }
            xmlSerializer.endTag("", "blebeaconclusters");
            xmlSerializer.endDocument();

            //writer.toString();
            myFile.write(writer.toString().getBytes());
        }
        catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
            throw new SAXException(e);

        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }


}