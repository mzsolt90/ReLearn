package com.azyoot.relearn.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [DataModule::class, AnalyticsModule::class])
abstract class ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder

        fun build(): ApplicationComponent
    }

    abstract fun serviceSubcomponentBuilder(): ServiceSubcomponent.Builder
}