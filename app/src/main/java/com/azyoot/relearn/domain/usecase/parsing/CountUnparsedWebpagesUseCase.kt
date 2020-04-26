package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.data.repository.WebpageVisitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CountUnparsedWebpagesUseCase @Inject constructor(private val repository: WebpageVisitRepository) {
    suspend fun countUntranslatedWebpages(): Int = withContext(Dispatchers.IO) {
        return@withContext repository.getUnparsedWebpageVisitCount().also { Timber.d("We have $it unparsed webpage visits") }
    }
}