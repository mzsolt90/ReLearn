package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SyncReLearnsUseCase @Inject constructor(private val repository: RelearnEventRepository) {

    suspend fun syncReLearns() {
        withContext(Dispatchers.IO) {
            Timber.d("Syncing ReLearns")

            repository.reloadSourcesCache()
        }
    }
}