package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.util.DateTimeMapper
import javax.inject.Inject
import com.azyoot.relearn.data.entity.TranslationEvent as DataEntity
import com.azyoot.relearn.domain.entity.TranslationEvent as DomainEntity

class TranslationEventMapper @Inject constructor(private val dateTimeMapper: DateTimeMapper) :
    EntityMapper<DomainEntity, DataEntity> {

    override fun toDataEntity(domainEntity: DomainEntity): DataEntity = DataEntity(
        id = domainEntity.databaseId,
        fromText = domainEntity.fromText,
        toText = domainEntity.toText,
        timestamp = dateTimeMapper.mapToTimestamp(domainEntity.timestamp)
    )

    override fun toDomainEntity(dataEntity: DataEntity): DomainEntity = DomainEntity(
        databaseId = dataEntity.id,
        fromText = dataEntity.fromText,
        toText = dataEntity.toText,
        timestamp = dateTimeMapper.mapToLocalDateTime(dataEntity.timestamp)
    )

}