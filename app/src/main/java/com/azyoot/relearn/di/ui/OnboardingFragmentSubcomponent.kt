package com.azyoot.relearn.di.ui

import com.azyoot.relearn.ui.onboarding.OnboardingFragment
import com.azyoot.relearn.ui.onboarding.OnboardingFragmentParams
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ViewModelModule::class, AdapterModule::class])
interface OnboardingFragmentSubcomponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance params: OnboardingFragmentParams
        ): OnboardingFragmentSubcomponent
    }

    fun inject(fragment: OnboardingFragment)
}