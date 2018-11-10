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

import com.example.githubrepo.services.BusProvider;
import com.example.githubrepo.services.GitHubService;
import com.example.githubrepo.services.event.BusEvent;
import com.example.githubrepo.services.event.LoadReposEvent;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by sarahneo on 20/2/17.
 */

public abstract class BaseSearchActivity extends AppCompatActivity {

    protected Drawable x;

    @Inject
    BusProvider busProvider;
    @Inject
    GitHubService gitHubService;
    @Inject
    SharedPreferences sharedPref;

    protected LoadReposEvent event;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        event = (LoadReposEvent) getEvent(BusEvent.EventType.REPOS);
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
