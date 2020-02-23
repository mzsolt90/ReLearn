package com.azyoot.relearn.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azyoot.relearn.data.entity.WebpageVisit
import com.azyoot.relearn.di.AppScope

@AppScope
@Database(entities = [WebpageVisit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webpageVisitDao(): WebpageVisitDao
}
