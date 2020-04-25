package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.data.repository.WebpageVisitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountUnparsedWebpagesUseCase @Inject constructor(private val repository: WebpageVisitRepository) {
    suspend fun countUntranslatedWebpages(): Int = withContext(Dispatchers.IO) {
        return@withContext repository.getUnparsedWebpageVisitCount()
    }
}