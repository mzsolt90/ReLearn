package com.azyoot.relearn.ui.common.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

typealias ViewModelEffectAtPosition = ViewModelsList.ViewModelEffectAtPosition<ViewModelsListTest.TestViewModelEffect>

@ExperimentalCoroutinesApi
class ViewModelsListTest {

    class TestViewModelStub : BaseAndroidViewModel<TestViewModelState, TestViewModelEffect>(
        TestViewModelState(STATE_INIT)
    ) {
        fun setState(state: TestViewModelState) {
            viewState.value = state
        }
    }

    data class TestViewModelState(val state: Int)
    data class TestViewModelEffect(val effect: Int)

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
    fun `Given viewmodels When viewmodels added and effect produced in the last Then effect received in the last`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsProduced = mutableListOf<ViewModelEffectAtPosition>()
            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1))

            val vms = listOf(
                TestViewModelStub(),
                TestViewModelStub()
            )

            vms.forEach { list.add(it) }

            launch {
                withTimeout(1000) {
                    list.getEffects().toList(effectsProduced)
                }
            }
            effectsToSend.forEach {
                vms.last().sendEffect(it)
            }

            effectsProduced.assertProducedEventsFrom(vms.size - 1, effectsToSend)
        }

    @Test
    fun `Given viewmodels When viewmodels added to specific position and effect produced in first Then effect received in the first`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsProduced = mutableListOf<ViewModelEffectAtPosition>()
            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1), TestViewModelEffect(EFFECT_2))

            val viewModelAtPosition0 = TestViewModelStub()
            val viewModelAtPosition1 = TestViewModelStub()
            list.add(viewModelAtPosition1)
            list.add(0, viewModelAtPosition0)

            launch {
                withTimeout(1000) {
                    list.getEffects().toList(effectsProduced)
                }
            }
            effectsToSend.forEach {
                viewModelAtPosition0.sendEffect(it)
            }

            effectsProduced.assertProducedEventsFrom(0, effectsToSend)
        }

    @Test
    fun `Given viewmodels When viewmodels added or removed and effect produced in a removed one Then effect not received`() =
        testCoroutineScope.runBlockingTest {
            val list = givenViewModelList()

            val effectsProduced = mutableListOf<ViewModelEffectAtPosition>()
            val effectsToSend = listOf(TestViewModelEffect(EFFECT_1))

            val viewModelToDelete = TestViewModelStub()
            list.add(TestViewModelStub())
            list.add(viewModelToDelete)
            list.removeAt(1)

            launch {
                withTimeout(1000) {
                    list.getEffects().toList(effectsProduced)
                }
            }
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
        const val STATE_INIT = 1

        const val EFFECT_1 = 1
        const val EFFECT_2 = 2
    }
}