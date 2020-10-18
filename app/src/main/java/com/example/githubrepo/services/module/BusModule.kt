package com.example.githubrepo.services.module

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(library = true, complete = false)
class BusModule {
    @Provides
    @Singleton
    fun provideEventBus(): Bus {
        return Bus(ThreadEnforcer.MAIN)
    }
}