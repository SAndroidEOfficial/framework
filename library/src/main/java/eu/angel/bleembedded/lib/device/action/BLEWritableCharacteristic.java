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
package eu.angel.bleembedded.lib.device.action;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the characteristics writable on the remote device
 */
public class BLEWritableCharacteristic {

    private String name;
    private String service_name;
    private List<BLEMessageModel> bleMessageModels;
    private UUID uuid;

    /**
     * Constructor
     * @param name of the characteristic
     * @param service_name name of the service owner of the characteristic
     * @param uuid of the characteristic
     * @param bleMessageModels list of {@link BLEMessageModel} which defines messages configuration
     *                         to be sent to the remote device
     */
    private BLEWritableCharacteristic(String name, String service_name, UUID uuid,
                                      List<BLEMessageModel> bleMessageModels){
        this.name=name;
        this.bleMessageModels=bleMessageModels;
        this.service_name=service_name;
        this.uuid=uuid;
    }

    public String getName() {
        return name;
    }

    public String getService_name() {
        return service_name;
    }

    /**
     * Gets the {@link BLEMessageModel} required
     * @param model_id id of the required {@link BLEMessageModel}
     * @return the {@link BLEMessageModel} required if a match is found, null otherwise
     */
    @Nullable
    public BLEMessageModel getModel(String model_id){
        for (BLEMessageModel bleMessageModel:bleMessageModels){
            if (bleMessageModel.getId().equalsIgnoreCase(model_id)){
                return bleMessageModel;
            }
        }
        return null;
    }

    /**
     * Clone the {@link BLEWritableCharacteristic}
     */
    public BLEWritableCharacteristic clone(){
        List<BLEMessageModel> bleMessageModels=new ArrayList<>();
        for (BLEMessageModel bleMessageModel:this.bleMessageModels){
            bleMessageModels.add(bleMessageModel.clone());
        }
        BLEWritableCharacteristic bleWritableCharacteristic=
                new BLEWritableCharacteristic
                        (name, service_name, uuid,
                                bleMessageModels);
        return bleWritableCharacteristic;
    }

    public static class Builder{

        String name;
        private String service_name;
        List<BLEMessageModel> bleMessageModels=new ArrayList<>();
        private String uuid;

        public BLEWritableCharacteristic build(){
            return new BLEWritableCharacteristic(name,service_name, UUID.fromString(uuid), bleMessageModels);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBleMessageModels(List<BLEMessageModel> bleMessageModels) {
            this.bleMessageModels = bleMessageModels;
            return this;
        }

        public Builder addBleMessageModel(BLEMessageModel bleMessageModel) {
            this.bleMessageModels.add(bleMessageModel);
            return this;
        }

        public Builder setService_name(String service_name) {
            this.service_name = service_name;
            return this;
        }

        public Builder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
    }

}
