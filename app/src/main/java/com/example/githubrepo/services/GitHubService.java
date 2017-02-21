package com.example.githubrepo.services;

import com.example.githubrepo.models.Repository;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public interface GitHubService {

    // 10 calls per minute
    @GET("search/repositories")
    Call<List<Repository>> listRepos(@Query("q") String query,
                                     @Query("sort") String sort,
                                     @Query("order") String order,
                                     @Query("page") int page,
                                     @Query("per_page") int perPage);
}
