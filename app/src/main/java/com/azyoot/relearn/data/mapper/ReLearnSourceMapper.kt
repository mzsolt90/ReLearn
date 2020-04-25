package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.util.DateTimeMapper
import java.lang.Long.max
import javax.inject.Inject
import com.azyoot.relearn.data.entity.LatestSourcesView as DataEntity
import com.azyoot.relearn.domain.entity.ReLearnSource as DomainEntity

class ReLearnSourceMapper @Inject constructor(
    private val dateTimeMapper: DateTimeMapper,
    private val translationEventMapper: TranslationEventMapper,
    private val webpageVisitMapper: WebpageVisitMapper
) :
    EntityMapper<DomainEntity, DataEntity> {
    override fun toDataEntity(domainEntity: DomainEntity) = DataEntity(
        sourceText = domainEntity.sourceText,
        latestSourceTimestamp = dateTimeMapper.mapToTimestamp(domainEntity.latestSourceTime),
        latestSourceId = domainEntity.latestSourceId,
        latestReLearnTimestamp = domainEntity.latestReLearnTime?.let {
            dateTimeMapper.mapToTimestamp(
                it
            )
        },
        latestRelearnStatus = domainEntity.latestRelearnStatus?.value,
        sourceType = domainEntity.sourceType.value,
        latestTimestamp = max(domainEntity.latestReLearnTime?.let { dateTimeMapper.mapToTimestamp(it) }
            ?: 0, dateTimeMapper.mapToTimestamp(domainEntity.latestSourceTime)),
        webpageVisit = domainEntity.webpageVisit?.let { webpageVisitMapper.toDataEntity(it) },
        translationEvent = domainEntity.translationEvent?.let {
            translationEventMapper.toDataEntity(
                it
            )
        }
    )

    override fun toDomainEntity(dataEntity: DataEntity) = DomainEntity(
        sourceText = dataEntity.sourceText,
        latestSourceTime = dateTimeMapper.mapToLocalDateTime(dataEntity.latestSourceTimestamp),
        latestSourceId = dataEntity.latestSourceId,
        latestReLearnTime = dataEntity.latestReLearnTimestamp?.let {
            dateTimeMapper.mapToLocalDateTime(
                it
            )
        },
        latestRelearnStatus = dataEntity.latestRelearnStatus?.let { status ->
            RelearnEventStatus.values().firstOrNull { it.value == status }
        },
        sourceType = SourceType.values().first { it.value == dataEntity.sourceType },
        webpageVisit = dataEntity.webpageVisit?.let { webpageVisitMapper.toDomainEntity(it) },
        translationEvent = dataEntity.translationEvent?.let {
            translationEventMapper.toDomainEntity(
                it
            )
        }
    )

}