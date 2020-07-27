package com.azyoot.relearn.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azyoot.relearn.data.dao.RelearnEventDaoInternal
import com.azyoot.relearn.data.dao.TranslationEventDao
import com.azyoot.relearn.data.dao.WebpageTranslationDao
import com.azyoot.relearn.data.dao.WebpageVisitDao
import com.azyoot.relearn.data.entity.*

@Database(
    entities = [WebpageVisit::class, TranslationEvent::class, WebpageTranslation::class, RelearnEvent::class, LatestSourcesCache::class],
    views = [WebpageVisitWithLatestTranslationView::class, LatestSourcesView::class],
    version = 11,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webpageVisitDao(): WebpageVisitDao
    abstract fun translationEventDao(): TranslationEventDao
    abstract fun webpageTranslationDao(): WebpageTranslationDao
    abstract fun relearnEventDao(): RelearnEventDaoInternal
}
