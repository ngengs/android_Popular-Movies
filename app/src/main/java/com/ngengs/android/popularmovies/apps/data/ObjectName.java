package com.ngengs.android.popularmovies.apps.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused", "DefaultFileTemplate"})
public class ObjectName implements Parcelable {
    public static final Creator<ObjectName> CREATOR = new Creator<ObjectName>() {
        @Override
        public ObjectName createFromParcel(Parcel in) {
            return new ObjectName(in);
        }

        @Override
        public ObjectName[] newArray(int size) {
            return new ObjectName[size];
        }
    };
    private int id;
    private String name;
    @SerializedName("iso_639_1")
    private String iso;

    protected ObjectName(Parcel in) {
        id = in.readInt();
        name = in.readString();
        iso = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(iso);
    }
}
