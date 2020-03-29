package com.azyoot.relearn.domain.usecase.monitoring

import android.os.Bundle
import com.azyoot.relearn.data.TranslationEventRepository
import com.azyoot.relearn.domain.analytics.EVENT_TRANSLATION_LOGGED
import com.azyoot.relearn.domain.analytics.PROPERTY_FROM_TEXT
import com.azyoot.relearn.domain.analytics.PROPERTY_TO_TEXT
import com.azyoot.relearn.domain.entity.TranslationEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class LogTranslationEventWithBufferUseCase @Inject constructor(
    private val repository: TranslationEventRepository,
    private val firebaseAnalytics: FirebaseAnalytics
) {
    private suspend fun shouldLog(translationEvent: TranslationEvent): Boolean {
        val latestForSameText = repository.getLastTranslationEventForResultText(
            translationEvent.toText
        )

        return latestForSameText?.timestamp
            ?.isBefore(LocalDateTime.now().minusDays(1)) ?: true
    }

    suspend fun logTranslationEvent(translationEvent: TranslationEvent) {
        if (!shouldLog(translationEvent)) return

        firebaseAnalytics.logEvent(EVENT_TRANSLATION_LOGGED, Bundle().apply {
            putString(
                PROPERTY_FROM_TEXT, translationEvent.fromText
            )
            putString(
                PROPERTY_TO_TEXT, translationEvent.toText
            )
        })

        withContext(Dispatchers.IO) {
            repository.saveTranslationEvent(translationEvent)
        }
    }
}