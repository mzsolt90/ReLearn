package com.azyoot.relearn.ui.common.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.azyoot.relearn.testing.viewmodels.TestViewModelEffect
import com.azyoot.relearn.testing.viewmodels.TestViewModelState
import com.azyoot.relearn.testing.viewmodels.TestViewModelStub
import com.azyoot.relearn.testing.viewmodels.getEffectsObserved
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

typealias ViewModelEffectAtPosition = ViewModelsList.ViewModelEffectAtPosition<TestViewModelEffect>

@ExperimentalCoroutinesApi
class ViewModelsListTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @After
    fun cleanup() {
        testCoroutineScope.cleanupTestCoroutines()
    }

    private fun List<ViewModelEffectAtPosition>.assertProducedEventsFrom(
        position: Int,
        effects: List<TestViewModelEffect>
    ) {
        assertThat(this)
            .hasSize(effects.size)
            .allMatch {
                it.position == position
            }
            .extracting<TestViewModelEffect> { it.effect }
            .isEqualTo(effects)
    }

    @Test
    fun `Given ViewModels When ViewModels added and effect produced in the last Then effect received in the last`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1))

            val vms = listOf(
                TestViewModelStub(testCoroutineScope),
                TestViewModelStub(testCoroutineScope)
            )

            vms.forEach { list.add(it) }

            val effectsProduced = getEffectsObserved(list)

            effectsToSend.forEach {
                vms.last().sendEffect(it)
            }

            effectsProduced.assertProducedEventsFrom(vms.size - 1, effectsToSend)
        }

    @Test
    fun `Given ViewModels When ViewModels added to specific position and effect produced in first Then effect received in the first`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1), TestViewModelEffect(EFFECT_2))

            val viewModelAtPosition0 = TestViewModelStub(testCoroutineScope)
            val viewModelAtPosition1 = TestViewModelStub(testCoroutineScope)
            list.add(viewModelAtPosition1)
            list.add(0, viewModelAtPosition0)

            val effectsProduced = getEffectsObserved(list)

            effectsToSend.forEach {
                viewModelAtPosition0.sendEffect(it)
            }

            effectsProduced.assertProducedEventsFrom(0, effectsToSend)
        }

    @Test
    fun `Given ViewModels When ViewModels added or removed and effect produced in a removed one Then effect not received`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1))

            val viewModelToDelete = TestViewModelStub(testCoroutineScope)
            list.add(TestViewModelStub(testCoroutineScope))
            list.add(viewModelToDelete)
            list.removeAt(1)

            val effectsProduced = mutableListOf<ViewModelEffectAtPosition>()

            effectsToSend.forEach {
                viewModelToDelete.sendEffect(it)
            }

            assertThat(effectsProduced).hasSize(0)
        }

    private fun givenViewModelList() =
        ViewModelsList<TestViewModelState, TestViewModelEffect, BaseAndroidViewModel<TestViewModelState, TestViewModelEffect>>(
            testCoroutineScope
        )

    companion object {
        const val EFFECT_1 = 1
        const val EFFECT_2 = 2
    }
}