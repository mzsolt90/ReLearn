package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import javax.inject.Inject

class GetNextAndShowReLearnUseCase @Inject constructor(
    private val getNextReLearnSourceUseCase: GetNextReLearnSourceUseCase,
    private val repository: RelearnEventRepository
) {

    suspend fun getNextAndShowReLearnUseCase() =
        getNextReLearnSourceUseCase.getNextReLearnSource()?.also {
            repository.setLatestReLearnEventForSource(it, RelearnEventStatus.SHOWING)
        }
}