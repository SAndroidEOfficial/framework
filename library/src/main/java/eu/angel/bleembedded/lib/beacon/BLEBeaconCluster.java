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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.angel.bleembedded.lib.beacon.notifier.BLEBeaconMonitorNotifier;

/**
 * Class which includes list of {@link BLEBeaconRegion} which are used for ranging/monitoring.
 * They are imported/stored from/to xml file.
 */
public class BLEBeaconCluster implements Parcelable {

    public enum BeaconAction
    {
        RANGING,
        MONITORING,
        RANGING_AND_MONITORING
    }

    //private List<BLEBeacon> bleBeacons;
    private List<BLEBeaconRegion> bleBeaconRegions;
    private String uniqueId;
    private int state = BLEBeaconMonitorNotifier.OUTSIDE;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    /**
     * Construtor of {@link BLEBeaconCluster}.
     * @param uniqueId id of the Cluster
     * @param bleBeaconRegions list of  {@link BLEBeaconRegion} included in the Cluster
     */
    public BLEBeaconCluster(String uniqueId, List<BLEBeaconRegion> bleBeaconRegions)
    {
        this.uniqueId=uniqueId;
        //this.bleBeacons=bleBeacons;
        this.bleBeaconRegions=bleBeaconRegions;
        for(BLEBeaconRegion bleBeaconRegion:bleBeaconRegions)
            bleBeaconRegion.implementRegion(uniqueId);
    }

    public String getUniqueId() {
        return uniqueId;
    }


    public List<BLEBeaconRegion> getBleBeaconRegions() {
        return Collections.unmodifiableList(this.bleBeaconRegions);
    }

    /**
     * Required for making Beacon parcelable
     * @param in parcel
     */
    protected BLEBeaconCluster(Parcel in)
    {
        int available;
        available=in.readInt();
        if(available==1)
            uniqueId=in.readString();

        state=in.readInt();

        int bleBeaconRegionsSize=in.readInt();
        if(bleBeaconRegionsSize>0){
            bleBeaconRegions=new ArrayList<>();
            for (int i=0;i<bleBeaconRegionsSize;i++)
                bleBeaconRegions.add(BLEBeaconRegion.CREATOR.createFromParcel(in));
        }
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
        if(uniqueId!=null){
            dest.writeInt(1);
            dest.writeString(uniqueId.toString());
        }
        else{
            dest.writeInt(0);
        }

        dest.writeInt(state);

        if (bleBeaconRegions!=null) {
            dest.writeInt(bleBeaconRegions.size());
            for (BLEBeaconRegion bleBeaconRegion:bleBeaconRegions)
                bleBeaconRegion.writeToParcel(dest,flags);
        }else{
            dest.writeInt(0);
        }
    }

    /**
     * Required for making object Parcelable.  If you override this class, you must provide an
     * equivalent version of this method.
     */
    public static final Parcelable.Creator<BLEBeaconCluster> CREATOR
            = new Parcelable.Creator<BLEBeaconCluster>() {
        public BLEBeaconCluster createFromParcel(Parcel in) {
            return new BLEBeaconCluster(in);
        }

        public BLEBeaconCluster[] newArray(int size) {
            return new BLEBeaconCluster[size];
        }
    };



    public static class Builder implements Parcelable, Serializable
    {
        String uid;
        List<BLEBeaconRegion> bleBeaconRegions=new ArrayList<>();

        public String getUid() {
            return uid;
        }

        public List<BLEBeaconRegion> getBleBeaconRegions() {
            return Collections.unmodifiableList(this.bleBeaconRegions);}

        public Builder(){}

        public BLEBeaconCluster build()
        {
            return new BLEBeaconCluster(uid, bleBeaconRegions);
        }

        public Builder setUid(String uid)
        {
            this.uid=uid;
            return this;
        }

        public Builder addBLERegion(BLEBeaconRegion bleBeaconRegion)
        {
            this.bleBeaconRegions.add(bleBeaconRegion);
            return this;
        }

        public Builder setBLERegion(List<BLEBeaconRegion> bleBeaconRegions)
        {
            this.bleBeaconRegions=bleBeaconRegions;
            return this;
        }

        //region Parcelable
        /**
         * Required for making Beacon parcelable
         * @param in parcel
         */
        protected Builder(Parcel in)
        {
            int available=0;
            available=in.readInt();
            if(available==1)
                uid=in.readString();

            int bleBeaconRegionsSize=in.readInt();
            if(bleBeaconRegionsSize>0){
                bleBeaconRegions=new ArrayList<>();
                for (int i=0;i<bleBeaconRegionsSize;i++)
                    bleBeaconRegions.add(BLEBeaconRegion.CREATOR.createFromParcel(in));
            }
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
            if(uid!=null){
                dest.writeInt(1);
                dest.writeString(uid.toString());
            }
            else{
                dest.writeInt(0);
            }


            if (bleBeaconRegions!=null) {
                dest.writeInt(bleBeaconRegions.size());
                for (BLEBeaconRegion bleBeaconRegion:bleBeaconRegions)
                    bleBeaconRegion.writeToParcel(dest,flags);
            }else{
                dest.writeInt(0);
            }
        }

        /**
         * Required for making object Parcelable.  If you override this class, you must provide an
         * equivalent version of this method.
         */
        public static final Parcelable.Creator<Builder> CREATOR
                = new Parcelable.Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
        //endregion

    }

}
