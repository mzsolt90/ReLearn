package com.azyoot.relearn.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "relearn_event", foreignKeys = [ForeignKey(
    entity = WebpageVisit::class,
    parentColumns = ["id"],
    childColumns = ["webpage_visit_id"]
), ForeignKey(
    entity = TranslationEvent::class,
    parentColumns = ["id"],
    childColumns = ["translation_event_id"]
)])
data class RelearnEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "webpage_visit_id") val webpageVisitId: Int?,
    @ColumnInfo(name = "translation_event_id") val translationEventId: Int?,
    @ColumnInfo(name = "deleted") val isDeleted: Boolean = false
)