package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.domain.entity.SourceRange
import com.azyoot.relearn.domain.math.ReLearnProbabilityDistribution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetRandomOrderingNumberFromSourceRangeUseCase @Inject constructor(
    private val reLearnProbabilityDistribution: ReLearnProbabilityDistribution
) {
    suspend fun getRandomOrderingNumberFromSourceRangeUseCase(range: SourceRange) =
        withContext(Dispatchers.Default) {
            Timber.v("Calculating next relearn source in range $range")

            val randomValue = reLearnProbabilityDistribution.getNextValue()

            Timber.d("Next random value is $randomValue")

            val rangeSize = range.maxOrderingNumber - range.minOrderingNumber

            (range.minOrderingNumber.toDouble() + rangeSize.toDouble() * randomValue).toInt()
        }
}