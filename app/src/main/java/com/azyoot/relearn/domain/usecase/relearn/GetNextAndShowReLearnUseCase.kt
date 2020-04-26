package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import timber.log.Timber
import javax.inject.Inject

class GetNextAndShowReLearnUseCase @Inject constructor(
    private val getNextReLearnSourceUseCase: GetNextReLearnSourceUseCase,
    private val repository: RelearnEventRepository
) {

    suspend fun getNextAndShowReLearnUseCase() =
        getNextReLearnSourceUseCase.getNextReLearnSource()?.also {
            Timber.d("Next source is for ${it.sourceText}")
            repository.setLatestReLearnEventForSource(it, RelearnEventStatus.SHOWING)
        }
}