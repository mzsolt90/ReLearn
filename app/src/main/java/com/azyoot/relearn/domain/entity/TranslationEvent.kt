package com.azyoot.relearn.domain.entity

import java.time.LocalDateTime

data class TranslationEvent(
    val fromText: String,
    val toText: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val databaseId: Int = 0
)