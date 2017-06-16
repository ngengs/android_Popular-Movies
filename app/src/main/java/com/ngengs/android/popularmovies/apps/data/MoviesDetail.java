package com.ngengs.android.popularmovies.apps.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.ngengs.android.popularmovies.apps.globals.Values;

import java.util.Date;
import java.util.List;

/**
 * Created by ngengs on 6/15/2017.
 */

@SuppressWarnings({"SameParameterValue", "unused", "DefaultFileTemplate"})
public class MoviesDetail implements Parcelable {
    public static final Creator<MoviesDetail> CREATOR = new Creator<MoviesDetail>() {
        @Override
        public MoviesDetail createFromParcel(Parcel in) {
            return new MoviesDetail(in);
        }

        @Override
        public MoviesDetail[] newArray(int size) {
            return new MoviesDetail[size];
        }
    };
    private int id;
    @SerializedName("adult")
    private boolean adult;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("budget")
    private float budget;
    @SerializedName("genres")
    private List<ObjectName> genres;
    @SerializedName("homepage")
    private String homepage;
    @SerializedName("imdb_id")
    private String imdbId;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("overview")
    private String overview;
    @SerializedName("popularity")
    private double popularity;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("production_companies")
    private List<ObjectName> productionCompanies;
    @SerializedName("production_countries")
    private List<ObjectName> productionCountries;
    @SerializedName("release_date")
    private Date releaseDate;
    @SerializedName("revenue")
    private float revenue;
    @SerializedName("runtime")
    private int runtime;
    @SerializedName("spoken_languages")
    private List<ObjectName> spokenLanguages;
    @SerializedName("status")
    private String status;
    @SerializedName("tagline")
    private String tagline;
    @SerializedName("title")
    private String title;
    @SerializedName("video")
    private boolean video;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("vote_count")
    private int voteCount;
    //Error Data
    @SerializedName("status_code")
    private int statusCode;
    @SerializedName("status_message")
    private String statusMessage;

    @SuppressWarnings("WeakerAccess")
    protected MoviesDetail(Parcel in) {
        id = in.readInt();
        adult = in.readByte() != 0;
        backdropPath = in.readString();
        budget = in.readFloat();
        genres = in.createTypedArrayList(ObjectName.CREATOR);
        homepage = in.readString();
        imdbId = in.readString();
        originalLanguage = in.readString();
        originalTitle = in.readString();
        overview = in.readString();
        popularity = in.readDouble();
        posterPath = in.readString();
        productionCompanies = in.createTypedArrayList(ObjectName.CREATOR);
        productionCountries = in.createTypedArrayList(ObjectName.CREATOR);
        revenue = in.readFloat();
        runtime = in.readInt();
        spokenLanguages = in.createTypedArrayList(ObjectName.CREATOR);
        status = in.readString();
        tagline = in.readString();
        title = in.readString();
        video = in.readByte() != 0;
        voteAverage = in.readDouble();
        voteCount = in.readInt();
        statusCode = in.readInt();
        statusMessage = in.readString();
    }

    public String getPosterPath(int sizeType) {
        if (posterPath != null && !posterPath.equals("")) {
            String size = Values.IMAGE_SIZE_PATH[0];
            if (sizeType >= 0 && sizeType < Values.IMAGE_SIZE_PATH.length)
                size = Values.IMAGE_SIZE_PATH[sizeType];
            return Values.URL_IMAGE + size + posterPath;
        } else return null;
    }

    public String getBackdropPath() {
        return getBackdropPath(Values.TYPE_DEFAULT_IMAGE_THUMB);
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getPosterPath() {
        return getPosterPath(Values.TYPE_DEFAULT_IMAGE_THUMB);
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath(int sizeType) {
        if (backdropPath != null && !backdropPath.equals("")) {
            String size = Values.IMAGE_SIZE_BACKDROP[0];
            if (sizeType >= 0 && sizeType < Values.IMAGE_SIZE_BACKDROP.length)
                size = Values.IMAGE_SIZE_BACKDROP[sizeType];
            return Values.URL_IMAGE + size + backdropPath;
        } else return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    public List<ObjectName> getGenres() {
        return genres;
    }

    public void setGenres(List<ObjectName> genres) {
        this.genres = genres;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public List<ObjectName> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ObjectName> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public List<ObjectName> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(List<ObjectName> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public float getRevenue() {
        return revenue;
    }

    public void setRevenue(float revenue) {
        this.revenue = revenue;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public List<ObjectName> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(List<ObjectName> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
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
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeString(backdropPath);
        dest.writeFloat(budget);
        dest.writeTypedList(genres);
        dest.writeString(homepage);
        dest.writeString(imdbId);
        dest.writeString(originalLanguage);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeDouble(popularity);
        dest.writeString(posterPath);
        dest.writeTypedList(productionCompanies);
        dest.writeTypedList(productionCountries);
        dest.writeFloat(revenue);
        dest.writeInt(runtime);
        dest.writeTypedList(spokenLanguages);
        dest.writeString(status);
        dest.writeString(tagline);
        dest.writeString(title);
        dest.writeByte((byte) (video ? 1 : 0));
        dest.writeDouble(voteAverage);
        dest.writeInt(voteCount);
        dest.writeInt(statusCode);
        dest.writeString(statusMessage);
    }
}
