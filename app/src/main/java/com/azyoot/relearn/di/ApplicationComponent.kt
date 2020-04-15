package com.azyoot.relearn.di

import android.content.Context
import com.azyoot.relearn.data.AppDatabase
import com.azyoot.relearn.service.di.MainFragmentSubcomponent
import com.azyoot.relearn.service.di.ServiceSubcomponent
import com.azyoot.relearn.service.di.WorkerSubcomponent
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [DataModule::class, AnalyticsModule::class, MathModule::class, HttpModule::class])
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }

    fun serviceSubcomponentBuilder(): ServiceSubcomponent.Builder
    fun mainFragmentSubcomponent(): MainFragmentSubcomponent
    fun workerSubcomponent(): WorkerSubcomponent
}