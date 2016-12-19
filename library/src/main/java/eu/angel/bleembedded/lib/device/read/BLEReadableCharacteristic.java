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

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.angel.bleembedded.lib.data.BLEDeviceData;

/**
 * Class for handling the readable characteristics
 */
public class BLEReadableCharacteristic {

    UUID uuid;
    String name;
    String service_name;
    //DeviceDataParser parser;

    //Device collector parsers and list of Cluster are in the same order, otherwise the reading doesn't work
    DeviceDataParserCollector deviceDataParserCollector;

    //List of Cluster related to the different sub parser of the Parser
    List<List<BLEDeviceDataCluster>> bLEDeviceDataClustersList;


    //TODO: rootitem/item should be an information given by the data (retrieved by the parser)
    String rootItem;

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getService_name() {
        return service_name;
    }

    /**
     * Constructor
     * @param uuid of the characteristic
     * @param name of the characteristic
     * @param deviceDataParserCollector {@link DeviceDataParserCollector} which describes the parsing
     *                                  of the characteristic message
     * @param bLEDeviceDataClustersList list of list of {@link BLEDeviceDataCluster}s. The first list lavel
     *                                  gather the list of {@link BLEDeviceDataCluster}s of the
     *                                  {@link DeviceDataParser} owned by the {@link DeviceDataParserCollector}.
     *                                  The the list of {@link BLEDeviceDataCluster}s and the related
     *                                  {@link DeviceDataParser} are placed at the same index of the
     *                                  their own list
     * @param rootItem
     *
     */
    BLEReadableCharacteristic (UUID uuid, String name, String service_name,
                               DeviceDataParserCollector deviceDataParserCollector,
                               List<List<BLEDeviceDataCluster>> bLEDeviceDataClustersList,
                               String rootItem){
        this.uuid=uuid;
        this.name=name;
        this.service_name=service_name;
//        this.parser=parser;
//        this.bleDeviceDataClusters=bleDeviceDataClusters;
        this.deviceDataParserCollector=deviceDataParserCollector;
        this.bLEDeviceDataClustersList=bLEDeviceDataClustersList;
        this.rootItem=rootItem;
    }

    /**
     * Updates the value of the {@link BLEDeviceData}s owned by the {@link BLEDeviceDataCluster}
     * @param bytes message read for updating {@link BLEDeviceData}s
     *
     */
    public void read(byte[] bytes){

        deviceDataParserCollector.updateDataFromByteArray(bLEDeviceDataClustersList, bytes);

    }


    /**
     * Clones the {@link BLEReadableCharacteristic}
     * @return the cloned {@link BLEReadableCharacteristic}
     *
     */
    public BLEReadableCharacteristic clone(){
        BLEReadableCharacteristic bleReadableCharacteristic=
                new BLEReadableCharacteristic
                        (uuid, name, service_name, deviceDataParserCollector,
                            deviceDataParserCollector.cloneListOfDeviceDataClusters(), rootItem);
        return bleReadableCharacteristic;
    }

    public List<List<BLEDeviceDataCluster>> getBLEDeviceDataClustersList() {
        return bLEDeviceDataClustersList;
    }

    public static class Builder{

        private UUID uuid;
        private String name;
        private String service_name;
        DeviceDataParserCollector deviceDataParserCollector;
        List<List<BLEDeviceDataCluster>> BLEDeviceDataClustersList=new ArrayList<>();
        //TODO: rootitem/item should be an information given by the data (retrieved by the parser)
        String rootItem;

        @Nullable
        public BLEReadableCharacteristic build(){
            if ((uuid!=null)&&(name!=null)&&(deviceDataParserCollector!=null))
                return new BLEReadableCharacteristic
                        (uuid, name, service_name, deviceDataParserCollector,
                                BLEDeviceDataClustersList, rootItem);
            else
                return null;
        }

        public Builder setUUID(String uuid) {
            this.uuid = UUID.fromString(uuid);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDeviceDataParserCollector
                (DeviceDataParserCollector deviceDataParserCollector) {
            this.deviceDataParserCollector = deviceDataParserCollector;
            return this;
        }

        public Builder setBLEDeviceDataClustersList
                (List<List<BLEDeviceDataCluster>> BLEDeviceDataClustersList) {
            this.BLEDeviceDataClustersList = BLEDeviceDataClustersList;
            return this;
        }

        public Builder addBLEDeviceDataClusters(List<BLEDeviceDataCluster> BLEDeviceDataClusters) {
            this.BLEDeviceDataClustersList.add(BLEDeviceDataClusters);
            return this;
        }

        public Builder setRootItem(String rootItem) {
            this.rootItem = rootItem;
            return this;
        }

        public Builder setService_name(String service_name) {
            this.service_name = service_name;
            return this;
        }
    }


}
