package com.azyoot.relearn.data.mapper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import com.azyoot.relearn.data.entity.WebpageVisit as DataEntity
import com.azyoot.relearn.domain.entity.WebpageVisit as DomainEntity

class WebpageVisitMapper @Inject constructor() : EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        id = domainEntity.databaseId,
        url = domainEntity.url,
        appPackageName = domainEntity.appPackageName,
        timestamp = domainEntity.time.toInstant(ZoneOffset.UTC).toEpochMilli(),
        lastParseVersion = domainEntity.lastParseVersion
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        databaseId = dataEntity.id,
        url = dataEntity.url,
        appPackageName = dataEntity.appPackageName,
        time = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                dataEntity.timestamp
            ), ZoneOffset.UTC
        ),
        lastParseVersion = dataEntity.lastParseVersion
    )

}