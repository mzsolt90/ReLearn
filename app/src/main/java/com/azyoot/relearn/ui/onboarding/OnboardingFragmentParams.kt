package com.azyoot.relearn.ui.onboarding

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class OnboardingScreen {
    WELCOME, HOW_IT_WORKS, ENABLE_ACCESSIBILITY, DONE
}

@Parcelize
data class OnboardingFragmentParams(
    val screen: OnboardingScreen
) : Parcelable