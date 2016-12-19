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

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconMonitorNotifier;


/**
 * Class for wrapping {@link Region}.
 */
//TODO: this class should extend the Region rather than wraps it
public class BLEBeaconRegion implements Parcelable, Serializable{

    private Identifier mId1;
    private Identifier mId2;
    private Identifier mId3;
    private String uidBleRegion;
    private boolean regionImplemented=false;
    private Region region;
    private List<String> regionsParsers;
    private int state= BLEBeaconMonitorNotifier.OUTSIDE;
    private BLEBeacon bleBeacon;
    private boolean singleBeaconRegion=false;

    public Identifier getmId1() {
        return mId1;
    }

    public Identifier getmId2() {
        return mId2;
    }

    public Identifier getmId3() {
        return mId3;
    }

    public String getUidBleRegion() {
        return uidBleRegion;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }



    public boolean isSingleBeaconRegion() {
        return singleBeaconRegion;
    }

    public BLEBeacon getBleBeacon() {
        return bleBeacon;
    }

    public boolean isRegionImplemented() {
        return regionImplemented;
    }

    public Region getRegion() {
        return region;
    }
    public List<String> getRegionsParsers() {
        return Collections.unmodifiableList(this.regionsParsers);
    }

    private BLEBeaconRegion(Region region, List<String> regionsParsers)
    {
        this.region=region;
        this.regionsParsers=regionsParsers;
        this.regionImplemented=true;
        this.uidBleRegion=region.getUniqueId();
    }

    /**
     * Constructor.
     * @param bleBeacon {@link BLEBeacon} is the seed of the {@link BLEBeaconRegion}
     */
    private BLEBeaconRegion(BLEBeacon bleBeacon)
    {
        singleBeaconRegion=true;
        this.bleBeacon=bleBeacon;
        this.regionsParsers=new ArrayList<>();
        this.regionsParsers.add(bleBeacon.getParserIdentifier());
        this.uidBleRegion=bleBeacon.getUniqueId();
        List<Identifier>  identifiers=bleBeacon.getIdentifiers();
        if(identifiers!=null){
        int idSize=identifiers.size();
        if (idSize>0)
        this.mId1=bleBeacon.getId1();
        if (idSize>1)
        this.mId2=bleBeacon.getId2();
        if (idSize>2)
        this.mId3=bleBeacon.getId3();}
    }

    /**
     * Constructor.
     * @param uidBleRegion id of the region
     * @param mId1 first {@link Identifier} of the region
     * @param mId2 second {@link Identifier} of the region
     * @param mId3 third {@link Identifier} of the region
     * @param regionsParsers id of the parsers for the region as defined in the parser xml file
     */
    private BLEBeaconRegion(String uidBleRegion, Identifier mId1, Identifier mId2,
                            Identifier mId3, List<String> regionsParsers)
    {
        this.regionsParsers=regionsParsers;
        this.uidBleRegion=uidBleRegion;
        this.mId1=mId1;
        this.mId2=mId2;
        this.mId3=mId3;
    }

    /**
     * Implements the region related to the {@link BLEBeaconRegion}.
     *
     * @param uidClusterOwner the name of the {@link BLEBeaconCluster} owner.
     *                        The name is used in order to build the uniqueId of the region.
     *
     * @return whether the implementation succeeded or not
     * (the last occurs when the region il already implemented).
     */
    public boolean implementRegion(String uidClusterOwner)
    {
        if (this.region!=null)
            return false;

        this.region = new Region(new StringBuilder()
                .append(uidClusterOwner)
                .append(uidBleRegion).toString(), mId1, mId2, mId3);
        this.regionImplemented=true;
        return true;
    }

    /**
     * Required for making Beacon parcelable
     * @param in parcel
     */
    protected BLEBeaconRegion(Parcel in)
    {
        int available=0;
        available=in.readInt();
        if(available==1)
            mId1=Identifier.parse(in.readString());
        available=in.readInt();
        if(available==1)
            mId2=Identifier.parse(in.readString());
        available=in.readInt();
        if(available==1)
            mId3=Identifier.parse(in.readString());


        available=in.readInt();
        if(available==1)
            uidBleRegion=in.readString();

        boolean[] bArray = new boolean[2];
        in.readBooleanArray(bArray);
        regionImplemented=bArray[0];
        singleBeaconRegion=bArray[1];

        available=in.readInt();
        if(available==1)
            uidBleRegion=in.readString();

        available=in.readInt();
        if(available==1)
            region=Region.CREATOR.createFromParcel(in);

        int regionsParsersSize=in.readInt();
        for(int i=0;i<regionsParsersSize;i++)
            regionsParsers.add(in.readString());

        state=in.readInt();

        available=in.readInt();
        if(available==1)
            bleBeacon=BLEBeacon.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Required for making object Parcelable
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(mId1!=null){
            dest.writeInt(1);
            dest.writeString(mId1.toString());
        }
        else{
            dest.writeInt(0);
        }
        if(mId2!=null){
            dest.writeInt(1);
            dest.writeString(mId2.toString());
        }
        else{
            dest.writeInt(0);
        }
        if(mId3!=null) {
            dest.writeInt(1);
            dest.writeString(mId3.toString());
        }
        else {
            dest.writeInt(0);
        }

        if(uidBleRegion!=null) {
            dest.writeInt(1);
            dest.writeString(uidBleRegion);
        }
        else {
            dest.writeInt(0);
        }

        dest.writeBooleanArray(new  boolean[]{regionImplemented, singleBeaconRegion});

        if(region!=null) {
            dest.writeInt(1);
            region.writeToParcel(dest, flags);
        }
        else {
            dest.writeInt(0);
        }

        dest.writeInt(regionsParsers.size());
        for (String s:regionsParsers)
            dest.writeString(s);

        dest.writeInt(state);

        if(bleBeacon!=null) {
            dest.writeInt(1);
            bleBeacon.writeToParcel(dest, flags);
        }
        else {
            dest.writeInt(0);
        }
    }

    /**
     * Required for making object Parcelable.  If you override this class, you must provide an
     * equivalent version of this method.
     */
    public static final Parcelable.Creator<BLEBeaconRegion> CREATOR
            = new Parcelable.Creator<BLEBeaconRegion>() {
        public BLEBeaconRegion createFromParcel(Parcel in) {
            return new BLEBeaconRegion(in);
        }

        public BLEBeaconRegion[] newArray(int size) {
            return new BLEBeaconRegion[size];
        }
    };


    public static class Builder {

        private Identifier mId1;
        private Identifier mId2;
        private Identifier mId3;
        private String uid;
        private BLEBeacon bleBeacon;
        private List<String> regionsParsers = new ArrayList<>();
        private BLEBeaconRegion bleRegionSeed;

        public Builder() {
        }

        public BLEBeaconRegion buildNotImplementingRegion() {
            if (bleBeacon!=null)
            {
                return new BLEBeaconRegion(bleBeacon);
            }
            else if (bleRegionSeed!=null)
            {
                return new BLEBeaconRegion(bleRegionSeed.uidBleRegion, bleRegionSeed.getmId1(),
                        bleRegionSeed.getmId2(), bleRegionSeed.getmId3(),
                        bleRegionSeed.getRegionsParsers());
            }
            else
            {
                return new BLEBeaconRegion(uid, mId1, mId2, mId3,
                        regionsParsers);
            }

        }

        public BLEBeaconRegion.Builder setId1(String id1String) {
            this.mId1 = Identifier.parse(id1String);
            return this;
        }

        public BLEBeaconRegion.Builder setId2(String id2String) {
            this.mId2 = Identifier.parse(id2String);
            return this;
        }

        public BLEBeaconRegion.Builder setId3(String id3String) {
            this.mId3 = Identifier.parse(id3String);
            return this;
        }

        public BLEBeaconRegion.Builder setUid(String uid) {
            this.uid=uid;
            return this;
        }

        public BLEBeaconRegion.Builder addParser(String parse) {
            this.regionsParsers.add(parse);
            return this;
        }

        /**
         * Implements the region related to the {@link BLEBeaconRegion}.
         *
         * @param bleBeacon in case a {@link BLEBeacon} is passed to the builder,
         *                  the other parameter are ignored. The {@link BLEBeaconRegion}
         *                  builded is a single beacon Region
         *
         *
         * @return
         */
        public BLEBeaconRegion.Builder setTheSingleBLEBeacon(BLEBeacon bleBeacon) {
            this.bleBeacon=bleBeacon;
            return this;
        }

        /**
         * Implements the region related to the {@link BLEBeaconRegion}.
         *
         * @param bleRegionSeed in case a {@link BLEBeaconRegion} is passed to the builder,
         *                  the new <code>BLEbeaconRegion</code> will inherits the parameters of the passed
         *                      <code>BLEbeaconRegion</code> bleRegionSeed, but the {@link Region}
         *                      which will be created by {@link BLEBeaconRegion#implementRegion(java.lang.String)},
         *                      to have a name compatible with the {@link BLEBeaconCluster} owner.
         *                      In case {@link Builder#bleBeacon} is different from <code>null</code> the
         *                      {@link Builder#bleRegionSeed} will be ignored.
         *
         *
         * @return
         */
        public BLEBeaconRegion.Builder setBLERegionSeed(BLEBeaconRegion bleRegionSeed) {
            this.bleRegionSeed=bleRegionSeed;
            return this;
        }
    }
}
