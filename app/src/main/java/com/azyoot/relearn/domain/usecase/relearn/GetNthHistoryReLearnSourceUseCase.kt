package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetNthHistoryReLearnSourceUseCase @Inject constructor(private val repository: RelearnEventRepository) {
    suspend fun getNthHistoryReLearnSourceUseCase(n: Int) = withContext(Dispatchers.IO) {
        repository.getNthLatestNotShowingSource(n)
    }
}