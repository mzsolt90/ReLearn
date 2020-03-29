package com.azyoot.relearn.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azyoot.relearn.data.entity.*
import com.azyoot.relearn.di.AppScope

@AppScope
@Database(
    entities = [WebpageVisit::class, TranslationEvent::class, WebpageTranslation::class],
    views = [WebpageVisitWithLatestTranslationView::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webpageVisitDao(): WebpageVisitDao
    abstract fun translationEventDao(): TranslationEventDao
    abstract fun webpageTranslationDao(): WebpageTranslationDao
}
