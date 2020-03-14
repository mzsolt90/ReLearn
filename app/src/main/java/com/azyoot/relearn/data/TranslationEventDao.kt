package com.azyoot.relearn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.azyoot.relearn.data.entity.TranslationEvent


@Dao
interface TranslationEventDao {
    @Insert
    suspend fun addTranslationEvent(event: TranslationEvent)

    @Query("SELECT * FROM translation_event WHERE to_text LIKE :toText ORDER BY timestamp DESC")
    suspend fun getLastTranslationEventByResultText(toText: String): TranslationEvent?
}