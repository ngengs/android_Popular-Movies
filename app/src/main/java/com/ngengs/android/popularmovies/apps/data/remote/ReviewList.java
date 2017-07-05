package com.ngengs.android.popularmovies.apps.data.remote;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ngengs on 7/1/2017.
 */

@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess", "unused"})
public class ReviewList implements Parcelable {
    public static final Creator<ReviewList> CREATOR = new Creator<ReviewList>() {
        @Override
        public ReviewList createFromParcel(Parcel in) {
            return new ReviewList(in);
        }

        @Override
        public ReviewList[] newArray(int size) {
            return new ReviewList[size];
        }
    };
    private String id;
    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private List<ReviewDetail> review;
    @SerializedName("total_pages")
    private int totalPage;
    @SerializedName("total_results")
    private int totalResult;
    //Error Data
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_message")
    private String statusMessage;

    protected ReviewList(Parcel in) {
        id = in.readString();
        page = in.readInt();
        review = in.createTypedArrayList(ReviewDetail.CREATOR);
        totalPage = in.readInt();
        totalResult = in.readInt();
        statusCode = in.readInt();
        statusMessage = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<ReviewDetail> getReview() {
        return review;
    }

    public void setReview(List<ReviewDetail> review) {
        this.review = review;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
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
        dest.writeString(id);
        dest.writeInt(page);
        dest.writeTypedList(review);
        dest.writeInt(totalPage);
        dest.writeInt(totalResult);
        dest.writeInt(statusCode);
        dest.writeString(statusMessage);
    }
}
