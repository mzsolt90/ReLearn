package com.azyoot.relearn.service.di

import com.azyoot.relearn.di.ViewModelModule
import com.azyoot.relearn.ui.main.MainFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ViewModelModule::class])
interface MainFragmentSubcomponent {
    fun inject(fragment: MainFragment)
}