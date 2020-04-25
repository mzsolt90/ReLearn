package com.azyoot.relearn.data.repository

import com.azyoot.relearn.data.AppDatabase
import com.azyoot.relearn.data.mapper.ReLearnSourceMapper
import com.azyoot.relearn.data.mapper.SourceRangeMapper
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import com.azyoot.relearn.domain.entity.SourceRange
import com.azyoot.relearn.util.DateTimeMapper
import java.time.LocalDateTime
import javax.inject.Inject

class RelearnEventRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val sourceRangeMapper: SourceRangeMapper,
    private val reLearnSourceMapper: ReLearnSourceMapper,
    private val dateTimeMapper: DateTimeMapper
) {
    private val suppressedThreshold = dateTimeMapper.mapToTimestamp(
        LocalDateTime.now().minusDays(SUPPRESSED_DAYS.toLong())
    )

    private suspend fun needsCacheReload() =
        appDatabase.relearnEventDao().getCacheSize() < MIN_CACHE_SIZE

    private suspend fun reloadCacheIfNeeded() {
        if (needsCacheReload()) appDatabase.relearnEventDao()
            .reloadSourcesCache(suppressedThreshold)
    }

    suspend fun getSourceRange(): SourceRange? {
        reloadCacheIfNeeded()

        return appDatabase.relearnEventDao().getLatestSourceRange()
            ?.let { sourceRangeMapper.toDomainEntity(it) }
    }

    suspend fun getNearestSource(id: Int): ReLearnSource? {
        reloadCacheIfNeeded()

        return appDatabase.relearnEventDao().getNearestSourceForId(id)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun getShowingReLearnEventSource(): ReLearnSource? {
        reloadCacheIfNeeded()

        return appDatabase.relearnEventDao()
            .getOldestReLearnSourceWithState(RelearnEventStatus.SHOWING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun getOldestPendingReLearnEventSource(): ReLearnSource? {
        reloadCacheIfNeeded()

        return appDatabase.relearnEventDao()
            .getOldestReLearnSourceWithState(RelearnEventStatus.PENDING.value)
            ?.let { reLearnSourceMapper.toDomainEntity(it) }
    }

    suspend fun setLatestReLearnEventForSource(source: ReLearnSource, status: RelearnEventStatus) {
        appDatabase.relearnEventDao()
            .setLatestReLearnStatusForSource(reLearnSourceMapper.toDataEntity(source), status.value)
    }

    companion object {
        const val SUPPRESSED_DAYS = 30
        const val MIN_CACHE_SIZE = 30
    }
}