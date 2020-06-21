package com.azyoot.relearn.ui.common

import kotlinx.coroutines.flow.Flow

interface ViewEffectsProducer<E> {
    fun getEffects(): Flow<E>
    fun sendEffect(effect: E)
}