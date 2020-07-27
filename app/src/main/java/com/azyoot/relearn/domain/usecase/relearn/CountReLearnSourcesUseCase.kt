package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountReLearnSourcesUseCase @Inject constructor(private val relearnEventRepository: RelearnEventRepository) {

    suspend fun countReLearnSourcesUseCase(): Int = withContext(Dispatchers.IO) {
        relearnEventRepository.getSourceRange()?.count ?: 0
    }
}