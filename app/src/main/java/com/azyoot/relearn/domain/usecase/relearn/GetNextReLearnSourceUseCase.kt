package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import javax.inject.Inject

class GetNextReLearnSourceUseCase @Inject constructor(private val getIdForNextReLearnSourceUseCase: GetIdForNextReLearnSourceUseCase,
                                                      private val getNextPredeterminedReLearnSourceUseCase: GetNextPredeterminedReLearnSourceUseCase,
                                                      private val relearnEventRepository: RelearnEventRepository
) {

    suspend fun getNextReLearnSource(): ReLearnSource? {
        val predetermined = getNextPredeterminedReLearnSourceUseCase.getNextPredeterminedReLearnSource()
        if(predetermined != null) return predetermined

        val nextId = getIdForNextReLearnSourceUseCase.getIdForNextReLearnSource() ?: return null
        return relearnEventRepository.getNearestSource(nextId)
    }
}