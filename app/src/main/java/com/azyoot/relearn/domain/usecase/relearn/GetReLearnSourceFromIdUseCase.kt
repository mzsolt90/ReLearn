package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetReLearnSourceFromIdUseCase @Inject constructor(private val relearnEventRepository: RelearnEventRepository) {

    suspend fun getReLearnSourceFromIdUseCase(id: Long, type: SourceType): ReLearnSource? =
        withContext(Dispatchers.IO) {
            relearnEventRepository.getSourceFromId(id, type)
        }
}