package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.azyoot.relearn.data.entity.ENTITY_TYPE_TRANSLATION
import com.azyoot.relearn.data.entity.ENTITY_TYPE_WEBPAGE
import com.azyoot.relearn.data.entity.PARSE_VERSION

class SoftDeletionSupportMigration : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE relearn_event ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE webpage_visit ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE translation_event ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0")

        database.execSQL("DROP VIEW LatestSourcesView")
        database.execSQL(
            """CREATE VIEW `LatestSourcesView` AS SELECT
     latest_visits.source AS source_text,
     latest_visits.latest_webpage_timestamp AS latest_source_timestamp,
     latest_visits.webpage_visit_id AS latest_source_id,
     latest_visits.latest_relearn_timestamp AS latest_relearn_timestamp,
     relearn_event.status AS latest_relearn_status,
     MAX(latest_visits.latest_webpage_timestamp, IFNULL(latest_visits.latest_relearn_timestamp, 0)) AS latest_timestamp,
     $ENTITY_TYPE_WEBPAGE AS source_type,
     webpage_visit.id AS webpage_visit_id,
     webpage_visit.url AS webpage_visit_url,
     webpage_visit.timestamp AS webpage_visit_timestamp,
     webpage_visit.last_parse_version AS webpage_visit_last_parse_version,
     webpage_visit.appPackageName AS webpage_visit_appPackageName,
     0 AS webpage_visit_deleted,
     NULL AS translation_event_id,
     NULL AS translation_event_timestamp,
     NULL AS translation_event_from_text,
     NULL AS translation_event_to_text,
     NULL AS translation_event_deleted
 FROM
     (
         SELECT
             webpage_visit.url AS source,
             MAX(webpage_visit.timestamp) AS latest_webpage_timestamp,
             MAX(relearn_event.timestamp) AS latest_relearn_timestamp,
             MAX(webpage_visit.id) AS webpage_visit_id
         FROM
             webpage_visit
             LEFT JOIN relearn_event ON relearn_event.webpage_visit_id = webpage_visit.id
                AND relearn_event.deleted = 0
             INNER JOIN webpage_translation ON webpage_translation.webpage_visit_id = webpage_visit.id
         WHERE
             webpage_visit.last_parse_version = $PARSE_VERSION
             AND webpage_visit.deleted = 0
         GROUP BY
             webpage_visit.url
     ) AS latest_visits
     LEFT JOIN relearn_event ON relearn_event.webpage_visit_id = latest_visits.webpage_visit_id
		AND relearn_event.timestamp = latest_visits.latest_relearn_timestamp
	 INNER JOIN webpage_visit ON webpage_visit.id = latest_visits.webpage_visit_id
 UNION
 SELECT
     latest_translation_events.source AS source_text,
     latest_translation_events.latest_translation_timestamp AS latest_source_timestamp,
     latest_translation_events.translation_event_id AS latest_source_id,
     latest_translation_events.latest_relearn_timestamp AS latest_relearn_timestamp,
     relearn_event.status AS latest_relearn_status,
     MAX(latest_translation_events.latest_translation_timestamp, IFNULL(latest_translation_events.latest_relearn_timestamp, 0)) AS latest_timestamp,
     $ENTITY_TYPE_TRANSLATION AS source_type,
     NULL AS webpage_visit_id,
     NULL AS webpage_visit_url,
     NULL AS webpage_visit_timestamp,
     NULL AS webpage_visit_last_parse_version,
     NULL AS webpage_visit_appPackageName,
     NULL AS webpage_visit_deleted,
     translation_event.id AS translation_event_id,
     translation_event.timestamp AS translation_event_timestamp,
     translation_event.from_text AS translation_event_from_text,
     translation_event.to_text AS translation_event_to_text,
     0 AS translation_event_deleted
 FROM
     (
         SELECT
             translation_event.from_text AS source,
            MAX(translation_event.timestamp) AS latest_translation_timestamp,
             MAX(relearn_event.timestamp) AS latest_relearn_timestamp,
             MAX(translation_event.id) AS translation_event_id
         FROM
             translation_event
             LEFT JOIN relearn_event ON relearn_event.translation_event_id = translation_event.id
                AND relearn_event.deleted = 0
         WHERE translation_event.deleted = 0
         GROUP BY
             translation_event.from_text
     ) AS latest_translation_events
     LEFT JOIN relearn_event ON relearn_event.translation_event_id = latest_translation_events.translation_event_id
		AND relearn_event.timestamp = latest_translation_events.latest_relearn_timestamp
	 INNER JOIN translation_event ON translation_event.id = latest_translation_events.translation_event_id"""
        )
    }

}