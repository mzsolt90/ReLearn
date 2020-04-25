package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetNextPredeterminedReLearnSourceUseCase @Inject constructor(private val repository: RelearnEventRepository) {

    suspend fun getNextPredeterminedReLearnSource() = withContext(Dispatchers.IO) {
        repository.getShowingReLearnEventSource() ?: repository.getOldestPendingReLearnEventSource()
    }
}