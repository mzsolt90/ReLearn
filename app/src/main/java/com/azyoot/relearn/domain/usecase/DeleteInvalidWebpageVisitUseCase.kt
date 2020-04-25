package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.data.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.util.isValidUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteInvalidWebpageVisitUseCase @Inject constructor(private val repository: WebpageVisitRepository) {

    private fun WebpageVisit.isInvalid() = !url.isValidUrl()

    suspend fun deleteWebpageVisitIfInvalid(webpageVisit: WebpageVisit) {
        if (webpageVisit.isInvalid()) withContext(Dispatchers.IO) {
            repository.deleteWebpageVisit(webpageVisit)
        }
    }
}