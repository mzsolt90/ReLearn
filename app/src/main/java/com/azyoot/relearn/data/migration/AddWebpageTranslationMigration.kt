package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AddWebpageTranslationMigration : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `webpage_translation` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `from_text` TEXT NOT NULL, `to_text` TEXT NOT NULL, `webpage_visit_id` INTEGER NOT NULL, `parse_version` INTEGER NOT NULL, FOREIGN KEY(`webpage_visit_id`) REFERENCES `webpage_visit`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
        database.execSQL("ALTER TABLE webpage_visit ADD last_parse_version INTEGER NOT NULL DEFAULT 0")
        database.execSQL(
            """CREATE VIEW `webpage_visit_with_latest_parse_version_view` AS SELECT webpage_visit.id, 
        webpage_visit.appPackageName, 
        webpage_visit.last_parse_version,
        webpage_visit.timestamp, 
        webpage_visit.url,
        webpage_translation.id as translation_id, 
        webpage_translation.parse_version as translation_parse_version, 
        webpage_translation.webpage_visit_id as translation_webpage_visit_id,
        webpage_translation.to_text as translation_to_text, 
        webpage_translation.from_text as translation_from_text,
        max_parse_version.max_translation_parse_version
     FROM webpage_visit 
        LEFT JOIN (SELECT webpage_translation.webpage_visit_id, 
                            MAX(webpage_translation.parse_version) AS max_translation_parse_version
                    FROM  webpage_translation
                     GROUP BY webpage_visit_id) max_parse_version 
            ON max_parse_version.webpage_visit_id = webpage_visit.id
        INNER JOIN webpage_translation 
            ON webpage_translation.webpage_visit_id = webpage_visit.id 
            AND webpage_translation.parse_version = max_translation_parse_version"""
        )
    }
}