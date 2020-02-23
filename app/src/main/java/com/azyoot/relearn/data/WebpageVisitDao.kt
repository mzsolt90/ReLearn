package com.azyoot.relearn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.azyoot.relearn.data.entity.WebpageVisit

@Dao
interface WebpageVisitDao {

    @Insert
    suspend fun addWebpageVisit(visit: WebpageVisit)

    @Query("SELECT * FROM webpage_visit ORDER BY timestamp DESC")
    suspend fun getAllWebpagesByDate(): List<WebpageVisit>

    @Query("SELECT * FROM webpage_visit WHERE url LIKE :url ORDER BY timestamp DESC")
    suspend fun getLastWebpageVisitByUrl(url: String): WebpageVisit?
}