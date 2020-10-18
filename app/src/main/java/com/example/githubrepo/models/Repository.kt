package com.example.githubrepo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Repository(
        @SerializedName("id")
        val id: Long = 0,
        @SerializedName("full_name")
        val fullName: String? = null,
        @SerializedName("description")
        val desc: String? = null,
        @SerializedName("pushed_at")
        val pushedAt: String? = null,
        @SerializedName("stargazers_count")
        val stargazersCount: Int = 0,
        @SerializedName("language")
        val language: String? = null,
        @SerializedName("forks_count")
        val forksCount: Int = 0,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        var ownerAvatarUrl: String? = null,
        var total: Int = 0
) : Parcelable