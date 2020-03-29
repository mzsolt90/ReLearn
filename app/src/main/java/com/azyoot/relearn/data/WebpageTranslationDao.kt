package com.azyoot.relearn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.azyoot.relearn.data.entity.PARSE_VERSION
import com.azyoot.relearn.data.entity.WebpageTranslation
import com.azyoot.relearn.data.entity.WebpageVisit
import com.azyoot.relearn.data.entity.WebpageVisitWithLatestTranslationView

@Dao
interface WebpageTranslationDao {

    @Insert
    suspend fun addWebpageTranslation(webpageTranslation: WebpageTranslation)

    @Query("UPDATE webpage_visit SET last_parse_version = :lastParseVersion WHERE id = :id")
    suspend fun updateLastParseVersionOfWebpageVisit(id: Int, lastParseVersion: Int)

    @Transaction
    suspend fun addWebpageTranslationForWebpageVisit(webpageVisit: WebpageVisit, webpageTranslation: List<WebpageTranslation>) {
        webpageTranslation.forEach { addWebpageTranslation(it) }
        updateLastParseVersionOfWebpageVisit(webpageVisit.id, PARSE_VERSION)
    }

    @Transaction
    @Query("""SELECT * FROM webpage_visit_with_latest_parse_version_view
             LIMIT :maxLimit""")
    suspend fun getWebpageTranslationsAndVisits(maxLimit: Int = -1): List<WebpageVisitWithLatestTranslationView>
}