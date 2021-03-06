package com.azyoot.relearn.ui.common.viewmodels

import androidx.lifecycle.ViewModel
import com.azyoot.relearn.di.ui.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ViewModelsList<S : Any, E : Any, VM : BaseAndroidViewModel<S, E>>
@Inject
constructor(@ViewModelScope private val viewModelScope: CoroutineScope) : ViewModel(),
    ViewEffectsProducer<ViewModelsList.ViewModelEffectAtPosition<E>> by FlowEffectsProducer() {

    data class ViewModelEffectAtPosition<E>(val position: Int, val effect: E)

    private class ViewModelAndJob<VM>(val viewModel: VM, val job: Job)

    private val viewModels: MutableList<ViewModelAndJob<VM>> = mutableListOf()

    private fun getViewModelJob(viewModel: VM) =
        viewModel.getEffects().onEach {
            val position = viewModels.indexOfFirst { vm ->
                vm.viewModel === viewModel
            }

            sendEffect(
                ViewModelEffectAtPosition(
                    position,
                    it
                )
            )
        }.launchIn(viewModelScope)

    fun removeAt(position: Int) {
        viewModels[position].job.cancel()
        viewModels.removeAt(position)
    }

    fun add(viewModel: VM) {
        viewModels.add(
            ViewModelAndJob(
                viewModel,
                job = getViewModelJob(viewModel)
            )
        )
    }

    fun add(position: Int, viewModel: VM) {
        viewModels.add(
            position,
            ViewModelAndJob(
                viewModel,
                job = getViewModelJob(viewModel)
            )
        )
    }

    operator fun get(i: Int) = viewModels[i].viewModel

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}