package com.azyoot.relearn.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseAndroidViewModel<S> : ViewModel() {
    protected val stateInternal : MutableLiveData<S> = MutableLiveData()
    val stateLiveData : LiveData<S>
    get() = stateInternal

    val currentState: S
    get() = stateLiveData.value ?: initialState

    protected abstract val initialState: S

    init {
        stateInternal.value = initialState
    }
}