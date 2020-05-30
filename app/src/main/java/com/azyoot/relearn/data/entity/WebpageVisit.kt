package com.azyoot.relearn.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webpage_visit")
data class WebpageVisit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val appPackageName: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "last_parse_version") val lastParseVersion: Int,
    @ColumnInfo(name = "deleted") val isDeleted: Boolean = false
)