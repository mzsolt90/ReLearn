package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.util.DateTimeMapper
import javax.inject.Inject
import com.azyoot.relearn.data.entity.RelearnEvent as DataEntity
import com.azyoot.relearn.domain.entity.RelearnEvent as DomainEntity

class RelearnEventMapper @Inject constructor(private val dateTimeMapper: DateTimeMapper) : EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        id = domainEntity.databaseId,
        timestamp = dateTimeMapper.mapToTimestamp(domainEntity.time),
        status = domainEntity.status.value,
        webpageVisitId = domainEntity.webpageVisit?.databaseId,
        translationEventId = domainEntity.translationEvent?.databaseId
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        databaseId = dataEntity.id,
        time = dateTimeMapper.mapToLocalDateTime(dataEntity.timestamp),
        status = RelearnEventStatus.values().find { it.value == dataEntity.status }!!,
        webpageVisit = null,
        translationEvent = null
    )
}