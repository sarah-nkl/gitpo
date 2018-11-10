package com.example.githubrepo.services.module

import com.example.githubrepo.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MyApplicationModule {

    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): MainActivity
}