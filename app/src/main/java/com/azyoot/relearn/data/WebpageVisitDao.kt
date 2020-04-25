package com.azyoot.relearn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.azyoot.relearn.data.entity.PARSE_VERSION
import com.azyoot.relearn.data.entity.WebpageVisit

@Dao
interface WebpageVisitDao {

    @Insert
    suspend fun addWebpageVisit(visit: WebpageVisit)

    @Delete
    suspend fun deleteWebpageVisit(visit: WebpageVisit)

    @Query("SELECT * FROM webpage_visit ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllWebpagesByDate(limit: Int): List<WebpageVisit>

    @Query("SELECT * FROM webpage_visit WHERE last_parse_version < :parseVersion LIMIT :limit")
    suspend fun getUnparsedWebpageVisits(parseVersion: Int = PARSE_VERSION, limit: Int = -1): List<WebpageVisit>

    @Query("SELECT COUNT(*) FROM webpage_visit WHERE last_parse_version < :parseVersion")
    suspend fun getUnparsedWebpageVisitCount(parseVersion: Int = PARSE_VERSION): Int

    @Query("SELECT * FROM webpage_visit WHERE url LIKE :url ORDER BY timestamp DESC")
    suspend fun getLastWebpageVisitByUrl(url: String): WebpageVisit?
}