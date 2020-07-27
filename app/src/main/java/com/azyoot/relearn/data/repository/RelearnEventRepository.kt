package com.azyoot.relearn.data.repository

import com.azyoot.relearn.data.dao.RelearnEventDataHandler
import com.azyoot.relearn.data.mapper.ReLearnSourceMapper
import com.azyoot.relearn.data.mapper.SourceRangeMapper
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.util.DateTimeMapper
import java.time.LocalDateTime
import javax.inject.Inject

class RelearnEventRepository @Inject constructor(
    private val relearnEventDataHandler: RelearnEventDataHandler,
    private val sourceRangeMapper: SourceRangeMapper,
    private val reLearnSourceMapper: ReLearnSourceMapper,
    private val dateTimeMapper: DateTimeMapper
) {
    private val suppressedThreshold = dateTimeMapper.mapToTimestamp(
        LocalDateTime.now().minusDays(SUPPRESSED_DAYS.toLong())
    )

    private val acceptedThreshold = dateTimeMapper.mapToTimestamp(
        LocalDateTime.now().minusHours(ACCEPT_DELAY_HOURS.toLong())
    )

    suspend fun getSourceRange() =
        relearnEventDataHandler.getLatestValidSourceRange(
            suppressedThreshold,
            RelearnEventStatus.SUPPRESSED.value,
            acceptedThreshold,
            RelearnEventStatus.ACCEPTED.value
        )
            ?.let { sourceRangeMapper.toDomainEntity(it) }


    suspend fun getNearestSource(orderingNumber: Int) =
        relearnEventDataHandler.getNearestValidSourceForOrderingNumber(
            orderingNumber,
            suppressedThreshold,
            RelearnEventStatus.SUPPRESSED.value,
            acceptedThreshold,
            RelearnEventStatus.ACCEPTED.value
        )
            ?.let { reLearnSourceMapper.toDomainEntity(it) }

    suspend fun getSourceFromId(sourceId: Long, sourceType: SourceType) =
        relearnEventDataHandler.getSourceFromId(sourceId, sourceType.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }

    suspend fun getShowingReLearnEventSource() =
        relearnEventDataHandler
            .getOldestSourceWithState(RelearnEventStatus.SHOWING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }


    suspend fun getOldestPendingReLearnEventSource() = relearnEventDataHandler
        .getOldestSourceWithState(RelearnEventStatus.PENDING.value)
        ?.let { reLearnSourceMapper.toDomainEntity(it) }


    suspend fun setLatestReLearnEventForSource(source: ReLearnSource, status: RelearnEventStatus) =
        relearnEventDataHandler
            .setLatestStatusForSourceAndUpdateCache(
                reLearnSourceMapper.toDataEntity(source),
                status.value
            )
            .let { reLearnSourceMapper.toDomainEntity(it) }


    suspend fun getNthLatestNotShowingSource(n: Int) =
        relearnEventDataHandler.getNthLatestNotShowingSource(n, RelearnEventStatus.SHOWING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }

    suspend fun reloadSourcesCache() = relearnEventDataHandler.reloadSourcesCache()

    suspend fun setReLearnDeleted(source: ReLearnSource, isDeleted: Boolean) {
        relearnEventDataHandler.setReLearnDeleted(
            source.latestSourceId,
            source.sourceType.value,
            isDeleted
        )
    }

    companion object {
        const val SUPPRESSED_DAYS = 30
        const val ACCEPT_DELAY_HOURS = 48
    }
}