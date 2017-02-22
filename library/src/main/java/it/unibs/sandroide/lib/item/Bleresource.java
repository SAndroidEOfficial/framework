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
package it.unibs.sandroide.lib.item;

/**
 * The class representing the {@link BLEItem} resource description.
 */
public class Bleresource {

    final static String endOfField="; ;";
    final static String startOfline="-->";

    String devname;
    String devtype;
    String devversion;
    String devmacaddress;
    String devItem;
    String type;
    int cardinality;
    String name;

    /**
     * Constructor of the {@link Bleresource}
     * @param devname nickname of the device owner of the resource. It is assigned by the user and is used for differentiating
     *                the first name the resources of the devices of the same type
     * @param devtype type of the device. Each different device and FW has to be identified by a different type,
     *                because the type points to a descriptor in the xml file
     * @param devversion used for differentiate the version of the FW (it's not used up to now.
     * @param devmacaddress the MAC address of the device is used for connecting to the device.
     * @param devItem name of the resource assigned by the developer of the xml file (used for
     *                differentiating resources of the same type owned by the same device).
     * @param type type of the {@link BLEItem}. It defines the class which extends the {@link BLEItem}
     *             class.
     * @param cardinality cardinality of the single resource on the device owner. Defined by the developer
     *                    of the xml file.
     * @param name name used to point the resource by the means of the appropriate method.
     */
    private Bleresource(String devname,
            String devtype,
            String devversion,
            String devmacaddress,
            String devItem,
            String type,
            int cardinality,
            String name){
        this.devname=devname;
        this.devtype=devtype;
        this.devversion=devversion;
        this.devmacaddress=devmacaddress;
        this.devItem=devItem;
        this.type=type;
        this.cardinality=cardinality;
        this.name=name;

    }


    public String getDevname() {
        return devname;
    }

    public String getDevtype() {
        return devtype;
    }

    public String getDevversion() {
        return devversion;
    }

    public String getDevmacaddress() {
        return devmacaddress;
    }

    public String getDevItem() {
        return devItem;
    }

    public String getType() {
        return type;
    }

    public int getCardinality() {
        return cardinality;
    }

    public String getName() {
        return name;
    }

    public void setDevname(String str) {
        this.devname = str;
        this.name = this.devname+"_"+this.devItem;
    }

    @Override
    public String toString() {
        return startOfline+devmacaddress+endOfField+devname+endOfField+
                devversion+endOfField+type+endOfField+ Integer.toString(cardinality)+endOfField
                +name+endOfField;
    }

    public static class Builder{
        String devname;
        String devtype;
        String devversion;
        String devmacaddress;
        String devItem;
        String type;
        int cardinality;
        String name;

        public Bleresource build(){
            return new Bleresource(devname,
            devtype,
            devversion,
            devmacaddress,
            devItem,
            type,
            cardinality,
            name);
        }

        public Builder setDevname(String devname) {
            this.devname = devname;
            return this;
        }

        public Builder setDevtype(String devtype) {
            this.devtype = devtype;
            return this;
        }

        public Builder setDevversion(String devversion) {
            this.devversion = devversion;
            return this;
        }

        public Builder setDevmacaddress(String devmacaddress) {
            this.devmacaddress = devmacaddress;
            return this;
        }

        public Builder setDevItem(String devItem) {
            this.devItem = devItem;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setCardinality(int cardinality) {
            this.cardinality = cardinality;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }
    }
}
