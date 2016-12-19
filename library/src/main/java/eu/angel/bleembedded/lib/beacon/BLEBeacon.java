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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.angel.bleembedded.lib.data.BLEBeaconData;

/**
 * Class extending Altbeacon Beacon for SAndroidE integration.
 */
public class BLEBeacon extends Beacon implements Parcelable, Serializable{

    public static final int ALL_BEACONS = 0;
    public static final int UNKNOWN_BEACON = 1;
    public static final int ALL_EDDY_STONE_TYPE = 2;
    public static final int EDDY_STONE_UID = 3;
    public static final int EDDY_STONE_UID_AND_TELEMETRY = 4;
    public static final int EDDY_STONE_URL = 5;
    public static final int I_BEACON = 6;
    public static final int ALT_BEACON = 7;
    public static final int EDDY_STONE_URL_AND_TELEMETRY = 8;
    public static final int URI_BEACON = 9;



    public static final int MODEL_UNKNOWN = 0;
    public static final int MODEL_ESTIMOTE = 1;
    public static final int MODEL_GYMBAL = 2;



    //BLEBeacon attribute
    private int type;
    private int model;
    //define the last reception of the beacon;
    //private long timestamp;
    //private String parser;
    protected List<Identifier> mDefinedIdentifiers;
    protected List<String> mDataFieldDescriptions;
    protected String uniqueId;
    //private String uidClusterOwner;
    protected List<BLEBeaconData> bleBeaconDataList=new ArrayList<>();
    protected List<BLEBeaconData> bleBeaconExtraDataList=new ArrayList<>();

    public int getType() {
        return type;
    }
    public int getModel() {
        return model;
    }
//    public String getParser() {
//        return parser;
//    }

    @Nullable
    public String getUniqueId() {
        return uniqueId;
    }

    public List<String> getDataFieldDescriptions() {
        return Collections.unmodifiableList(this.mDataFieldDescriptions);
    }
//    public String getUidClusterOwner() {
//        return uidClusterOwner;
//    }


    /**
     * Copy constructor from base class
     * @param beacon {@link Beacon} seed of this Object
     * @param model model of the Beacon. The conversion can be found in {@link BeaconComplements}
     * @param type type of the Beacon
     * @param parser parser structure in the Altbeacon parser structure
     * @param mDefinedIdentifiers list of {@link Identifier} of the Beacon
     * @param mDataFieldDescriptions human readable String describing the data field with
     *                               the same index in the respective list
     * @param uniqueId the Id of the Beacon
     *
     */
    protected BLEBeacon(Beacon beacon, int model, int type, String parser,
                        List<Identifier> mDefinedIdentifiers,
                        List<String> mDataFieldDescriptions, String uniqueId) {
        super();
        this.mBluetoothAddress = beacon.getBluetoothAddress();
        this.mIdentifiers = beacon.getIdentifiers();
        this.mBeaconTypeCode = beacon.getBeaconTypeCode();
        this.mDataFields = beacon.getDataFields();
        this.mDistance = beacon.getDistance();
        this.mRssi = beacon.getRssi();
        this.mTxPower = beacon.getTxPower();
        this.model = model;
        this.type = type;
        this.mParserIdentifier = parser;
        this.mDefinedIdentifiers=mDefinedIdentifiers;
        this.mDataFieldDescriptions=mDataFieldDescriptions;
        this.uniqueId=uniqueId;
        this.mExtraDataFields=beacon.getExtraDataFields();
        setBLEBeaconData(mParserIdentifier);
        upDateBLEBeaconData(beacon);
        //this.uidClusterOwner = uidClusterOwner;
    }

    public List<BLEBeaconData> getBLEBeaconData(){
        return Collections.unmodifiableList(this.bleBeaconDataList);
    }

    /**
     * Updates the data fields of the BleBeacon
     * @param beacon {@link Beacon} received by Altbeacon library
     *
     */
    protected void upDateBLEBeaconData(Beacon beacon) throws BeaconParserException{
        if (bleBeaconDataList.size()!=beacon.getDataFields().size())
            throw new BeaconParserException
                    ("Number of data in the beacon is different from what declared in the 'Descriptor'");
        else{
        for (int i=0;i<bleBeaconDataList.size();i++)
            bleBeaconDataList.get(i).setValue(beacon.getDataFields().get(i));}

        if (bleBeaconExtraDataList.size()>0){
            if(beacon.getExtraDataFields().size()>0){
                if (bleBeaconExtraDataList.size()!=beacon.getExtraDataFields().size())
                    throw new BeaconParserException
                            ("Number of extradata in the beacon is different from what declared in the 'Descriptor'");
                else{
                    for (int i=0;i<bleBeaconExtraDataList.size();i++)
                        bleBeaconExtraDataList.get(i).setValue(beacon.getExtraDataFields().get(i));
                }
            }
        }
    }

    /**
     *
     * Generates the data fields for the BLEBeacon by the means of the parser
     * @param parserId the id of the parser as defined in the xml file
     *
     */
    private void setBLEBeaconData(String parserId){
        for (BLEBeaconParser bleBeaconParser:BLEBeaconManager.allBleBeaconParsers){
            if (bleBeaconParser.getParser_identifier().equalsIgnoreCase(parserId)){
                for (BLEBeaconData bleBeaconData:bleBeaconParser.getData()){
                    bleBeaconDataList.add(bleBeaconData);
                }
                if (bleBeaconParser.hasExtra_layout()){
                    for (BLEBeaconData bleBeaconData:bleBeaconParser.getExtra_layoutData()){
                        bleBeaconExtraDataList.add(bleBeaconData);
                    }
                }
            }
        }
    }

    @Deprecated
    public BLEBeacon (int type)
    {
        super();
        this.type=type;
    }


    //region Parcelable interface

    /**
     * Required for making object Parcelable
     **/
    protected BLEBeacon(Parcel in) {
        super(in);
        type=in.readInt();
        model=in.readInt();
        //parser=in.readString();
        this.mDefinedIdentifiers=new ArrayList<>();
        int mDefinedIdentifiersSize=in.readInt();
        for (int i=0;i<mDefinedIdentifiersSize;i++)
            mDefinedIdentifiers.add(Identifier.parse(in.readString()));

        this.mDataFieldDescriptions=new ArrayList<>();
        int mDataFieldDescriptionsSize=in.readInt();
        for (int i=0;i<mDataFieldDescriptionsSize;i++)
            mDataFieldDescriptions.add(in.readString());

        uniqueId=in.readString();
    }

    /**
     * Required for making object Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Required for making object Parcelable
     **/
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(type);
        out.writeInt(model);
        //out.writeString(parser);
        out.writeInt(mDefinedIdentifiers.size());
        for (Identifier identifier: mDefinedIdentifiers) {
            out.writeString(identifier == null ? null : identifier.toString());
        }

        out.writeInt(mDataFieldDescriptions.size());
        for (String s: mDataFieldDescriptions) {
            out.writeString(s);
        }
        out.writeString(uniqueId);
    }

    /**
     * Required for making object Parcelable.  If you override this class, you must provide an
     * equivalent version of this method.
     */
    public static final Parcelable.Creator<BLEBeacon> CREATOR
            = new Parcelable.Creator<BLEBeacon>() {
        public BLEBeacon createFromParcel(Parcel in) {
            return new BLEBeacon(in);
        }

        public BLEBeacon[] newArray(int size) {
            return new BLEBeacon[size];
        }
    };
    //endregion


    //region Serializable interface
    // PRIVATE

    /**
     * Custom deserialization is needed.
     */
    private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
        aStream.defaultReadObject();
        //manually deserialize and init superclass
        this.mBluetoothAddress = (String)aStream.readObject();
        this.mIdentifiers = new ArrayList<>((Collection<Identifier>)aStream.readObject());
        this.mBeaconTypeCode = aStream.readInt();
        this.mDataFields = new ArrayList<>((Collection<Long>)aStream.readObject());
        this.mDistance = aStream.readDouble();
        this.mRssi = aStream.readInt();
        this.mTxPower = aStream.readInt();
        this.mParserIdentifier = (String) aStream.readObject();
    }

    /**
     * Custom serialization is needed.
     */
    private void writeObject(ObjectOutputStream aStream) throws IOException {
        aStream.defaultWriteObject();
        //manually serialize superclass
        aStream.writeObject(getBluetoothAddress());
        aStream.writeObject(getIdentifiers());
        aStream.writeInt(getBeaconTypeCode());
        aStream.writeObject(getDataFields());
        aStream.writeDouble(getDistance());
        aStream.writeInt(getRssi());
        aStream.writeInt(getTxPower());
        aStream.writeObject(getParserIdentifier());
    }
    //endregion


    protected BLEBeacon(){}


    public void setConfigurableParameters(Beacon beacon) throws BeaconParserException
    {
        this.setExtraDataFields(beacon.getExtraDataFields());
        if (mExtraDataFields!=null)
            upDateBLEBeaconData(beacon);
        this.setRssi(beacon.getRssi());
        this.mBluetoothAddress=beacon.getBluetoothAddress();
        //this.setRunningAverageRssi(beacon);
    }

    /**
     * Builder class for BLEBeacon objects. Provides a convenient way to set the various fields of a
     * Beacon
     *
     * <p>Example:
     *
     * <pre>
     * Beacon beacon = new Beacon.Builder()
     *         .setId1(&quot;2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6&quot;)
     *         .setId2("1")
     *         .setId3("2")
     *         .set...(3);
     *         .build();
     * </pre>
     */
    public static class Builder extends Beacon.Builder {

        protected List<String> mDataFieldDescriptions=new ArrayList<>();
        int model=BLEBeacon.MODEL_UNKNOWN;
        int type=BLEBeacon.UNKNOWN_BEACON;
        //String parser;
        String parserIdentifier;
        private Identifier mdefId1, mdefId2, mdefId3;
        private String uid;
        private Beacon beacon;
        //private String uidClusterOwner;
        private List<BLEBeaconData> bleBeaconDatas;

        @Override
        public BLEBeacon build() {
            List<Identifier> identifiers = new ArrayList<>();
            if (mdefId1!= null) {
                identifiers.add(mdefId1);
                if (mdefId2!= null) {
                    identifiers.add(mdefId2);
                    if (mdefId3!= null) {
                        identifiers.add(mdefId3);
                    }
                }
            }
            if (beacon==null)
            {
//                beacon=super.setId1(mdefId1.toString())
//                        .setId2(mdefId2.toString())
//                        .setId3(mdefId3.toString())
//                        .build();
                Beacon.Builder builder = new Beacon.Builder();
                if (mdefId1!=null)
                builder.setId1(mdefId1.toString());
                if (mdefId2!=null)
                    builder.setId2(mdefId2.toString());
                if (mdefId3!=null)
                    builder.setId3(mdefId3.toString());

                beacon=builder.build();
            }
            else
            {
                if (beacon.getParserIdentifier()!=null)
                    parserIdentifier=beacon.getParserIdentifier();
            }
            return new BLEBeacon(beacon, model, type, parserIdentifier,
                    identifiers, mDataFieldDescriptions, uid);
        }

        public Builder setModel(int model) {
            this.model = model;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setParserIdentifier(String parser) {
            this.parserIdentifier = parser;
            return this;
        }


        public Builder setUid(String uid) {
            this.uid = uid;
            return this;
        }

//        public Builder setUidClusterOwner(String uidClusterOwner) {
//            this.uidClusterOwner = uidClusterOwner;
//            return this;
//        }


        /**
         * Convenience method allowing the first beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id1String string to parse into an identifier
         * @return builder
         */
        public Builder setDefinedId1(String id1String) {
            mdefId1 = Identifier.parse(id1String);
            return this;
        }

        /**
         * Convenience method allowing the second beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id2String string to parse into an identifier
         * @return builder
         */
        public Builder setDefinedId2(String id2String) {
            mdefId2 = Identifier.parse(id2String);
            return this;
        }

        /**
         * Convenience method allowing the third beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id3String string to parse into an identifier
         * @return builder
         */
        public Builder setDefinedId3(String id3String) {
            mdefId3 = Identifier.parse(id3String);
            return this;
        }

        /**
         * Method adding the description of the data. The description shall be sorted by the
         * position in the beacon payload
         * @param dataFieldDescription string to be added as data descritpr
         * @return builder
         */
        public Builder addDataFieldDescription(String dataFieldDescription) {
            mDataFieldDescriptions.add(dataFieldDescription);
            return this;
        }

        public Builder setSeedBeacon(Beacon beacon)
        {
            this.beacon=beacon;
            List<Identifier>  identifiers=beacon.getIdentifiers();
            if(identifiers!=null){
                int idSize=identifiers.size();
                if (idSize>0)
                    this.mdefId1=beacon.getId1();
                if (idSize>1)
                    this.mdefId2=beacon.getId2();
                if (idSize>2)
                    this.mdefId3=beacon.getId3();}
            this.parserIdentifier=beacon.getParserIdentifier();
            return this;
        }
    }


}
