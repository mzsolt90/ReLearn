package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.util.isValidUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DeleteWebpageVisitUseCase @Inject constructor(private val repository: WebpageVisitRepository) {

    private fun WebpageVisit.isInvalid() = !url.isValidUrl()

    suspend fun deleteWebpageVisit(webpageVisit: WebpageVisit) {
        withContext(Dispatchers.IO) {
            Timber.i("Deleting ${webpageVisit.url}")
            repository.deleteWebpageVisit(webpageVisit)
        }
    }
}