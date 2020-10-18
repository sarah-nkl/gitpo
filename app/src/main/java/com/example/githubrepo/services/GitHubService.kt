package com.example.githubrepo.services

import com.example.githubrepo.models.RepositoryList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubService {
    // 10 calls per minute
    @GET("search/repositories")
    fun listRepos(@Query("q") query: String,
                  @Query("sort") sort: String,
                  @Query("order") order: String?,
                  @Query("page") page: Int,
                  @Query("per_page") perPage: Int): Call<RepositoryList>
}