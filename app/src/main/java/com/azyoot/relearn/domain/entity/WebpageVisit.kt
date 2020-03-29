package com.azyoot.relearn.domain.entity

import com.azyoot.relearn.data.entity.PARSE_VERSION
import java.time.LocalDateTime

data class WebpageVisit(
    val url: String,
    val appPackageName: String,
    val time: LocalDateTime = LocalDateTime.now(),
    val databaseId: Int = 0,
    val lastParseVersion: Int = PARSE_VERSION
)