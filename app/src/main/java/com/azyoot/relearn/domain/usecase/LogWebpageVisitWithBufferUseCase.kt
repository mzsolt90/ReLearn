package com.azyoot.relearn.domain.usecase

import android.os.Bundle
import com.azyoot.relearn.data.WebpageVisitRepository
import com.azyoot.relearn.domain.analytics.EVENT_WEBPAGE_VISIT_LOGGED
import com.azyoot.relearn.domain.analytics.PROPERTY_URL
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.google.firebase.analytics.FirebaseAnalytics
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class LogWebpageVisitBufferUseCase @Inject constructor(
    private val repository: WebpageVisitRepository,
    private val firebaseAnalytics: FirebaseAnalytics
) {

    private suspend fun shouldLog(webpageVisit: WebpageVisit): Boolean {
        val latestForSameUrl = repository.getLastWebpageVisitForUrl(webpageVisit.url)

        return LocalDateTime.now().minusDays(1).isAfter(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(
                    latestForSameUrl?.timestamp ?: 0
                ), ZoneOffset.UTC
            )
        )

    }

    suspend fun logWebpageVisit(webpageVisit: WebpageVisit) {
        if(!shouldLog(webpageVisit)) return

        firebaseAnalytics.logEvent(EVENT_WEBPAGE_VISIT_LOGGED, Bundle().apply {
            putString(
                PROPERTY_URL, webpageVisit.url
            )
        })

        repository.saveWebpageVisit(webpageVisit)
    }
}