package com.azyoot.relearn.data

import com.azyoot.relearn.di.AppScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.azyoot.relearn.domain.entity.TranslationEvent as DomainEntity
import com.azyoot.relearn.data.entity.TranslationEvent as DataEntity
import javax.inject.Inject

@AppScope
class TranslationEventRepository @Inject constructor(private val database: AppDatabase) {

    private fun DomainEntity.toDataEntity() = DataEntity(
        id = id,
        fromText = fromText,
        toText = toText,
        timestamp = timestamp.toInstant(
            ZoneOffset.UTC
        ).toEpochMilli()
    )

    private fun DataEntity.toDomainEntity() = DomainEntity(
        id = id,
        fromText = fromText,
        toText = toText,
        timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                this.timestamp
            ), ZoneOffset.UTC
        )
    )

    suspend fun saveTranslationEvent(translationEvent: DomainEntity) {
        database.translationEventDao().addTranslationEvent(translationEvent.toDataEntity())
    }

    suspend fun getLastTranslationEventForResultText(toText: String) =
        database.translationEventDao().getLastTranslationEventByResultText(toText)?.toDomainEntity()
}