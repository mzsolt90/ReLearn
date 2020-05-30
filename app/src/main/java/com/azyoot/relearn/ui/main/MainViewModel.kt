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
) : BaseAndroidViewModel<MainViewState>() {

    override val initialState = MainViewState.Initial

    init {
        loadData()
        scheduleReLearn()
    }

    private fun loadData() {
        stateInternal.value = MainViewState.Loading

        viewModelScope.launch {
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()
            withContext(Dispatchers.Main) {
                stateInternal.value =
                    MainViewState.Loaded(
                        count,
                        MonitoringService.isRunning(applicationContext),
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
            stateInternal.value = MainViewState.Loading

            syncReLearnsUseCase.syncReLearns()
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()

            withContext(Dispatchers.Main) {
                stateInternal.value =
                    MainViewState.Loaded(
                        count,
                        MonitoringService.isRunning(applicationContext),
                        previousPage?.let { if(it >= count) null else it } ?: getDefaultPage(count)
                    )
            }
        }
    }

    fun onPageChanged(page: Int) {
        currentState.let {
            if (it !is MainViewState.Loaded) return@let
            stateInternal.value = it.copy(page = page)
        }
    }

    fun getDefaultPage(relearnCount: Int) = relearnCount
}
