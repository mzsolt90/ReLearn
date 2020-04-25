package com.azyoot.relearn.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.azyoot.relearn.data.entity.*
import com.azyoot.relearn.domain.entity.RelearnEventStatus
import java.lang.IllegalArgumentException

@Dao
interface RelearnEventDao {

    @Query("""SELECT COUNT(*) FROM latest_sources_cache""")
    suspend fun getCacheSize(): Int

    @Query("""DELETE FROM latest_sources_cache""")
    suspend fun clearSourcesCache()

    @Query(
        """INSERT INTO latest_sources_cache (source_text, latest_source_timestamp, latest_source_id, latest_relearn_timestamp, latest_relearn_status, latest_timestamp, source_type)
        SELECT source_text, latest_source_timestamp, latest_source_id, latest_relearn_timestamp, latest_relearn_status, latest_timestamp, source_type
        FROM LatestSourcesView
        WHERE (latest_relearn_status != :suppressedCode OR latest_relearn_status IS NULL OR latest_relearn_timestamp < :suppressedThreshold)
        ORDER BY LatestSourcesView.latest_timestamp DESC"""
    )
    suspend fun populateSourcesCache(
        suppressedThreshold: Long,
        suppressedCode: Int = RelearnEventStatus.SUPPRESSED.value
    )

    @Transaction
    suspend fun reloadSourcesCache(
        suppressedThreshold: Long,
        suppressedCode: Int = RelearnEventStatus.SUPPRESSED.value
    ) {
        clearSourcesCache()
        populateSourcesCache(suppressedThreshold)
    }

    @Query("""UPDATE latest_sources_cache
        SET latest_relearn_status = :newStatus
        WHERE latest_source_id = :sourceId 
        AND source_type = :sourceType""")
    suspend fun updateCacheEntryStatus(sourceId: Long, sourceType: Int, newStatus: Int)

    @Query(
        """SELECT MIN(latest_timestamp) AS min_timestamp, 
                MAX(latest_timestamp) AS max_timestamp,
                MIN(id) AS min_id, 
                MAX(id) AS max_id
             FROM latest_sources_cache"""
    )
    suspend fun getLatestSourceRange(): SourceRange?

    @Query(
        """SELECT LatestSourcesView.* 
        FROM latest_sources_cache 
        JOIN LatestSourcesView ON LatestSourcesView.latest_source_id = latest_sources_cache.latest_source_id 
            AND LatestSourcesView.source_type = latest_sources_cache.source_type
        WHERE ABS(latest_sources_cache.id - :id) = (SELECT MIN(ABS(latest_sources_cache.id - :id)) FROM latest_sources_cache)"""
    )
    suspend fun getNearestSourceForId(
        id: Int
    ): LatestSourcesView?

    @Query(
        """SELECT LatestSourcesView.*  
        FROM latest_sources_cache
        JOIN LatestSourcesView ON LatestSourcesView.latest_source_id = latest_sources_cache.latest_source_id 
            AND LatestSourcesView.source_type = latest_sources_cache.source_type
        WHERE latest_sources_cache.latest_relearn_status = :status
        ORDER BY latest_sources_cache.id ASC"""
    )
    suspend fun getOldestReLearnSourceWithState(status: Int): LatestSourcesView?

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
    suspend fun setLatestReLearnStatusForSource(source: LatestSourcesView, status: Int) {
        val latestEvent = getLatestReLearnEventForSource(source)
        if (latestEvent?.status == status) return

        val newEvent = RelearnEvent(
            timestamp = System.currentTimeMillis(),
            status = status,
            webpageVisitId = source.webpageVisit?.id,
            translationEventId = source.translationEvent?.id
        )
        addReLearnEvent(newEvent)

        updateCacheEntryStatus(source.latestSourceId, source.sourceType, status)
    }
}