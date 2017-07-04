package com.ngengs.android.popularmovies.apps.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ngengs on 6/30/2017.
 */

@SuppressWarnings({"DefaultFileTemplate", "unused", "WeakerAccess"})
public class VideosList implements Parcelable {
    public static final Creator<VideosList> CREATOR = new Creator<VideosList>() {
        @Override
        public VideosList createFromParcel(Parcel in) {
            return new VideosList(in);
        }

        @Override
        public VideosList[] newArray(int size) {
            return new VideosList[size];
        }
    };
    private int id;
    @SerializedName("results")
    private List<VideosDetail> videos;

    //Error Data
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_message")
    private String statusMessage;

    protected VideosList(Parcel in) {
        id = in.readInt();
        videos = in.createTypedArrayList(VideosDetail.CREATOR);
        statusCode = in.readInt();
        statusMessage = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<VideosDetail> getVideos() {
        return videos;
    }

    public void setVideos(List<VideosDetail> videos) {
        this.videos = videos;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeTypedList(videos);
        dest.writeInt(statusCode);
        dest.writeString(statusMessage);
    }
}
