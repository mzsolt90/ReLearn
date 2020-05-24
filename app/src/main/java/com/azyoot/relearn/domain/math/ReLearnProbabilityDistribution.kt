package com.azyoot.relearn.domain.math

import java.util.*
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow

/**
 * Generates a random value based on the cumulative probability function (cdf) of
 * pow(tan((2x - 1) * PI / 4) + 1, 1 - OLD_BIAS). The generation works with the inversion method,
 * generating a uniform random value and substituting it into the inverse of the cdf.
 * You can see the resulting density function:
 * https://www.wolframalpha.com/input/?i=derivative+of+%28%28tan%28%28+2+*x++-+1%29+*+PI+%2F++4%29+%2B+1+%29++%2F+2%29+%5E+0.91+from+x%3D0+to+1+
 */
class ReLearnProbabilityDistribution @Inject constructor(private val random: Random) {

    private fun invertedCdfValue(x: Double) =
        ((4 / PI) * atan(2 * x.pow(1 / (1 - OLD_BIAS)) - 1) + 1) / 2

    fun getNextValue() = invertedCdfValue(random.nextDouble())

    companion object {
        private const val OLD_BIAS = 0.15
    }
}