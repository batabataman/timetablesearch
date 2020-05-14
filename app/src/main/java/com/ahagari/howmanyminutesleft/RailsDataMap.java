package com.ahagari.howmanyminutesleft;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class RailsDataMap implements Parcelable {

    public RailsDataMap(HashMap<String, RailsInfoData> map) {
        this.map = map;
    }

    public RailsDataMap() {
    }

    HashMap<String, RailsInfoData> map;

    public HashMap<String, RailsInfoData> getMap() {
        return map;
    }

    public void setMap(HashMap<String, RailsInfoData> map) {
        this.map = map;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.map);
    }

    protected RailsDataMap(Parcel in) {
        this.map = (HashMap<String, RailsInfoData>) in.readSerializable();
    }

    public static final Parcelable.Creator<RailsDataMap> CREATOR = new Parcelable.Creator<RailsDataMap>() {
        @Override
        public RailsDataMap createFromParcel(Parcel source) {
            return new RailsDataMap(source);
        }

        @Override
        public RailsDataMap[] newArray(int size) {
            return new RailsDataMap[size];
        }
    };
}
