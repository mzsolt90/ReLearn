package com.azyoot.relearn.domain.entity

data class SourceRange(
    val minTime: Long,
    val maxTime: Long,
    val minOrderingNumber: Int,
    val maxOrderingNumber: Int,
    val count: Int
)