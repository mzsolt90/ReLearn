package com.azyoot.relearn.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.usecase.relearn.CountReLearnSourcesUseCase
import com.azyoot.relearn.domain.usecase.relearn.SyncReLearnsUseCase
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.service.worker.ReLearnWorker
import com.azyoot.relearn.ui.common.BaseAndroidViewModel
import com.azyoot.relearn.ui.relearn.ReLearnPeriodicScheduler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
            withContext(Dispatchers.Main){
                stateInternal.value = MainViewState.Loaded(count, MonitoringService.isRunning(applicationContext))
            }
        }
    }

    fun scheduleReLearn() {
        reLearnPeriodicScheduler.schedule()
    }

    fun refresh(){
        coroutineScope.launch {
            syncReLearnsUseCase.syncReLearns()
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()
            withContext(Dispatchers.Main){
                stateInternal.value = MainViewState.Loaded(count, MonitoringService.isRunning(applicationContext))
            }
        }
    }
}
