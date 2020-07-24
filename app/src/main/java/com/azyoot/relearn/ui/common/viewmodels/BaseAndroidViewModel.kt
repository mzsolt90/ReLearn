package com.azyoot.relearn.ui.common.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@ExperimentalCoroutinesApi
abstract class BaseAndroidViewModel<S : Any, E : Any>(initialState: S) : ViewModel(),
    ViewEffectsProducer<E> by FlowEffectsProducer() {

    protected val viewState: MutableStateFlow<S?> = MutableStateFlow(null)
    fun getViewState(): Flow<S> = viewState.filterNotNull().distinctUntilChanged()
    protected val currentViewState: S
        get() = viewState.value!!

    init {
        viewState.value = initialState
    }

    open val coroutineScope: CoroutineScope = viewModelScope
}