package com.azyoot.relearn.ui.common.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.azyoot.relearn.testing.viewmodels.STATE_INIT
import com.azyoot.relearn.testing.viewmodels.TestViewModelState
import com.azyoot.relearn.testing.viewmodels.TestViewModelStub
import com.azyoot.relearn.testing.viewmodels.getObservedStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BaseAndroidViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @After
    fun cleanup() {
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun `Given new ViewModel When initial state set Then state is set`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = TestViewModelStub(testCoroutineScope)

            val statesObserved = getObservedStates(viewModel)

            assertThat(statesObserved)
                .isEqualTo(listOf(TestViewModelState(STATE_INIT)))
        }

}