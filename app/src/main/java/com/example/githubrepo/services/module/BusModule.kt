package com.example.githubrepo.services.module

import com.example.githubrepo.services.BusProvider
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by sarahneo on 18/10/17.
 */

@Module
class BusModule {
    @Provides
    @Singleton
    fun provideEventBus(): BusProvider {
        return BusProvider(Bus(ThreadEnforcer.MAIN))
    }
}