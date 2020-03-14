package com.azyoot.relearn.domain.entity

import java.time.LocalDateTime

data class TranslationEvent(
    val id: Int = 0,
    val fromText: String,
    val toText: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)