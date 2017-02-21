package com.example.githubrepo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class Repository implements Parcelable {

    @SerializedName("id")
    private long id;
    @SerializedName("name")
    private String name;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("private")
    private boolean privateRepo;
    @SerializedName("html_url")
    private String htmlUrl;
    @SerializedName("description")
    private String desc;
    @SerializedName("create_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("pushed_at")
    private String pushedAt;
    @SerializedName("homepage")
    private String homePage;
    @SerializedName("stargazers_count")
    private int stargazersCount;
    @SerializedName("language")
    private String language;
    @SerializedName("forks_count")
    private int forksCount;
    @SerializedName("open_issues_count")
    private int openIssuesCount;
    @SerializedName("watchers")
    private int watchers;
    @SerializedName("score")
    private double score;

    private Owner owner;

    public Repository() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isPrivateRepo() {
        return privateRepo;
    }

    public void setPrivateRepo(boolean privateRepo) {
        this.privateRepo = privateRepo;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPushedAt() {
        return pushedAt;
    }

    public void setPushedAt(String pushedAt) {
        this.pushedAt = pushedAt;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
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

    public int getOpenIssuesCount() {
        return openIssuesCount;
    }

    public void setOpenIssuesCount(int openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(fullName);
        dest.writeInt(privateRepo ? 1 : 0);
        dest.writeString(htmlUrl);
        dest.writeString(desc);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeString(pushedAt);
        dest.writeString(homePage);
        dest.writeInt(stargazersCount);
        dest.writeString(language);
        dest.writeInt(forksCount);
        dest.writeInt(openIssuesCount);
        dest.writeInt(watchers);
        dest.writeDouble(score);
        dest.writeSerializable((Serializable) owner);
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
        privateRepo = pc.readInt() == 1;
        htmlUrl = pc.readString();
        desc = pc.readString();
        createdAt = pc.readString();
        updatedAt = pc.readString();
        pushedAt = pc.readString();
        homePage = pc.readString();
        stargazersCount = pc.readInt();
        language = pc.readString();
        forksCount = pc.readInt();
        openIssuesCount = pc.readInt();
        watchers = pc.readInt();
        score = pc.readDouble();
        owner = (Owner) pc.readSerializable();
    }
}
