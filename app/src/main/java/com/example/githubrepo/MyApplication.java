package com.example.githubrepo;

import android.app.Application;

import com.example.githubrepo.services.module.AppModule;

import dagger.ObjectGraph;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class MyApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new AppModule(this));
        objectGraph.inject(this);
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    public void inject(Object obj) {
        objectGraph.inject(obj);
    }
}
