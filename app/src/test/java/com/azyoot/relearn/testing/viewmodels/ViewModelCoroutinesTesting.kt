package com.azyoot.relearn.testing.viewmodels

import com.azyoot.relearn.ui.common.viewmodels.BaseAndroidViewModel
import com.azyoot.relearn.ui.common.viewmodels.ViewEffectsProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@ExperimentalCoroutinesApi
fun <S : Any, E: Any> CoroutineScope.getObservedStates(viewModel: BaseAndroidViewModel<S, E>): List<S> {
    val statesObserved = mutableListOf<S>()
    launch {
        withTimeout(1000) {
            viewModel.getViewState().toList(statesObserved)
        }
    }
    return statesObserved
}

@ExperimentalCoroutinesApi
fun <E: Any> CoroutineScope.getObservedEffects(viewModel: ViewEffectsProducer<E>): List<E> {
    val effectsObserved = mutableListOf<E>()
    launch {
        withTimeout(1000) {
            viewModel.getEffects().toList(effectsObserved)
        }
    }
    return effectsObserved
}