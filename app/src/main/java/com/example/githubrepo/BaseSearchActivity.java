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

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.githubrepo.databinding.ActivityMainBinding;
import com.example.githubrepo.models.Repository;
import com.example.githubrepo.services.BusProvider;
import com.example.githubrepo.services.GitHubService;
import com.example.githubrepo.services.event.BusEvent;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sarahneo on 20/2/17.
 */

public abstract class BaseSearchActivity extends AppCompatActivity {

    protected Drawable x;

    protected GitHubService mService;
    protected SharedPreferences mSharedPref;
    protected ActivityMainBinding mBinding;

    @Inject
    BusProvider busProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(mBinding.getRoot());

        ((MyApplication) getApplication()).inject(this);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initGitHubService();
    }

    private void initGitHubService() {
        //Create GitHubService
        GsonBuilder mBuilder = new GsonBuilder();
        Type listStockMover = new TypeToken<List<Repository>>() {
        }.getType();
        mBuilder.registerTypeAdapter(listStockMover, new RepoListDeserializer());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.GITHUB_SEARCH_URL)
                .addConverterFactory(GsonConverterFactory.create(mBuilder.create()))
                .build();

        mService = retrofit.create(GitHubService.class);
    }



    @Override
    protected void onStart() {
        super.onStart();
        try {
            busProvider.register(this);
        } catch (IllegalArgumentException e) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        busProvider.unregister(this);
    }

    public void post(Object event) {
        busProvider.post(event);
    }

    public Object getEvent(BusEvent.EventType type) {
        return busProvider.getEvent(type);
    }

}
