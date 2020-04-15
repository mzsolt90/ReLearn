package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.RelearnEventRepository
import com.azyoot.relearn.domain.math.BoxMuellerCalculation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetIdForNextReLearnSourceUseCase @Inject constructor(
    private val relearnEventRepository: RelearnEventRepository,
    private val boxMuellerCalculation: BoxMuellerCalculation
) {

    private fun flipAndThresholdGauss(value: Double) = when {
        value <= -1 -> 0.0
        value <= 0 -> -1.0 - value
        value <= 1 -> 1.0 - value
        else -> 0.0
    }

    suspend fun getIdForNextReLearnSource(): Int? {
        val range = withContext(Dispatchers.IO) {
            relearnEventRepository.getSourceRange()
        }
        range ?: return null

        val gaussValue = boxMuellerCalculation.getGaussianValuePair(0.0, 0.3).first
        val flippedValue = flipAndThresholdGauss(gaussValue)
        val centeredValue = flippedValue / 2.0 + 0.5

        val rangeSize = range.maxId - range.minId

        return (range.minId.toDouble() + rangeSize.toDouble() * centeredValue).toInt()
    }
}