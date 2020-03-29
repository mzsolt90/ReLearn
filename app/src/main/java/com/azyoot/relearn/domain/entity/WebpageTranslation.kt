package com.azyoot.relearn.domain.entity

import com.azyoot.relearn.data.entity.PARSE_VERSION

data class WebpageTranslation(
    val fromText: String,
    val toText: String,
    val webpageVisit: WebpageVisit,
    val parseVersion: Int = PARSE_VERSION,
    val databaseId: Int = 0
)