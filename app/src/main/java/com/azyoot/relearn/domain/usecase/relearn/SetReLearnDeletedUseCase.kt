package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SetReLearnDeletedUseCase @Inject constructor(private val relearnEventRepository: RelearnEventRepository) {
    suspend fun setReLearnDeleted(source: ReLearnSource, isDeleted: Boolean) =
        withContext(Dispatchers.IO) {
            Timber.d("Setting source ${source.latestSourceId} deleted: $isDeleted")
            relearnEventRepository.setReLearnDeleted(source, isDeleted)
        }
}