package com.azyoot.relearn.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
abstract class BaseAndroidViewModel<S : Any, E : Any>(initialState: S) : ViewModel() {
    protected val viewState: MutableStateFlow<S?> = MutableStateFlow(null)
    fun getViewState(): Flow<S> = viewState.filterNotNull().distinctUntilChanged()
    val currentViewState: S
        get() = viewState.value!!

    private val effectSent = MutableStateFlow(true)
    private val effects: MutableStateFlow<E?> = MutableStateFlow(null)
    protected fun sendEffect(effect: E){
        effects.value = effect
        effectSent.value = false
    }
    fun getEffects(): Flow<E> = effects.filterNotNull().distinctUntilChangedBy { !effectSent.value }.onEach { effectSent.value = true }

    init {
        viewState.value = initialState
    }

    open val coroutineScope: CoroutineScope = viewModelScope
}