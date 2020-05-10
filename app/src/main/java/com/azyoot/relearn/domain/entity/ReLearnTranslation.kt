package com.azyoot.relearn.domain.entity

data class ReLearnTranslation(
    val source: ReLearnSource,
    val sourceText: String,
    val translations: List<String>
)