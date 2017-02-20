package com.example.githubrepo.services.module;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sarah_neo on 22/02/2017.
 */

@Module(
        library  = true,
        complete = false
)
public class BusModule {
    @Provides
    @Singleton
    public Bus provideEventBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }
}
