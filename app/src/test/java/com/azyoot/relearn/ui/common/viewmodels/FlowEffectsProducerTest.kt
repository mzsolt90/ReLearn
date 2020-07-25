package com.azyoot.relearn.ui.common.viewmodels

import com.azyoot.relearn.testing.viewmodels.getEffectsObserved
import com.squareup.burst.BurstJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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

            val items = getEffectsObserved(effectsProducer)

            config.effectsSent.forEach {
                effectsProducer.sendEffect(it)
            }

            assertThat(items).isEqualTo(
                (config.effectsSentBefore.lastOrNull()
                    ?.let { listOf(it) } ?: listOf())
                    .plus(config.effectsSent))
        }

    @Test
    fun `Given some effects sent and existing subscriber When new subscriber Then effects are not received`() =
        runBlockingTest {
            val effectsProducer = FlowEffectsProducer<Int>()

            val items = getEffectsObserved(effectsProducer)

            effectsProducer.sendEffect(EFFECT_1)
            effectsProducer.sendEffect(EFFECT_2)

            assertThat(items).isEqualTo(listOf(EFFECT_1, EFFECT_2))

            val items2 = getEffectsObserved(effectsProducer)

            assertThat(items2).isEmpty()
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