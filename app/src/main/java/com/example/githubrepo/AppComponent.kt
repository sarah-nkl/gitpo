package com.example.githubrepo

import com.example.githubrepo.services.module.AppModule
import com.example.githubrepo.services.module.BusModule
import com.example.githubrepo.services.module.MyApplicationModule
import com.example.githubrepo.services.module.NetModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            MyApplicationModule::class,
            AppModule::class,
            BusModule::class,
            NetModule::class
        ]
)
interface AppComponent : AndroidInjector<MyApplication> {

    override fun inject(app: MyApplication)

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MyApplication>() {
        abstract fun appModule(appModule: AppModule): Builder

        abstract override fun build(): AppComponent
    }
}