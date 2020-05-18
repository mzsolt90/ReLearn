package com.azyoot.relearn.ui.main

import com.azyoot.relearn.domain.entity.SourceRange

sealed class MainViewState {
    object Initial : MainViewState()
    object Loading : MainViewState()
    data class Loaded(val sourceCount: Int,
                      val isServiceEnabled: Boolean,
                      val page: Int): MainViewState()
}