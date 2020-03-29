package com.azyoot.relearn.domain.usecase.monitoring

import android.os.Bundle
import com.azyoot.relearn.data.WebpageVisitRepository
import com.azyoot.relearn.domain.analytics.EVENT_WEBPAGE_VISIT_LOGGED
import com.azyoot.relearn.domain.analytics.PROPERTY_URL
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class LogWebpageVisitBufferUseCase @Inject constructor(
    private val repository: WebpageVisitRepository,
    private val firebaseAnalytics: FirebaseAnalytics
) {

    private suspend fun shouldLog(webpageVisit: WebpageVisit): Boolean {
        val latestForSameUrl = repository.getLastWebpageVisitForUrl(webpageVisit.url)

        return latestForSameUrl?.time
            ?.isBefore(LocalDateTime.now().minusDays(1)) ?: true
    }

    suspend fun logWebpageVisit(webpageVisit: WebpageVisit) {
        if (!shouldLog(webpageVisit)) return

        firebaseAnalytics.logEvent(EVENT_WEBPAGE_VISIT_LOGGED, Bundle().apply {
            putString(
                PROPERTY_URL, webpageVisit.url
            )
        })

        withContext(Dispatchers.IO) {
            repository.saveWebpageVisit(webpageVisit)
        }
    }
}