package com.azyoot.relearn.data.repository

import com.azyoot.relearn.data.dao.RelearnEventDataHandler
import com.azyoot.relearn.data.mapper.ReLearnSourceMapper
import com.azyoot.relearn.data.mapper.SourceRangeMapper
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.domain.entity.SourceRange
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

    private suspend fun needsCacheReload() =
        relearnEventDataHandler.getCacheSize() < MIN_CACHE_SIZE

    private suspend fun reloadCacheIfNeeded() {
        if (needsCacheReload()) relearnEventDataHandler
            .reloadSourcesCache()
    }

    suspend fun getSourceRange(): SourceRange? {
        reloadCacheIfNeeded()

        return relearnEventDataHandler.getLatestValidSourceRange(
            suppressedThreshold,
            RelearnEventStatus.SUPPRESSED.value
        )
            ?.let { sourceRangeMapper.toDomainEntity(it) }
    }

    suspend fun getNearestSource(id: Int): ReLearnSource? {
        reloadCacheIfNeeded()

        return relearnEventDataHandler.getNearestValidSourceForId(
            id,
            suppressedThreshold,
            RelearnEventStatus.SUPPRESSED.value
        )
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun getShowingReLearnEventSource(): ReLearnSource? {
        reloadCacheIfNeeded()

        return relearnEventDataHandler
            .getOldestReLearnSourceWithState(RelearnEventStatus.SHOWING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun getOldestPendingReLearnEventSource(): ReLearnSource? {
        reloadCacheIfNeeded()

        return relearnEventDataHandler
            .getOldestReLearnSourceWithState(RelearnEventStatus.PENDING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun setLatestReLearnEventForSource(source: ReLearnSource, status: RelearnEventStatus) {
        relearnEventDataHandler
            .setLatestReLearnStatusForSourceAndUpdateCache(
                reLearnSourceMapper.toDataEntity(source),
                status.value
            )
    }

    companion object {
        const val SUPPRESSED_DAYS = 30
        const val MIN_CACHE_SIZE = 30
    }
}