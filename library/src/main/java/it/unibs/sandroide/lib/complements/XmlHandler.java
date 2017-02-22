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
package it.unibs.sandroide.lib.complements;

import android.content.Context;
import android.net.Uri;
;
import android.util.Log;
import android.util.Xml;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibs.sandroide.lib.BLEContext;
import it.unibs.sandroide.lib.item.Bleresource;

/**
 * Class for xmls handling (storage and parsing)
 */
//TODO: the xmls handling should be placed in the class of the object returned by the xml handling
public class XmlHandler {

    private static final String TAG = "XmlHandler";



    //region bleResources xml
    public static List<Bleresource> parseBLEResources(Context context) {

        List<Bleresource> bleresources = new ArrayList<>();
        try {
            //Bleresource bleresource=new Bleresource();
            Bleresource.Builder bleresourceBuilder=new Bleresource.Builder();
            String text="";
            Uri uri = Uri.parse("content://it.unibs.sandroide.flasher.fileprovider/bleresources.xml");
            InputStream is= null;

            is = context.getContentResolver().openInputStream(uri);
            Log.d(TAG, is.toString());

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

        } catch (FileNotFoundException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
        }

        return bleresources;
    }


    public static void saveBleresources
            (Context context, List<Bleresource> bleresources) throws IOException, SAXException {

        //TODO:should be handled by ContentProvider
        try{
            Uri uri = Uri.parse
                    ("content://it.unibs.sandroide.flasher.fileprovider/bleresources.xml");
            FileOutputStream myFile=
                    (FileOutputStream) context.getContentResolver().openOutputStream(uri);
            //TODO: add subsection "device" and "Item" in "bleresource"

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
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
            throw new SAXException(e);

        } catch (IOException e) {
            BLEContext.displayToastOnMainActivity(e.getMessage());
            e.printStackTrace();
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

    public static void renameDevice
            (Context context, String from, String to) throws IOException, SAXException
    {
        List<Bleresource> existingResources=parseBLEResources(context);
        HashMap<String,List<Bleresource>> mymap = new HashMap<String,List<Bleresource>>();
        String old;
        for (Bleresource bleresource:existingResources) {
            old = bleresource.getDevname();
            if (from.equals(old)) {
                old = to;
                bleresource.setDevname(to);
            }

            if (!mymap.containsKey(old)) {
                mymap.put(old,new ArrayList<Bleresource>());
            }

            mymap.get(old).add(bleresource);
        }
        List<Bleresource> newBleresources = new ArrayList<Bleresource>();
        for (List<Bleresource> l:mymap.values()) {
            for (Bleresource bleresource:l) {
                newBleresources.add(bleresource);
            }
        }

        saveBleresources(context, newBleresources);
    }


    public static void flushResources(Context context) throws IOException, SAXException
    {
        saveBleresources(context, new ArrayList<Bleresource>());
    }
    //endregion

    //region Cluster xml

}