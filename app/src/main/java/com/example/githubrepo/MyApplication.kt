package com.example.githubrepo

import android.app.Activity
import android.app.Application
import com.example.githubrepo.services.module.AppModule
import dagger.android.AndroidInjector
import dagger.android.DaggerActivity
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * Created by sarahneo on 12/8/17.
 */

class MyApplication: Application(), HasActivityInjector {

    @Inject
    internal lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .create(this)
                .inject(this)
    }
}