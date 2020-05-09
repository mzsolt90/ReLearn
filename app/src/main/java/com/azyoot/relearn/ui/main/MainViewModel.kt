package com.azyoot.relearn.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.domain.usecase.relearn.*
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.service.worker.ReLearnWorker
import com.azyoot.relearn.ui.relearn.ReLearnScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(
    private val applicationContext: Context,
    private val repository: WebpageVisitRepository,
    private val reLearnScheduler: ReLearnScheduler
) : ViewModel() {

    val history: MutableLiveData<List<WebpageVisit>> = MutableLiveData()

    private val checkMonitoringServiceChannel = ConflatedBroadcastChannel<Unit>()
    val isMonitoringServiceEnabled = checkMonitoringServiceChannel.asFlow().map {
        MonitoringService.isRunning(applicationContext)
    }.distinctUntilChanged().asLiveData()

    init {
        checkMonitoringService()
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            history.postValue(repository.getWebpageVisitsByTimeDesc())
        }
    }

    fun checkMonitoringService() {
        viewModelScope.launch {
            checkMonitoringServiceChannel.send(Unit)
        }
    }

    fun testReLearn() {
        val req = OneTimeWorkRequestBuilder<ReLearnWorker>().build()

        WorkManager.getInstance(applicationContext).enqueue(req)
    }

    fun scheduleReLearn() {
        reLearnScheduler.schedule()
    }
}
