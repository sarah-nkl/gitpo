/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.example.githubrepo;

import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.GitHubService;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RepoSearchEngine {

    public static final int PER_PAGE = 20;
    private static final String GITHUB_SEARCH_URL = "https://api.github.com/";
    private GitHubService mService;

    public RepoSearchEngine() {
        //Create GitHubService
        GsonBuilder mBuilder = new GsonBuilder();
        Type listStockMover = new TypeToken<List<Repository>>() {
        }.getType();
        mBuilder.registerTypeAdapter(listStockMover, new RepoListDeserializer());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GITHUB_SEARCH_URL)
                .addConverterFactory(GsonConverterFactory.create(mBuilder.create()))
                .build();

        mService = retrofit.create(GitHubService.class);
    }

    public List<Repository> search(String query, int pageNum) {
        query = query.toLowerCase();

        List<Repository> result = new ArrayList<>();

        Call<List<Repository>> repoList = mService.listRepos(query, "stars", null, pageNum, PER_PAGE);



        try {
            result = repoList.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
