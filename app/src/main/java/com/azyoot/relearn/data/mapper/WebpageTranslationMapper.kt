package com.azyoot.relearn.data.mapper

import javax.inject.Inject
import com.azyoot.relearn.data.entity.WebpageTranslation as DataEntity
import com.azyoot.relearn.domain.entity.WebpageTranslation as DomainEntity

class WebpageTranslationMapper @Inject constructor() : EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        id = domainEntity.databaseId,
        toText = domainEntity.toText,
        fromText = domainEntity.fromText,
        webpageVisitId = domainEntity.webpageVisit.databaseId,
        parseVersion = domainEntity.parseVersion
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        fromText = dataEntity.fromText,
        toText = dataEntity.toText,
        parseVersion = dataEntity.parseVersion,
        databaseId = dataEntity.id,
        webpageVisit = UNSET_WEBPAGE_VISIT
    )
}