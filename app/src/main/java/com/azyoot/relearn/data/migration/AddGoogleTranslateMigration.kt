package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AddGoogleTranslateMigration : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `translation_event` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `from_text` TEXT NOT NULL, `to_text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
    }
}