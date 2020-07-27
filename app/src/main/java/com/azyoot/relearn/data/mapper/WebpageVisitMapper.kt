package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.util.DateTimeMapper
import javax.inject.Inject
import com.azyoot.relearn.data.entity.WebpageVisit as DataEntity
import com.azyoot.relearn.domain.entity.WebpageVisit as DomainEntity

class WebpageVisitMapper @Inject constructor(private val dateTimeMapper: DateTimeMapper) :
    EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        id = domainEntity.databaseId,
        url = domainEntity.url,
        appPackageName = domainEntity.appPackageName,
        timestamp = dateTimeMapper.mapToTimestamp(domainEntity.time),
        lastParseVersion = domainEntity.lastParseVersion
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        databaseId = dataEntity.id,
        url = dataEntity.url,
        appPackageName = dataEntity.appPackageName,
        time = dateTimeMapper.mapToLocalDateTime(dataEntity.timestamp),
        lastParseVersion = dataEntity.lastParseVersion
    )

}