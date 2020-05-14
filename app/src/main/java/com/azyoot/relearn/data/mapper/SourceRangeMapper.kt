package com.azyoot.relearn.data.mapper

import javax.inject.Inject
import com.azyoot.relearn.data.entity.SourceRange as DataEntity
import com.azyoot.relearn.domain.entity.SourceRange as DomainEntity

class SourceRangeMapper @Inject constructor() :
    EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        minTimestamp = domainEntity.minTime,
        maxTimestamp = domainEntity.maxTime,
        minOrderingNumber = domainEntity.minOrderingNumber,
        maxOrderingNumber = domainEntity.maxOrderingNumber,
        count = domainEntity.count
    )

    override fun toDomainEntity(dataEntity: DataEntity) =
        DomainEntity(
            minTime = dataEntity.minTimestamp,
            maxTime = dataEntity.maxTimestamp,
            minOrderingNumber = dataEntity.minOrderingNumber,
            maxOrderingNumber = dataEntity.maxOrderingNumber,
            count = dataEntity.count
        )
}