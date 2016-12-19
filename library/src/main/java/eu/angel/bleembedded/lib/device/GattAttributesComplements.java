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

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import eu.angel.bleembedded.lib.item.BLEItem;

/**
 * Service class for handling the resources/description of the Items/devices.
 */
public class GattAttributesComplements {
	private final static String TAG= "GAttrComplements";

    /**
     * Returns the UUID of the required service
     * 
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device for retrieving the right UUID of the service
     * @param service {@link String} service required
     * 
     * 
     * @return the UUID of the service. {@link String}
     * 
     */
    
    public static String UUIDFromServCharactString
        (DevicesDescriptorNew devicesDescriptorNew, String service)
    {
        return devicesDescriptorNew.getStToUUID().get(service);
    }

    /**
     * Returns the name of the service/characteristics identified by the UUID
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device for retrieving the right UUID of the service
     * @param uuid {@link String} uuid of the service/characteristic required
     *
     *
     * @return the name of the service/characteristic required. {@link String}
     *
     */
    public static String getStringFromUUID(DevicesDescriptorNew devicesDescriptorNew, String uuid)
    {
        return devicesDescriptorNew.getUUIDTost().get(uuid);
    }

    /**
     * Returns the name of the service/characteristics identified by the UUID. It returns the defaultName
     * if the UUID doesn't correspond to any known service/characteristic
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param uuid {@link String} uuid of the service/characteristic required
     * @param defaultName the name returned if  the UUID doesn't correspond
     *                    to any known service/characteristic
     *
     *
     * @return the name of the service/characteristic required. {@link String}
     *
     */
    public static String getStringFromUUIDWithDefault
            (DevicesDescriptorNew devicesDescriptorNew, String uuid, String defaultName)
    {
        String ret=getStringFromUUID(devicesDescriptorNew, uuid);
        if (ret==null)
            ret=defaultName;

        return ret;
    }

    /**
     * Returns the cardinality of the required item.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param devItem {@link String} name of the Item
     *
     *
     * @return cardinality of the item
     *
     */
    public static int getDevItemCardinality(DevicesDescriptorNew devicesDescriptorNew, String devItem)
    {
        return devicesDescriptorNew.getDevItemCardinality().get(devItem);
    }

    /**
     * Returns the characteristics required for handling the item.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param Item {@link String} name of the Item
     *
     *
     * @return array of the mandatory characteristics
     *
     */
    public static String[] getCharacteristicsMandatoryForItem
            (DevicesDescriptorNew devicesDescriptorNew, String Item)
    {
        return devicesDescriptorNew.getMandatoryCharacteristicsForDevItem().get(Item);
    }

    /**
     * Returns the services and characteristics required for handling the item.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param Item {@link String} name of the Item
     *
     *
     * @return HashMap of the mandatory services with associated the related mandatory characteristics
     *
     */
    public static HashMap<String, String[]> getServicesCharacteristicsMandatoryForDevItem
            (DevicesDescriptorNew devicesDescriptorNew, String Item)
    {
        return devicesDescriptorNew.getMandatoryServicesCharacteristicsForDevItem().get(Item);
    }

    /**
     * Returns the services and characteristics required for handling the type of the deviec.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     *
     *
     * @return HashMap of the mandatory services with associated the related mandatory characteristics
     *
     */
    public static HashMap<String, HashMap<String, String[]>> getServicesCharacteristicsMandatoryForDevType
            (DevicesDescriptorNew devicesDescriptorNew)
    {
        return devicesDescriptorNew.getMandatoryServicesCharacteristicsForDevItem();
    }

    /**
     * Returns the characteristics required for handling the type of the deviec.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     *
     *
     * @return HashMap of the mandatory services with associated the related mandatory characteristics
     *
     */
    @Deprecated
    public static HashMap<String, String[]> getCharacterisitcsMandatoryForDevType
            (DevicesDescriptorNew devicesDescriptorNew)
    {
        return devicesDescriptorNew.getMandatoryCharacteristicsForDevItem();
    }

    /**
     * Returns the characteristics required for handling the type of the device.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param devItem the item of the device required
     *
     *
     * @return the bletype (as defined by the Library) of the item
     *
     */
    public static String getItemTypeFromDevItem
            (DevicesDescriptorNew devicesDescriptorNew, String devItem)
    {
        return devicesDescriptorNew.getDevItemsToItemType().get(devItem);
    }


    /**
     * Returns the item of the device, which requires the characteristics passad as parameter.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param characteristics characteristics which identify the required item
     *
     *
     * @return the item which requires the characteristics as mandatory
     *
     */
    @Deprecated
    public static List<String> characteristicsToBLEDevItems
            (DevicesDescriptorNew devicesDescriptorNew, List<String> characteristics)
    {
        HashMap<String, String[]> mandatoryAttributeForItem=
                getCharacterisitcsMandatoryForDevType(devicesDescriptorNew);
        List<String> items= new ArrayList<>();
        String[] mandatoryCahrs;
        mandatoryAttributeForItemFor:
        for (String mKey:mandatoryAttributeForItem.keySet())
        {
            mandatoryCahrs=mandatoryAttributeForItem.get(mKey);
            mandatoryCahrsFor:
            for (String mandatoryCahr:mandatoryCahrs)
            {
                for (String charact:characteristics)
                {
                    if (charact.equalsIgnoreCase(mandatoryCahr))
                        continue mandatoryCahrsFor;
                }
                continue mandatoryAttributeForItemFor;
            }

            items.add(mKey);
        }

        return items;
    }

    /**
     * Returns the item of the device, which requires the services and related characteristics passed as parameter.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param serviceAndCar HAshMap of service and related characteristics which identify the required item
     *
     *
     * @return the item which requires the services and characteristics as mandatory
     *
     */
    public static List<String> serviceAndCharacteristicsToBLEDevItems
            (DevicesDescriptorNew devicesDescriptorNew,
             HashMap<String, HashMap<String, BluetoothGattCharacteristic>> serviceAndCar)
    {
        HashMap<String, HashMap<String, String[]>> mandatoryServiceCharacteristics=
                getServicesCharacteristicsMandatoryForDevType(devicesDescriptorNew);
        List<String> items= new ArrayList<>();
        HashMap<String, String[]> mandatoryServCahrs;
        mandatoryAttributeForItemFor:
        for (String mKey:mandatoryServiceCharacteristics.keySet())
        {
            String serv=null;
            for (String mKey2:mandatoryServiceCharacteristics.get(mKey).keySet())
            {
                if (serviceAndCar.get(mKey2)!=null)
                    serv=mKey2;
            }
            if (serv!=null)
            {
                mandatoryServCahrs=mandatoryServiceCharacteristics.get(mKey);
                for (String mKey3:mandatoryServCahrs.keySet())
                {
                    String[] mandatoryCahrs=mandatoryServCahrs.get(mKey3);
                    mandatoryCahrsFor:
                    for (String mandatoryCahr:mandatoryCahrs)
                    {
                        for (String charact:serviceAndCar.get(serv).keySet())
                        {
                            if (charact.equalsIgnoreCase(mandatoryCahr))
                                continue mandatoryCahrsFor;
                        }
                        continue mandatoryAttributeForItemFor;
                    }

                    items.add(mKey);
                }

            }

        }
        return items;
    }

    /**
     * Returns the item of the device, which requires the characteristic passed as parameter.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param charact characteristic which identifies the required item
     *
     *
     * @return the item which requires the characteristic as mandatory
     *
     */
    public static List<String> characteristicToBLEDevItems
            (DevicesDescriptorNew devicesDescriptorNew, UUID charact)
    {
        List<String> items=new ArrayList<>();
        HashMap<String, HashMap<String, String[]>> mandatoryServiceCharacteristicsForType=
                getServicesCharacteristicsMandatoryForDevType(devicesDescriptorNew);
        for (String mKey:mandatoryServiceCharacteristicsForType.keySet()){
            HashMap<String, String[]> mandatoryServiceAttribute=
                    mandatoryServiceCharacteristicsForType.get(mKey);
            for (String mKey2:mandatoryServiceAttribute.keySet()){
                String[] characts=mandatoryServiceAttribute.get(mKey2);
                for (String charactStr:characts)
                {
                    if (charactStr.equalsIgnoreCase(charact.toString())){
                        items.add(mKey);
                    }
                }
            }
        }
        return items;
    }

    /**
     * Returns the {@link eu.angel.bleembedded.lib.device.read.BLEReadableCharacteristic} name
     * with the related {@link eu.angel.bleembedded.lib.beacon.BLEBeaconCluster} name associated with
     * their group.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param Item the required item
     *
     *
     * @return the HashMap of {@link eu.angel.bleembedded.lib.device.read.BLEReadableCharacteristic} name
     * with the related {@link eu.angel.bleembedded.lib.beacon.BLEBeaconCluster} name associated with
     * their group.
     *
     *
     */
    public static HashMap<String, String[][]> getReadableCharacteristicsAndClustersPlusGroupForDevItem
            (DevicesDescriptorNew devicesDescriptorNew, String Item)
    {
        return devicesDescriptorNew.getReadableCharacteristicsAndClustersPlusGroupForDevItem().get(Item);
    }

    /**
     * Returns the array of {@link eu.angel.bleembedded.lib.device.action.BLEAction} names
     * of the Item.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param Item the required item
     *
     *
     * @return array of the {@link eu.angel.bleembedded.lib.device.action.BLEAction} names associated
     * with the Item
     *
     *
     */
    public static String[] getActionsForDevItem
            (DevicesDescriptorNew devicesDescriptorNew, String Item)
    {
        return devicesDescriptorNew.getActionsForDevItem().get(Item);
    }

    /**
     * Returns the array of {@link eu.angel.bleembedded.lib.device.action.BLEInit} names
     * of the Item.
     *
     * @param devicesDescriptorNew {@link DevicesDescriptorNew} device descriptor
     *             of the BLE device
     * @param Item the required item
     *
     *
     * @return array of the {@link eu.angel.bleembedded.lib.device.action.BLEInit} names associated
     * with the Item
     *
     *
     */
    public static String getInitForDevItem
            (DevicesDescriptorNew devicesDescriptorNew, String Item)
    {
        return devicesDescriptorNew.getInitForDevItem().get(Item);
    }

    //funzione che verifica se è disponibile la characteristic che ritorna il nome del dispositivo
    //TODO:GENERAL_LIB workaround for devices without GAP name, not yet implemented in general lib
    //Up to this release (V 0.1)the functionality of this method in the library is overcome
    public static boolean isCharacteristicNameAvailable(String name)
    {
        //String name=devicesDescriptorNew.getDeviceType();
        if ((name!=null)&&(name.length()>0)){
        switch(name)
        {
            case "LAPIS BLE SLD":
                return false;
        }

        return true;}
        else
            return true;
    }
}





