package com.azyoot.relearn.ui.common.viewmodels

import kotlinx.coroutines.flow.Flow

interface ViewEffectsProducer<E> {
    fun getEffects(): Flow<E>
    fun sendEffect(effect: E)
}