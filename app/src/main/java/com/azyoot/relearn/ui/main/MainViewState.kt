package com.azyoot.relearn.ui.main

import com.azyoot.relearn.ui.onboarding.OnboardingScreen

sealed class MainViewState {
    object Initial : MainViewState()
    object Loading : MainViewState()
    data class Onboarding(val screen: OnboardingScreen) : MainViewState()
    data class Loaded(
        val sourceCount: Int,
        val page: Int
    ) : MainViewState()
}

sealed class MainViewEffect {
    object EnableAccessibilityService : MainViewEffect()
}