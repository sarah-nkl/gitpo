package com.example.githubrepo

import android.app.Application
import com.example.githubrepo.services.module.AppModule
import dagger.ObjectGraph

class MyApplication : Application() {

    lateinit var objectGraph: ObjectGraph
        private set

    override fun onCreate() {
        super.onCreate()
        objectGraph = ObjectGraph.create(AppModule(this))
        objectGraph.inject(this)
    }

    fun inject(obj: Any) {
        objectGraph.inject(obj)
    }
}