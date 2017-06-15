package com.ngengs.android.popularmovies.stage1.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused", "DefaultFileTemplate"})
public class ObjectName {
    private int id;
    private String name;
    @SerializedName("iso_639_1")
    private String iso;

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
}
