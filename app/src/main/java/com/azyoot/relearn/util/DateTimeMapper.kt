package com.azyoot.relearn.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

class DateTimeMapper @Inject constructor() {
    fun mapToTimestamp(localDateTime: LocalDateTime) = localDateTime.toInstant(
        ZoneOffset.UTC
    ).toEpochMilli()

    fun mapToTimestamp(localDateTime: ZonedDateTime) = localDateTime.toInstant().toEpochMilli()

    fun mapToLocalDateTime(timestamp: Long) = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(
            timestamp
        ), ZoneOffset.UTC
    )
}