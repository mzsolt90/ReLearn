package com.azyoot.relearn.data.entity

import androidx.room.ColumnInfo

data class SourceRange(
    @ColumnInfo(name = "min_timestamp") val minTimestamp: Long,
    @ColumnInfo(name = "max_timestamp") val maxTimestamp: Long,
    @ColumnInfo(name = "min_id") val minId: Int,
    @ColumnInfo(name = "max_id") val maxId: Int,
    @ColumnInfo(name = "count") val count: Int
)