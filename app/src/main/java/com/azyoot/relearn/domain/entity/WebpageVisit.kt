package com.azyoot.relearn.domain.entity

import java.time.LocalDateTime

data class WebpageVisit(
    val url: String,
    val appPackageName: String,
    val time: LocalDateTime = LocalDateTime.now(),
    val databaseId: Int = 0
)