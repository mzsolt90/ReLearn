package com.azyoot.relearn.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

abstract class BaseAndroidViewModel<S, E>(initialState: S) : ViewModel() {
    protected val stateInternal : MutableLiveData<S> = MutableLiveData()
    val stateLiveData : LiveData<S>
    get() = stateInternal

    val currentState: S
    get() = stateLiveData.value!!

    protected val effectsInternal : MutableLiveData<E> = MutableLiveData()
    val effectsLiveData : LiveData<E>
    get() = effectsInternal

    init {
        stateInternal.value = initialState
    }

    val coroutineScope: CoroutineScope = viewModelScope
}