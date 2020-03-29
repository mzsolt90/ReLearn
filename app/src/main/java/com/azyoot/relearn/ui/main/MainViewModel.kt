package com.azyoot.relearn.ui.main

import android.content.Context
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.azyoot.relearn.data.AppDatabase
import com.azyoot.relearn.data.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.service.MonitoringService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(private val applicationContext: Context, private val repository: WebpageVisitRepository) : ViewModel() {

    val history: MutableLiveData<List<WebpageVisit>> = MutableLiveData()

    private val checkMonitoringServiceChannel = ConflatedBroadcastChannel<Unit>()
    val isMonitoringServiceEnabled = checkMonitoringServiceChannel.asFlow().map {
        MonitoringService.isRunning(applicationContext)
    }.distinctUntilChanged().asLiveData()

    init {
        checkMonitoringService()
        loadData()
    }

    fun loadData(){
        viewModelScope.launch(Dispatchers.IO) {
            history.postValue(repository.getWebpageVisitsByTimeDesc())
        }
    }

    fun checkMonitoringService() {
        viewModelScope.launch {
            checkMonitoringServiceChannel.send(Unit)
        }
    }
}
