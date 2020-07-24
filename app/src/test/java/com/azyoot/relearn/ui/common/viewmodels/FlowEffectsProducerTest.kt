package com.azyoot.relearn.ui.common.viewmodels

import com.squareup.burst.BurstJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(BurstJUnit4::class)
class FlowEffectsProducerTest {

    @Test
    fun `Given some effects sent before When new effects is sent Then effects are received`(config: EffectsTestConfig) =
        runBlockingTest {

            val effectsProducer = FlowEffectsProducer<Int>()

            config.effectsSentBefore.forEach {
                effectsProducer.sendEffect(it)
            }

            launch {
                val items = mutableListOf<Int>()

                withTimeout(1000) {
                    effectsProducer.getEffects().toList(items)
                }

                assertThat(items).isEqualTo(config.effectsSent)
            }

            config.effectsSent.forEach {
                effectsProducer.sendEffect(it)
            }
        }

    @Test
    fun `Given some effects sent and existing subscriber When new subscriber Then effects are not received`() =
        runBlockingTest {
            val effectsProducer = FlowEffectsProducer<Int>()

            launch {
                val items = mutableListOf<Int>()
                withTimeout(1000) {
                    effectsProducer.getEffects().toList(items)
                }
                assertThat(items).isEqualTo(listOf(EFFECT_1, EFFECT_2))
            }

            effectsProducer.sendEffect(EFFECT_1)
            effectsProducer.sendEffect(EFFECT_2)

            launch {
                val items = mutableListOf<Int>()
                withTimeout(1000) {
                    effectsProducer.getEffects().toList(items)
                }
                assertThat(items).isEmpty()
            }
        }

    enum class EffectsTestConfig(
        val effectsSentBefore: List<Int>,
        val effectsSent: List<Int>
    ) {
        EMPTY_BEFORE_EMPTY_SENT(listOf(), listOf()),
        EMPTY_BEFORE_ONE_SENT(listOf(), listOf(EFFECT_1)),
        EMPTY_BEFORE_TWO_DIFFERENT_SENT(listOf(), listOf(EFFECT_1, EFFECT_2)),
        EMPTY_BEFORE_TWO_SAME_SENT(listOf(), listOf(EFFECT_1, EFFECT_1)),
        NON_EMPTY_BEFORE_SAME_SENT(listOf(EFFECT_1), listOf(EFFECT_1))
    }

    companion object {
        const val EFFECT_1 = 1
        const val EFFECT_2 = 2
    }
}