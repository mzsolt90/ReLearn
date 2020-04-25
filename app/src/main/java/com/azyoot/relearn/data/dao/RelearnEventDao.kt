package com.azyoot.relearn.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.azyoot.relearn.data.entity.LatestSourcesView
import com.azyoot.relearn.data.entity.SourceRange
import com.azyoot.relearn.domain.entity.RelearnEventStatus

@Dao
interface RelearnEventDao {

    @Query("""SELECT COUNT(*) FROM latest_sources_cache""")
    suspend fun getCacheSize(): Int

    @Query("""DELETE FROM latest_sources_cache""")
    suspend fun clearSourcesCache()

    @Query(
        """INSERT INTO latest_sources_cache (source_text, latest_timestamp, latest_source_id, if_relearn_status, source_type)
        SELECT source_text, latest_timestamp, latest_source_id, if_relearn_status, source_type
        FROM LatestSourcesView
        WHERE (if_relearn_status != :suppressedCode OR if_relearn_status IS NULL OR latest_timestamp < :suppressedThreshold)
        ORDER BY latest_timestamp DESC"""
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

    @Query(
        """SELECT MIN(latest_timestamp) AS min_timestamp, 
                MAX(latest_timestamp) AS max_timestamp,
                MIN(id) AS min_id, 
                MAX(id) AS max_id
             FROM latest_sources_cache"""
    )
    suspend fun getLatestNotSuppressedSourceRange(): SourceRange?

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
}