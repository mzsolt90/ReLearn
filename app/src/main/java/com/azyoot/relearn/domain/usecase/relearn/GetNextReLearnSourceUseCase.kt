package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import timber.log.Timber
import javax.inject.Inject

class GetNextReLearnSourceUseCase @Inject constructor(
    private val getOrderingNumberForNextReLearnSourceUseCase: GetOrderingNumberForNextReLearnSourceUseCase,
    private val getNextPredeterminedReLearnSourceUseCase: GetNextPredeterminedReLearnSourceUseCase,
    private val relearnEventRepository: RelearnEventRepository
) {

    suspend fun getNextReLearnSource(): ReLearnSource? {
        val predetermined =
            getNextPredeterminedReLearnSourceUseCase.getNextPredeterminedReLearnSource()
        if (predetermined != null)
            return predetermined
                .also {
                    Timber.d(
                        """Next source of text ${predetermined.sourceText}
                        | is predetermined with state ${predetermined.latestRelearnStatus}""".trimMargin()
                    )
                }

        val nextId =
            getOrderingNumberForNextReLearnSourceUseCase.getOrderingNumberForNextReLearnSource()
                ?: return null
        return relearnEventRepository.getNearestSource(nextId)
    }
}