package com.ngengs.android.popularmovies.apps.data.remote;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ngengs on 7/1/2017.
 */

@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess", "unused"})
public class ReviewDetail implements Parcelable {
    public static final Creator<ReviewDetail> CREATOR = new Creator<ReviewDetail>() {
        @Override
        public ReviewDetail createFromParcel(Parcel in) {
            return new ReviewDetail(in);
        }

        @Override
        public ReviewDetail[] newArray(int size) {
            return new ReviewDetail[size];
        }
    };
    private String id;
    @SerializedName("author")
    private String author;
    @SerializedName("content")
    private String content;
    @SerializedName("url")
    private String url;

    protected ReviewDetail(Parcel in) {
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }
}
