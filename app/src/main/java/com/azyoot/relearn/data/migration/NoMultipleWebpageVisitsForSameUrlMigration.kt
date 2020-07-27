package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class NoMultipleWebpageVisitsForSameUrlMigration : Migration(9, 10) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """DELETE FROM relearn_event
WHERE webpage_visit_id IN (
	SELECT webpage_visit.id 
	FROM webpage_visit 
	LEFT JOIN (
		SELECT url, MAX(id) max_id, COUNT(*) cnt 
		FROM webpage_visit
		GROUP BY url) max_ids 
	ON max_ids.url = webpage_visit.url 
		AND max_ids.max_id != webpage_visit.id 
	WHERE max_ids.url IS NOT NULL
)"""
        )

        database.execSQL(
            """DELETE FROM webpage_visit
WHERE ID IN (
	SELECT webpage_visit.id 
	FROM webpage_visit 
	LEFT JOIN (
		SELECT url, MAX(id) max_id, COUNT(*) cnt 
		FROM webpage_visit
		GROUP BY url) max_ids 
	ON max_ids.url = webpage_visit.url 
		AND max_ids.max_id != webpage_visit.id 
	WHERE max_ids.url IS NOT NULL
)"""
        )
    }

}