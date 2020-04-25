package com.azyoot.relearn.di.ui

import com.azyoot.relearn.ui.main.MainFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ViewModelModule::class])
interface MainFragmentSubcomponent {
    fun inject(fragment: MainFragment)
}