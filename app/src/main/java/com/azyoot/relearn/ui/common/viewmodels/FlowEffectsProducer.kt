package com.azyoot.relearn.ui.common.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
internal class FlowEffectsProducer<E : Any> :
    ViewEffectsProducer<E> {

    data class EffectState<E>(val isSent: Boolean = true, val effect: E?)

    private val effects = MutableStateFlow(
        EffectState<E>(
            effect = null
        )
    )

    override fun sendEffect(effect: E) {
        effects.value =
            EffectState(
                isSent = false,
                effect = effect
            )
    }

    override fun getEffects(): Flow<E> = effects
        .filter { !it.isSent && it.effect != null }
        .onEach { effects.value = it.copy(isSent = true) }
        .map { it.effect!! }
}