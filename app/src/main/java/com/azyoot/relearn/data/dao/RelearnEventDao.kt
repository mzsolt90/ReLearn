package com.azyoot.relearn.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.azyoot.relearn.data.entity.*

interface RelearnEventDataHandler {
    suspend fun getLatestValidSourceRange(
        suppressedThreshold: Long,
        suppressedCode: Int,
        acceptedThreshold: Long,
        acceptedCode: Int
    ): SourceRange?

    suspend fun getNearestValidSourceForId(
        id: Int,
        suppressedThreshold: Long,
        suppressedCode: Int,
        acceptedThreshold: Long,
        acceptedCode: Int
    ): LatestSourcesView?

    suspend fun getOldestReLearnSourceWithState(status: Int): LatestSourcesView?

    suspend fun setLatestReLearnStatusForSourceAndUpdateCache(
        source: LatestSourcesView,
        status: Int
    )
}

@Dao
interface RelearnEventDaoInternal : RelearnEventDataHandler {

    @Query("""SELECT COUNT(*) FROM latest_sources_cache""")
    suspend fun getCacheSize(): Int

    @Query("""DELETE FROM latest_sources_cache""")
    suspend fun clearSourcesCache()

    @Query(
        """INSERT INTO latest_sources_cache (source_text, latest_source_timestamp, latest_source_id, latest_relearn_timestamp, latest_relearn_status, latest_timestamp, source_type)
        SELECT source_text, latest_source_timestamp, latest_source_id, latest_relearn_timestamp, latest_relearn_status, latest_timestamp, source_type
        FROM LatestSourcesView
        ORDER BY LatestSourcesView.latest_timestamp ASC"""
    )
    suspend fun populateSourcesCache()

    @Transaction
    suspend fun reloadSourcesCacheIfNeeded() {
        if(getCacheSize() >= MIN_CACHE_SIZE) return
        clearSourcesCache()
        populateSourcesCache()
    }

    @Query(
        """DELETE FROM latest_sources_cache
         WHERE latest_source_id = :sourceId 
            AND source_type = :sourceType
            AND latest_timestamp < :timestamp"""
    )
    suspend fun deleteOldCacheEntry(sourceId: Long, sourceType: Int, timestamp: Long)

    @Query(
        """INSERT INTO latest_sources_cache (source_text, latest_source_timestamp, latest_source_id, latest_relearn_timestamp, latest_relearn_status, latest_timestamp, source_type)
        SELECT source_text, latest_source_timestamp, latest_source_id, :newTimestamp, :newStatus, :newTimestamp, source_type
         FROM latest_sources_cache
         WHERE latest_source_id = :sourceId 
            AND source_type = :sourceType"""
    )
    suspend fun appendCacheEntryWithNewReLearnStatus(
        sourceId: Long,
        sourceType: Int,
        newStatus: Int,
        newTimestamp: Long
    )

    @Query(
        """SELECT MIN(latest_timestamp) AS min_timestamp, 
                MAX(latest_timestamp) AS max_timestamp,
                MIN(id) AS min_id, 
                MAX(id) AS max_id
             FROM latest_sources_cache
             WHERE (latest_relearn_status != :suppressedCode OR latest_relearn_status IS NULL OR latest_relearn_timestamp < :suppressedThreshold)
             AND (latest_relearn_status != :acceptedCode OR latest_relearn_status IS NULL OR latest_relearn_timestamp < :acceptedThreshold)"""
    )
    suspend fun getLatestValidSourceRangeInternal(
        suppressedThreshold: Long,
        suppressedCode: Int,
        acceptedThreshold: Long,
        acceptedCode: Int
    ): SourceRange?

    @Transaction
    override suspend fun getLatestValidSourceRange(
        suppressedThreshold: Long,
        suppressedCode: Int,
        acceptedThreshold: Long,
        acceptedCode: Int
    ): SourceRange? {
        reloadSourcesCacheIfNeeded()
        return getLatestValidSourceRangeInternal(suppressedThreshold, suppressedCode, acceptedThreshold, acceptedCode)
    }

    @Query(
        """SELECT LatestSourcesView.* 
        FROM latest_sources_cache 
        JOIN LatestSourcesView ON LatestSourcesView.latest_source_id = latest_sources_cache.latest_source_id 
            AND LatestSourcesView.source_type = latest_sources_cache.source_type
        WHERE ABS(latest_sources_cache.id - :id) = (
            SELECT MIN(ABS(latest_sources_cache.id - :id)) 
            FROM latest_sources_cache
            WHERE (latest_relearn_status != :suppressedCode OR latest_relearn_status IS NULL OR latest_relearn_timestamp < :suppressedThreshold) 
            AND (latest_relearn_status != :acceptedCode OR latest_relearn_status IS NULL OR latest_relearn_timestamp < :acceptedThreshold))"""
    )
    override suspend fun getNearestValidSourceForId(
        id: Int,
        suppressedThreshold: Long,
        suppressedCode: Int,
        acceptedThreshold: Long,
        acceptedCode: Int
    ): LatestSourcesView?

    @Query(
        """SELECT LatestSourcesView.*  
        FROM latest_sources_cache
        JOIN LatestSourcesView ON LatestSourcesView.latest_source_id = latest_sources_cache.latest_source_id 
            AND LatestSourcesView.source_type = latest_sources_cache.source_type
        WHERE latest_sources_cache.latest_relearn_status = :status
        ORDER BY latest_sources_cache.id ASC"""
    )
    suspend fun getOldestReLearnSourceWithStateInternal(status: Int): LatestSourcesView?

    @Transaction
    override suspend fun getOldestReLearnSourceWithState(status: Int): LatestSourcesView? {
        reloadSourcesCacheIfNeeded()

        return getOldestReLearnSourceWithStateInternal(status)
    }

    @Query(
        """SELECT *  
        FROM relearn_event
        WHERE relearn_event.webpage_visit_id = :id
        ORDER BY relearn_event.id DESC"""
    )
    suspend fun getMostRecentReLearnEventForWebpageVisit(id: Long): RelearnEvent?

    @Query(
        """SELECT *  
        FROM relearn_event
        WHERE relearn_event.translation_event_id = :id
        ORDER BY relearn_event.id DESC"""
    )
    suspend fun getMostRecentReLearnEventForTranslation(id: Long): RelearnEvent?

    @Insert
    suspend fun addReLearnEvent(relearnEvent: RelearnEvent)

    suspend fun getLatestReLearnEventForSource(source: LatestSourcesView) =
        when (source.sourceType) {
            ENTITY_TYPE_WEBPAGE -> getMostRecentReLearnEventForWebpageVisit(source.latestSourceId)
            ENTITY_TYPE_TRANSLATION -> getMostRecentReLearnEventForTranslation(source.latestSourceId)
            else -> throw IllegalArgumentException("Invalid type: ${source.sourceType}")
        }

    @Transaction
    override suspend fun setLatestReLearnStatusForSourceAndUpdateCache(
        source: LatestSourcesView,
        status: Int
    ) {
        val latestEvent = getLatestReLearnEventForSource(source)
        if (latestEvent?.status == status) return

        val newEvent = RelearnEvent(
            timestamp = System.currentTimeMillis(),
            status = status,
            webpageVisitId = source.webpageVisit?.id,
            translationEventId = source.translationEvent?.id
        )
        addReLearnEvent(newEvent)

        appendCacheEntryWithNewReLearnStatus(
            source.latestSourceId,
            source.sourceType,
            status,
            newEvent.timestamp
        )
        deleteOldCacheEntry(source.latestSourceId, source.sourceType, newEvent.timestamp)
    }

    companion object {
        const val MIN_CACHE_SIZE = 30
    }
}