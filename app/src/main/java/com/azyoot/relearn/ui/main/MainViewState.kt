package com.azyoot.relearn.ui.main

sealed class MainViewState {
    object Initial : MainViewState()
    object Loading : MainViewState()
    data class Loaded(val sourceCount: Int,
                      val page: Int): MainViewState()
}

sealed class MainViewEffect {
    object EnableAccessibilityService : MainViewEffect()
}