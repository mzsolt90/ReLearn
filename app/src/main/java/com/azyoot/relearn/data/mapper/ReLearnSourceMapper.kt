package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.util.DateTimeMapper
import javax.inject.Inject
import com.azyoot.relearn.data.entity.LatestSourcesView as DataEntity
import com.azyoot.relearn.domain.entity.ReLearnSource as DomainEntity

class ReLearnSourceMapper @Inject constructor(private val dateTimeMapper: DateTimeMapper,
                                              private val translationEventMapper: TranslationEventMapper,
                                              private val webpageVisitMapper: WebpageVisitMapper) :
    EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        domainEntity.sourceText,
        dateTimeMapper.mapToTimestamp(domainEntity.latestTime),
        domainEntity.latestSourceId,
        domainEntity.statusIfRelearnEvent?.value,
        domainEntity.sourceType.value,
        domainEntity.webpageVisit?.let { webpageVisitMapper.toDataEntity(it) },
        domainEntity.translationEvent?.let { translationEventMapper.toDataEntity(it) }
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        dataEntity.sourceText,
        dateTimeMapper.mapToLocalDateTime(dataEntity.latestTimestamp),
        dataEntity.latestSourceId,
        dataEntity.statusIfRelearnEvent?.let { status ->
            RelearnEventStatus.values().firstOrNull { it.value == status }
        },
        SourceType.values().first { it.value == dataEntity.sourceType },
        dataEntity.webpageVisit?.let { webpageVisitMapper.toDomainEntity(it) },
        dataEntity.translationEvent?.let { translationEventMapper.toDomainEntity(it) }
    )

}