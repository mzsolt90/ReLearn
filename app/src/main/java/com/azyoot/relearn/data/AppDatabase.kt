package com.azyoot.relearn.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azyoot.relearn.data.dao.*
import com.azyoot.relearn.data.entity.*

@Database(
    entities = [WebpageVisit::class, TranslationEvent::class, WebpageTranslation::class, RelearnEvent::class, LatestSourcesCache::class],
    views = [WebpageVisitWithLatestTranslationView::class, LatestSourcesView::class],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webpageVisitDao(): WebpageVisitDao
    abstract fun translationEventDao(): TranslationEventDao
    abstract fun webpageTranslationDao(): WebpageTranslationDao
    abstract fun relearnEventDao(): RelearnEventDaoInternal
}
