package com.azyoot.relearn.service.di

import com.azyoot.relearn.service.MonitoringService
import dagger.BindsInstance
import dagger.Subcomponent
import kotlinx.coroutines.CoroutineScope

@ServiceScope
@Subcomponent(modules = [UseCasesModule::class])
interface ServiceSubcomponent {
    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun coroutineScope(coroutineScope: CoroutineScope): Builder

        fun build(): ServiceSubcomponent
    }

    fun inject(service: MonitoringService)
}