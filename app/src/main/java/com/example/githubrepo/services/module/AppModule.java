package com.example.githubrepo.services.module;

import android.app.Application;
import android.content.Context;

import com.example.githubrepo.BaseSearchActivity;
import com.example.githubrepo.MainActivity;
import com.example.githubrepo.MyApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sarah_neo on 22/02/2017.
 */

@Module(
        includes = {
                BusModule.class,
        },
        injects = {
                MyApplication.class,
                BaseSearchActivity.class,
                MainActivity.class
        },
        library = true
)
public class AppModule {
    private final MyApplication app;

    public AppModule(MyApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return app;
    }
}
