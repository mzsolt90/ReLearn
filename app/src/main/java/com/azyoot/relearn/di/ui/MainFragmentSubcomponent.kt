package com.azyoot.relearn.di.ui

import androidx.lifecycle.LifecycleOwner
import com.azyoot.relearn.ui.main.MainFragment
import dagger.BindsInstance
import dagger.Subcomponent
import kotlinx.coroutines.CoroutineScope

@FragmentScope
@Subcomponent(modules = [ViewModelModule::class, AdapterModule::class])
interface MainFragmentSubcomponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance coroutineScope: CoroutineScope
        ): MainFragmentSubcomponent
    }

    fun inject(fragment: MainFragment)
}