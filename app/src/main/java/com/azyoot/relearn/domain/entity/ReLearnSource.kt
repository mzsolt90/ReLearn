package com.azyoot.relearn.domain.entity

import com.azyoot.relearn.data.entity.ENTITY_TYPE_TRANSLATION
import com.azyoot.relearn.data.entity.ENTITY_TYPE_WEBPAGE
import java.time.LocalDateTime

enum class SourceType(val value: Int) {
    WEBPAGE_VISIT(ENTITY_TYPE_WEBPAGE),
    TRANSLATION(ENTITY_TYPE_TRANSLATION)
}

data class ReLearnSource(
    val sourceText: String,
    val latestSourceTime: LocalDateTime,
    val latestSourceId: Long,
    val latestReLearnTime: LocalDateTime?,
    val latestRelearnStatus: RelearnEventStatus?,
    val sourceType: SourceType,
    val webpageVisit: WebpageVisit?,
    val translationEvent: TranslationEvent?
)