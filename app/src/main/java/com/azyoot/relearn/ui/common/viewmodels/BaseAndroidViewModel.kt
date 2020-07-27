package com.azyoot.relearn.ui.common.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import androidx.lifecycle.viewModelScope as AndroidViewModelScope

@ExperimentalCoroutinesApi
abstract class BaseAndroidViewModel<S : Any, E : Any>(
    initialState: S,
    private val viewModelScopeOverride: CoroutineScope? = null
) : ViewModel(),
    ViewEffectsProducer<E> by FlowEffectsProducer() {

    protected val viewModelScope: CoroutineScope
        get() = viewModelScopeOverride ?: AndroidViewModelScope

    protected val viewState: MutableStateFlow<S?> = MutableStateFlow(null)
    fun getViewState(): Flow<S> = viewState.filterNotNull()
    protected val currentViewState: S
        get() = viewState.value!!

    init {
        viewState.value = initialState
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}