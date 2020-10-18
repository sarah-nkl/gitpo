package com.example.githubrepo.services.module

import android.app.Application
import android.content.Context
import com.example.githubrepo.BaseSearchActivity
import com.example.githubrepo.MainActivity
import com.example.githubrepo.MyApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
        includes = [BusModule::class],
        injects = [
            MyApplication::class,
            BaseSearchActivity::class,
            MainActivity::class
        ],
        library = true
)
class AppModule(private val app: MyApplication) {
    @Provides
    @Singleton
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return app
    }
}