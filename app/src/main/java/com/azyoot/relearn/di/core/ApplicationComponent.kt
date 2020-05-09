package com.azyoot.relearn.di.core

import android.content.Context
import com.azyoot.relearn.di.service.MathModule
import com.azyoot.relearn.di.service.ServiceSubcomponent
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.di.ui.MainFragmentSubcomponent
import com.azyoot.relearn.service.receiver.ReLearnNotificationActionsReceiver
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

    fun inject(notificationsReceiver: ReLearnNotificationActionsReceiver)
}