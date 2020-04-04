package com.azyoot.relearn.data.mapper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.azyoot.relearn.data.entity.TranslationEvent as DataEntity
import com.azyoot.relearn.domain.entity.TranslationEvent as DomainEntity

class TranslationEventMapper : EntityMapper<DomainEntity, DataEntity> {

    override fun toDataEntity(domainEntity: DomainEntity): DataEntity = DataEntity(
        id = domainEntity.databaseId,
        fromText = domainEntity.fromText,
        toText = domainEntity.toText,
        timestamp = domainEntity.timestamp.toInstant(
            ZoneOffset.UTC
        ).toEpochMilli()
    )

    override fun toDomainEntity(dataEntity: DataEntity): DomainEntity = DomainEntity(
        databaseId = dataEntity.id,
        fromText = dataEntity.fromText,
        toText = dataEntity.toText,
        timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                dataEntity.timestamp
            ), ZoneOffset.UTC
        )
    )

}