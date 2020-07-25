package com.azyoot.relearn.testing.viewmodels

import com.azyoot.relearn.ui.common.viewmodels.BaseAndroidViewModel
import com.azyoot.relearn.ui.common.viewmodels.ViewModelsListTest
import kotlinx.coroutines.CoroutineScope

class TestViewModelStub(viewModelScope: CoroutineScope) :
    BaseAndroidViewModel<TestViewModelState, TestViewModelEffect>(
        TestViewModelState(STATE_INIT),
        viewModelScope
    ) {
    fun setState(state: TestViewModelState) {
        viewState.value = state
    }
}

data class TestViewModelState(val state: Int)
data class TestViewModelEffect(val effect: Int)

const val STATE_INIT = 1