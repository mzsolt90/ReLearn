package com.azyoot.relearn.domain.entity

import java.time.LocalDateTime

enum class RelearnEventStatus(val value: Int) {
    SHOWING(1),
    APPROVED(2),
    PENDING(3),
    SUPPRESSED(4)
}

data class RelearnEvent(
    val databaseId: Int = 0,
    val time: LocalDateTime,
    val status: RelearnEventStatus,
    val webpageVisit: WebpageVisit?,
    val translationEvent: TranslationEvent?
)