package com.example.githubrepo.services.module;

import com.example.githubrepo.RepoListDeserializer;
import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.GitHubService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.githubrepo.ConstantsKt.GITHUB_SEARCH_URL;

/**
 * Created by sarah_neo on 24/02/2017.
 */

@Module
public class NetModule {

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder mBuilder = new GsonBuilder();
        Type listStockMover = new TypeToken<List<Repository>>() {
        }.getType();
        mBuilder.registerTypeAdapter(listStockMover, new RepoListDeserializer());

        return mBuilder.create();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GITHUB_SEARCH_URL)
                .build();
    }

    @Provides
    @Singleton
    GitHubService provideGitHubService(Retrofit retrofit) {
        return retrofit.create(GitHubService.class);
    }
}
