package com.ngengs.android.popularmovies.apps.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.ngengs.android.popularmovies.apps.globals.Values;

/**
 * Created by ngengs on 6/30/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class VideosDetail implements Parcelable {
    public static final Creator<VideosDetail> CREATOR = new Creator<VideosDetail>() {
        @Override
        public VideosDetail createFromParcel(Parcel in) {
            return new VideosDetail(in);
        }

        @Override
        public VideosDetail[] newArray(int size) {
            return new VideosDetail[size];
        }
    };
    private String id;
    @SerializedName("iso_639_1")
    private String iso639_1;
    @SerializedName("iso_3166_1")
    private String iso3166_1;
    @SerializedName("key")
    private String key;
    @SerializedName("site")
    private String site;
    @SerializedName("size")
    private int size;
    @SerializedName("type")
    private String type;
    //Error Data
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_message")
    private String statusMessage;

    protected VideosDetail(Parcel in) {
        id = in.readString();
        iso639_1 = in.readString();
        iso3166_1 = in.readString();
        key = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
        statusCode = in.readInt();
        statusMessage = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIso639_1() {
        return iso639_1;
    }

    public void setIso639_1(String iso639_1) {
        this.iso639_1 = iso639_1;
    }

    public String getIso3166_1() {
        return iso3166_1;
    }

    public void setIso3166_1(String iso3166_1) {
        this.iso3166_1 = iso3166_1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isYoutubeVideo() {
        return site.equalsIgnoreCase("youtube");
    }

    public String getYoutubeVideo() {
        if (key != null && !key.equals("") && isYoutubeVideo())
            return Values.URL_VIDEO_YOUTUBE + key;
        else return null;
    }

    public String getYoutubeThumbnail() {
        if (key != null && !key.equals("") && isYoutubeVideo())
            return String.format(Values.URL_VIDEO_YOUTUBE_THUMB, key);
        else return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(iso639_1);
        dest.writeString(iso3166_1);
        dest.writeString(key);
        dest.writeString(site);
        dest.writeInt(size);
        dest.writeString(type);
        dest.writeInt(statusCode);
        dest.writeString(statusMessage);
    }
}
