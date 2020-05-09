package com.azyoot.relearn.data.entity

import androidx.room.*

@Entity(
    tableName = "webpage_translation", foreignKeys = [ForeignKey(
        entity = WebpageVisit::class,
        parentColumns = ["id"],
        childColumns = ["webpage_visit_id"]
    )]
)
data class WebpageTranslation(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "from_text") val fromText: String,
    @ColumnInfo(name = "to_text") val toText: String,
    @ColumnInfo(name = "webpage_visit_id") val webpageVisitId: Int,
    @ColumnInfo(name = "parse_version") val parseVersion: Int = PARSE_VERSION
)

@DatabaseView(
    viewName = "webpage_visit_with_latest_parse_version_view", value =
    """SELECT webpage_visit.id, 
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
data class WebpageVisitWithLatestTranslationView(
    @Embedded val webpageVisit: WebpageVisit,
    @Embedded(prefix = "translation_") val webpageTranslation: WebpageTranslation,
    @ColumnInfo(name = "max_translation_parse_version") val latestTranslatedVersion: Int
)

const val PARSE_VERSION = 4
