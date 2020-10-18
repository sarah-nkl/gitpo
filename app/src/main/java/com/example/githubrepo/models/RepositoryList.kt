package com.example.githubrepo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepositoryList(
        @SerializedName("items")
        val repos: List<Repository>? = null,
        @SerializedName("total_count")
        val totalCount: Int = 0
) : Parcelable