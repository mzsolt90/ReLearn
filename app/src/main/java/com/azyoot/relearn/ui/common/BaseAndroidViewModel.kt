package com.azyoot.relearn.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
abstract class BaseAndroidViewModel<S : Any, E : Any>(initialState: S) : ViewModel() {
    protected val state: MutableStateFlow<S?> = MutableStateFlow(null)
    fun state(): Flow<S> = state.filterNotNull().distinctUntilChanged()
    val currentState: S
        get() = state.value!!

    private val effectSent = MutableStateFlow(true)
    private val effects: MutableStateFlow<E?> = MutableStateFlow(null)
    protected fun sendEffect(effect: E){
        effects.value = effect
        effectSent.value = false
    }
    fun effects(): Flow<E> = effects.filterNotNull().distinctUntilChangedBy { !effectSent.value }.onEach { effectSent.value = true }

    init {
        state.value = initialState
    }

    val coroutineScope: CoroutineScope = viewModelScope
}