package com.example.githubrepo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class Repository implements Parcelable {

    @SerializedName("id")
    private long id;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("description")
    private String desc;
    @SerializedName("pushed_at")
    private String pushedAt;
    @SerializedName("stargazers_count")
    private int stargazersCount;
    @SerializedName("language")
    private String language;
    @SerializedName("forks_count")
    private int forksCount;
    @SerializedName("html_url")
    private String htmlUrl;

    private String ownerAvatarUrl;
    private int total;

    public Repository() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPushedAt() {
        return pushedAt;
    }

    public void setPushedAt(String pushedAt) {
        this.pushedAt = pushedAt;
    }

    public int getStargazersCount() {
        return stargazersCount;
    }

    public void setStargazersCount(int stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getForksCount() {
        return forksCount;
    }

    public void setForksCount(int forksCount) {
        this.forksCount = forksCount;
    }

    public String getOwnerAvatarUrl() {
        return ownerAvatarUrl;
    }

    public void setOwnerAvatarUrl(String ownerAvatarUrl) {
        this.ownerAvatarUrl = ownerAvatarUrl;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(id);
        dest.writeString(fullName);
        dest.writeString(desc);
        dest.writeString(pushedAt);
        dest.writeInt(stargazersCount);
        dest.writeString(language);
        dest.writeInt(forksCount);
        dest.writeString(ownerAvatarUrl);
        dest.writeInt(total);
        dest.writeString(htmlUrl);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<Repository> CREATOR = new Parcelable.Creator<Repository>() {
        public Repository createFromParcel(Parcel pc) {
            return new Repository(pc);
        }
        public Repository[] newArray(int size) {
            return new Repository[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    Repository(Parcel pc){
        id = pc.readLong();
        fullName = pc.readString();
        desc = pc.readString();
        pushedAt = pc.readString();
        stargazersCount = pc.readInt();
        language = pc.readString();
        forksCount = pc.readInt();
        ownerAvatarUrl = pc.readString();
        total = pc.readInt();
        htmlUrl = pc.readString();
    }
}
