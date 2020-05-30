package com.azyoot.relearn.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.azyoot.relearn.data.entity.PARSE_VERSION
import com.azyoot.relearn.data.entity.WebpageTranslation
import com.azyoot.relearn.data.entity.WebpageVisit

@Dao
interface WebpageTranslationDao {

    @Insert
    suspend fun addWebpageTranslation(webpageTranslation: WebpageTranslation)

    @Query("UPDATE webpage_visit SET last_parse_version = :lastParseVersion WHERE id = :id")
    suspend fun updateLastParseVersionOfWebpageVisit(id: Int, lastParseVersion: Int)

    @Query("""SELECT * FROM webpage_translation WHERE webpage_visit_id = :webpageVisitId""")
    suspend fun getTranslationsForWebpageVisit(webpageVisitId: Int): List<WebpageTranslation>

    @Transaction
    suspend fun addWebpageTranslationForWebpageVisit(
        webpageVisit: WebpageVisit,
        webpageTranslation: List<WebpageTranslation>
    ) {
        webpageTranslation.forEach { addWebpageTranslation(it) }
        updateLastParseVersionOfWebpageVisit(webpageVisit.id, PARSE_VERSION)
    }
}