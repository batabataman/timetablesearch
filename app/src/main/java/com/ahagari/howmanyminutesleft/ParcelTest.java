package com.ahagari.howmanyminutesleft;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelTest implements Parcelable {
    String a;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public ParcelTest(String a) {
        this.a = a;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.a);
    }

    public ParcelTest() {
    }

    protected ParcelTest(Parcel in) {
        this.a = in.readString();
    }

    public static final Parcelable.Creator<ParcelTest> CREATOR = new Parcelable.Creator<ParcelTest>() {
        @Override
        public ParcelTest createFromParcel(Parcel source) {
            return new ParcelTest(source);
        }

        @Override
        public ParcelTest[] newArray(int size) {
            return new ParcelTest[size];
        }
    };
}
