package com.azyoot.relearn.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AddLatestSourcesViewMigration : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE VIEW `LatestSourcesView` AS SELECT
    latest_visits.source AS source_text,
    latest_visits.latest_timestamp AS latest_timestamp,
    latest_visits.webpage_visit_id AS latest_source_id,
    relearn_event.status AS if_relearn_status,
    1 AS source_type
FROM
    (
        SELECT
            webpage_visit.url AS source,
            MAX(
                IFNULL(MAX(webpage_visit.timestamp), 0),
                IFNULL(MAX(relearn_event.timestamp), 0)
            ) AS latest_timestamp,
            MAX(webpage_visit.id) AS webpage_visit_id
        FROM
            webpage_visit
            LEFT JOIN relearn_event ON relearn_event.webpage_visit_id = webpage_visit.id
            INNER JOIN webpage_translation ON webpage_translation.webpage_visit_id = webpage_visit.id
        WHERE
            webpage_visit.last_parse_version = 3
        GROUP BY
            webpage_visit.url
    ) AS latest_visits
    LEFT JOIN relearn_event ON relearn_event.webpage_visit_id = latest_visits.webpage_visit_id
    AND relearn_event.timestamp = latest_visits.latest_timestamp
UNION
SELECT
    latest_translation_events.source AS source_text,
    latest_translation_events.latest_timestamp AS latest_timestamp,
    latest_translation_events.translation_event_id AS latest_source_id,
    relearn_event.status AS if_relearn_status,
    2 AS source_type
FROM
    (
        SELECT
            translation_event.from_text AS source,
            MAX(
                IFNULL(MAX(translation_event.timestamp), 0),
                IFNULL(MAX(relearn_event.timestamp), 0)
            ) AS latest_timestamp,
            MAX(translation_event.id) AS translation_event_id
        FROM
            translation_event
            LEFT JOIN relearn_event ON relearn_event.translation_event_id = translation_event.id
        GROUP BY
            translation_event.from_text
    ) AS latest_translation_events
    LEFT JOIN relearn_event ON relearn_event.translation_event_id = latest_translation_events.translation_event_id
    AND relearn_event.timestamp = latest_translation_events.latest_timestamp"""
        )
    }
}