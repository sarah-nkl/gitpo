package com.example.githubrepo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class Owner implements Parcelable {

    @SerializedName("login")
    private String login;
    @SerializedName("id")
    private long id;
    @SerializedName("avatar_url")
    private String avatarUrl;

    public Owner() { }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(login);
        dest.writeLong(id);
        dest.writeString(avatarUrl);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<Owner> CREATOR = new Parcelable.Creator<Owner>() {
        public Owner createFromParcel(Parcel pc) {
            return new Owner(pc);
        }
        public Owner[] newArray(int size) {
            return new Owner[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    Owner(Parcel pc){
        login = pc.readString();
        id = pc.readLong();
        avatarUrl = pc.readString();
    }
}
