package com.azyoot.relearn.domain.entity

import java.time.LocalDateTime

enum class SourceType(val value: Int) {
    WEBPAGE_VISIT(1),
    TRANSLATION(2)
}

data class ReLearnSource(val sourceText : String,
                         val latestTime : LocalDateTime,
                         val latestSourceId : Long,
                         val statusIfRelearnEvent : RelearnEventStatus?,
                         val sourceType : SourceType,
                         val webpageVisit: WebpageVisit?,
                         val translationEvent: TranslationEvent?)