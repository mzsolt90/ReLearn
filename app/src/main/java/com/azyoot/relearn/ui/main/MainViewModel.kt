package com.azyoot.relearn.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.azyoot.relearn.service.MonitoringService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(private val applicationContext: Context) : ViewModel() {

    private val checkMonitoringServiceChannel = ConflatedBroadcastChannel<Unit>()
    val isMonitoringServiceEnabled = checkMonitoringServiceChannel.asFlow().map {
        MonitoringService.isRunning(applicationContext)
    }.distinctUntilChanged().asLiveData()

    init {
        checkMonitoringService()
    }

    fun checkMonitoringService() {
        viewModelScope.launch {
            checkMonitoringServiceChannel.send(Unit)
        }
    }
}
