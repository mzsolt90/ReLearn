package com.azyoot.relearn.domain.math

import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

class BoxMuellerCalculation @Inject constructor(private val random: Random) {
    fun getGaussianValuePair(mu: Double, sigma: Double): Pair<Double, Double> {
        val u1 = random.nextDouble()
        val u2 = random.nextDouble()

        return Pair(
            sqrt(-2 * ln(u1)) * cos(TWO_PI * u2),
            sqrt(-2 * ln(u1)) * sin(TWO_PI * u2)
        ).let {
            Pair(it.first * sigma + mu, it.second * sigma + mu)
        }
    }

    companion object {
        const val TWO_PI = 2.0 * Math.PI
    }
}