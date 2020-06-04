package com.azyoot.relearn.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.azyoot.relearn.domain.usecase.relearn.CountReLearnSourcesUseCase
import com.azyoot.relearn.domain.usecase.relearn.SyncReLearnsUseCase
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.ui.common.BaseAndroidViewModel
import com.azyoot.relearn.ui.relearn.ReLearnPeriodicScheduler
import kotlinx.coroutines.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(
    private val applicationContext: Context,
    private val reLearnPeriodicScheduler: ReLearnPeriodicScheduler,
    private val countReLearnSourcesUseCase: CountReLearnSourcesUseCase,
    private val syncReLearnsUseCase: SyncReLearnsUseCase
) : BaseAndroidViewModel<MainViewState, MainViewEffect>(MainViewState.Initial) {

    init {
        checkAccessibilityService()
        loadData()
        scheduleReLearn()
    }

    private fun checkAccessibilityService() {
        if (!MonitoringService.isRunning(applicationContext)) {
            coroutineScope.launch {
                sendEffect(MainViewEffect.EnableAccessibilityService)
            }
        }
    }

    private fun loadData() {
        state.value = MainViewState.Loading

        viewModelScope.launch {
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()
            withContext(Dispatchers.Main) {
                state.value =
                    MainViewState.Loaded(
                        count,
                        getDefaultPage(count)
                    )
            }
        }
    }

    fun scheduleReLearn() {
        reLearnPeriodicScheduler.schedule()
    }

    fun refresh() {
        coroutineScope.launch {
            val previousPage = (currentState as? MainViewState.Loaded)?.page
            state.value = MainViewState.Loading

            syncReLearnsUseCase.syncReLearns()
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()

            withContext(Dispatchers.Main) {
                state.value =
                    MainViewState.Loaded(
                        count,
                        previousPage?.let { if (it >= count) null else it } ?: getDefaultPage(count)
                    )
            }
        }
    }

    fun onPageChanged(page: Int) {
        currentState.let {
            if (it !is MainViewState.Loaded) return@let
            state.value = it.copy(page = page)
        }
    }

    private fun getDefaultPage(relearnCount: Int) = relearnCount
}
