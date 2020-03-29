package com.azyoot.relearn.di

import android.content.Context
import com.azyoot.relearn.service.di.MainFragmentSubcomponent
import com.azyoot.relearn.service.di.ServiceSubcomponent
import com.azyoot.relearn.service.di.WorkerSubcomponent
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [DataModule::class, AnalyticsModule::class, ViewModelModule::class])
abstract class ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder

        fun build(): ApplicationComponent
    }

    abstract fun serviceSubcomponentBuilder(): ServiceSubcomponent.Builder
    abstract fun mainFragmentSubcomponent(): MainFragmentSubcomponent
    abstract fun workerSubcomponent(): WorkerSubcomponent
}