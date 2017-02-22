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
package it.unibs.sandroide.lib.device.action;

import java.util.ArrayList;
import java.util.List;

import it.unibs.sandroide.lib.data.BLEDataConversionComplements;
import it.unibs.sandroide.lib.data.BLEDeviceData;

/**
 * Class describing the structure of the message
 */
public class BLEMessageModel {

    private String id;
    private byte[] message;
    private List<BLEDeviceData> bleDeviceDataList;

    public String getId() {
        return id;
    }

    /**
     * Constructor
     * @param id identifies the {@link BLEMessageModel}
     * @param message byte array which makes up the structure of the message
     * @param bleDeviceDataList variable fields of the message
     */
    private BLEMessageModel (String id, byte[] message, List<BLEDeviceData> bleDeviceDataList){
        this.id=id;
        this.message=message;
        this.bleDeviceDataList=bleDeviceDataList;
    }

    /**
     * Gets the message described by the {@link BLEMessageModel}
     * @param input list of Floats which updates the values of the variable fields of the
     *              message ({@link BLEMessageModel#bleDeviceDataList})
     */
    public byte[] getMessage(List<Float> input)
    {
        //TODO
        //warning: data and input has to be in the same order
        for (int i=0;i<bleDeviceDataList.size();i++){
            if (i<input.size()){
                bleDeviceDataList.get(i).setOutputValue(input.get(i));
            }
            else
                bleDeviceDataList.get(i).setValueToDefault();
            if(!BLEDataConversionComplements.setMessageValue(message,
                    (int)bleDeviceDataList.get(i).getValue(),
                    bleDeviceDataList.get(i).getFormat(),
                    bleDeviceDataList.get(i).getStartOffset())){
                //TODO: throw exception position and format do not match
            }
        }

        return message;
    }

    /**
     * Clones the {@link BLEMessageModel}
     */
    public BLEMessageModel clone(){
        List<BLEDeviceData> bleDeviceDataList=new ArrayList<>();
        for(BLEDeviceData bleDeviceData:this.bleDeviceDataList)
            bleDeviceDataList.add(bleDeviceData.clone());
        byte[] message=new byte[this.message.length];
        for (int i=0;i<this.message.length;i++)
            message[i]=this.message[i];

        return new BLEMessageModel(id, message, bleDeviceDataList);
    }

    public static class Builder{

        private String id;

        private List<BLEDeviceData> bleDeviceDataList=new ArrayList<>();
        private List<BLEDeviceData> bleDeviceStaticDataList=new ArrayList<>();


        public BLEMessageModel build(){
            int length=0;
            for (BLEDeviceData bleDeviceData:bleDeviceDataList)
                if (bleDeviceData.getStopOffset()>length)
                    length=bleDeviceData.getStopOffset();
            for (BLEDeviceData bleDeviceData:bleDeviceStaticDataList)
                if (bleDeviceData.getStopOffset()>length)
                    length=bleDeviceData.getStopOffset();
            byte[] message=new byte[length+1];

            for (BLEDeviceData bleDeviceData:bleDeviceStaticDataList){
                if(!BLEDataConversionComplements.setMessageValue(message, (int)bleDeviceData.getDefaultValue(),
                        bleDeviceData.getFormat(), bleDeviceData.getStartOffset())){
                    //TODO: throw exception position and format do not match
                }
            }


            return new BLEMessageModel(id, message, bleDeviceDataList);
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setBleDeviceDataList(List<BLEDeviceData> bleDeviceDataList) {
            this.bleDeviceDataList = bleDeviceDataList;
            return this;
        }

        public Builder setBleDeviceStaticDataList(List<BLEDeviceData> bleDeviceStaticDataList) {
            this.bleDeviceStaticDataList = bleDeviceStaticDataList;
            return this;
        }

        public Builder addBleDeviceData(BLEDeviceData bleDeviceData) {
            this.bleDeviceDataList.add(bleDeviceData);
            return this;
        }

        public Builder addBleDeviceStaticData(BLEDeviceData bleDeviceStaticData) {
            this.bleDeviceStaticDataList.add(bleDeviceStaticData);
            return this;
        }
    }

}
