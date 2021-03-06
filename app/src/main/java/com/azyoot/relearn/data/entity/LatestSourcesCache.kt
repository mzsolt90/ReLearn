package com.azyoot.relearn.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "latest_sources_cache")
data class LatestSourcesCache(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "source_text") val sourceText: String,
    @ColumnInfo(name = "latest_source_timestamp") val latestSourceTimestamp: Long,
    @ColumnInfo(name = "latest_source_id") val latestSourceId: Long,
    @ColumnInfo(name = "latest_relearn_timestamp") val latestReLearnTimestamp: Long?,
    @ColumnInfo(name = "latest_relearn_status") val latestReLearnStatus: Int?,
    @ColumnInfo(name = "latest_timestamp") val latestTimestamp: Long,
    @ColumnInfo(name = "source_type") val sourceType: Int
)