package com.azyoot.relearn.domain.usecase.monitoring

import android.os.Bundle
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.analytics.EVENT_WEBPAGE_VISIT_LOGGED
import com.azyoot.relearn.domain.analytics.PROPERTY_URL
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.util.UrlProcessing
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class LogWebpageVisitBufferUseCase @Inject constructor(
    private val repository: WebpageVisitRepository,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val urlProcessing: UrlProcessing
) {

    private fun shouldLog(
        latestForSameUrl: WebpageVisit?
    ) = latestForSameUrl?.time
        ?.isBefore(LocalDateTime.now().minusDays(1)) ?: true

    suspend fun logWebpageVisit(webpageVisit: WebpageVisit) {
        val latestForSameUrl = repository.getLastWebpageVisitForUrl(webpageVisit.url) ?:
        //for backwards compatibility
        repository.getLastWebpageVisitForUrl(urlProcessing.removeScheme(webpageVisit.url))

        if (!shouldLog(latestForSameUrl)) return

        firebaseAnalytics.logEvent(EVENT_WEBPAGE_VISIT_LOGGED, Bundle().apply {
            putString(
                PROPERTY_URL, webpageVisit.url
            )
        })

        withContext(Dispatchers.IO) {
            if (latestForSameUrl != null) {
                Timber.d("Updating webpage visit for url ${latestForSameUrl.url}")
                repository.updateWebpageVisitTime(latestForSameUrl, LocalDateTime.now())
            } else {
                Timber.d("New webpage visit for url ${webpageVisit.url}")
                repository.saveWebpageVisit(webpageVisit)
            }
        }
    }
}