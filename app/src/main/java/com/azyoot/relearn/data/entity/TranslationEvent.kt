package com.azyoot.relearn.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_event")
data class TranslationEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "from_text") val fromText: String,
    @ColumnInfo(name = "to_text") val toText: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "deleted") val deleted: Boolean = false
)