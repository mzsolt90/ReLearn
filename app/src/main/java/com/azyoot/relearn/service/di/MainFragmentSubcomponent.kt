package com.azyoot.relearn.service.di

import com.azyoot.relearn.ui.main.MainFragment
import dagger.Subcomponent

@Subcomponent
abstract class MainFragmentSubcomponent {
    abstract fun inject(fragment: MainFragment)
}