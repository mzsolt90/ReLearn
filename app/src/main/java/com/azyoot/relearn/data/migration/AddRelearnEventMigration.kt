package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AddRelearnEventMigration : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS `relearn_event` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `status` INTEGER NOT NULL, `webpage_visit_id` INTEGER, `translation_event_id` INTEGER, FOREIGN KEY(`webpage_visit_id`) REFERENCES `webpage_visit`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`translation_event_id`) REFERENCES `translation_event`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""")
    }

}