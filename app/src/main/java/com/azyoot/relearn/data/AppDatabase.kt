package com.azyoot.relearn.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azyoot.relearn.data.entity.TranslationEvent
import com.azyoot.relearn.data.entity.WebpageVisit
import com.azyoot.relearn.di.AppScope

@AppScope
@Database(entities = [WebpageVisit::class, TranslationEvent::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webpageVisitDao(): WebpageVisitDao
    abstract fun translationEventDao(): TranslationEventDao
}
