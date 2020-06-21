package com.azyoot.relearn.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
internal class AndroidEffectsProducer<E : Any> : ViewEffectsProducer<E> {

    data class EffectState<E>(val isSent: Boolean = true, val effect: E?)

    private val effects = MutableStateFlow(EffectState<E>(effect = null))
    override fun sendEffect(effect: E) {
        effects.value = EffectState(isSent = false, effect = effect)
    }

    override fun getEffects(): Flow<E> = effects
        .filter { !it.isSent && it.effect != null }
        .onEach { effects.value = it.copy(isSent = true) }
        .map { it.effect!! }
}

@ExperimentalCoroutinesApi
abstract class BaseAndroidViewModel<S : Any, E : Any>(initialState: S) : ViewModel(),
    ViewEffectsProducer<E> by AndroidEffectsProducer() {

    protected val viewState: MutableStateFlow<S?> = MutableStateFlow(null)
    fun getViewState(): Flow<S> = viewState.filterNotNull().distinctUntilChanged()
    protected val currentViewState: S
        get() = viewState.value!!

    init {
        viewState.value = initialState
    }

    open val coroutineScope: CoroutineScope = viewModelScope
}