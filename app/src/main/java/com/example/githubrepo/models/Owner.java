package com.example.githubrepo.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class Owner {

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
}
