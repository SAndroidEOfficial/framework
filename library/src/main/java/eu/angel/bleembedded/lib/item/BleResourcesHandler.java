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
package eu.angel.bleembedded.lib.item;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.device.DevicesDescriptorNew;
import eu.angel.bleembedded.lib.device.GattAttributesComplements;

/**
 * Handler for the {@link Bleresource}.
 */
public class BleResourcesHandler {

    public static void storeAllDevResources(Context context, String devName, DevicesDescriptorNew devicesDescriptorNew, String devVersion, String devMacAddress,
                                        List<String> characteristics)
    {
//        List<String> devItems=BLEItemDescriptor.characteristicsToBLEDevItems(characteristics,
//                GattAttributesComplements.getCharacterisitcsMandatoryForDevType(devType));

        List<String> devItems=GattAttributesComplements.characteristicsToBLEDevItems
                (devicesDescriptorNew, characteristics);
        List<Bleresource> bleresources=new ArrayList<>();
        for (String devItem:devItems)
        {
            Bleresource.Builder bleresourceBuilder=new Bleresource.Builder();
            bleresourceBuilder.setDevname(devName);
            bleresourceBuilder.setDevtype(devicesDescriptorNew.getDeviceType());
            bleresourceBuilder.setDevversion(devVersion);
            bleresourceBuilder.setDevmacaddress(devMacAddress);
            bleresourceBuilder.setType(GattAttributesComplements
                    .getItemTypeFromDevItem(devicesDescriptorNew, devItem));
            bleresourceBuilder.setDevItem(devItem);
            bleresourceBuilder.setCardinality(GattAttributesComplements
                    .getDevItemCardinality(devicesDescriptorNew, devItem));
            bleresourceBuilder.setName(devName+"_"+devItem);
            bleresources.add(bleresourceBuilder.build());
        }
        try {
            XmlHandler.saveAndAppendBleresources(context, bleresources);
        } catch (FileNotFoundException e) {
            //TODO: evaluate whether throwing exception
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores the {@link Bleresource} using descriptors to define the attributes
     * @param context
     * @param devName devName of the {@link Bleresource}
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} retrieved from the devices.xml file,
     *                              which describes some attributes of the resource
     * @param devVersion devVersion of the {@link Bleresource}
     * @param devMacAddress devMacAddress of the {@link Bleresource}
     * @param service used for retrieving the {@link BLEItem} from the {@link DevicesDescriptorNew}
     *                object
     */
    public static void storeAllDevResources2(Context context, String devName, DevicesDescriptorNew devicesDescriptorNew, String devVersion, String devMacAddress,
                                             HashMap<String, HashMap<String, BluetoothGattCharacteristic>> service)
    {
//        List<String> devItems=BLEItemDescriptor.characteristicsToBLEDevItems(characteristics,
//                GattAttributesComplements.getCharacterisitcsMandatoryForDevType(devType));

        List<String> devItems=GattAttributesComplements.serviceAndCharacteristicsToBLEDevItems
                (devicesDescriptorNew, service);
        List<Bleresource> bleresources=new ArrayList<>();
        for (String devItem:devItems)
        {
            Bleresource.Builder bleresourceBuilder=new Bleresource.Builder();
            bleresourceBuilder.setDevname(devName);
            bleresourceBuilder.setDevtype(devicesDescriptorNew.getDeviceType());
            bleresourceBuilder.setDevversion(devVersion);
            bleresourceBuilder.setDevmacaddress(devMacAddress);
            bleresourceBuilder.setType(GattAttributesComplements
                    .getItemTypeFromDevItem(devicesDescriptorNew, devItem));
            bleresourceBuilder.setDevItem(devItem);
            bleresourceBuilder.setCardinality(GattAttributesComplements
                    .getDevItemCardinality(devicesDescriptorNew, devItem));
            bleresourceBuilder.setName(devName+"_"+devItem);
            bleresources.add(bleresourceBuilder.build());
        }
        try {
            XmlHandler.saveAndAppendBleresources(context, bleresources);
        } catch (FileNotFoundException e) {
            //TODO: evaluate whether throwing exception
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Bleresource> getAllDevResources(Context context)
    {
        return XmlHandler.parseBLEResources(context);
    }

    public static Bleresource getItemDescriptorFromBleresources(Context context, String itemName)
    {
        List<Bleresource> bleresources=getAllDevResources(context);
        for (Bleresource bleresource:bleresources)
        {
            if (bleresource.getName().equalsIgnoreCase(itemName))
                return bleresource;
        }
        return null;
    }
}
