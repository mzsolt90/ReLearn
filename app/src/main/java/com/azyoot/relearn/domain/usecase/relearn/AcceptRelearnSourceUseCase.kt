package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AcceptRelearnSourceUseCase @Inject constructor(private val repository: RelearnEventRepository){

    suspend fun acceptRelearnUseCase(source: ReLearnSource) {
        withContext(Dispatchers.IO) {
            repository.setLatestReLearnEventForSource(source, RelearnEventStatus.ACCEPTED)
        }
    }
}