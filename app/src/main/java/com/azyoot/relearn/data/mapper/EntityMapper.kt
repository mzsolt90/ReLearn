package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.domain.entity.TranslationEvent
import com.azyoot.relearn.domain.entity.WebpageVisit
import java.time.LocalDateTime

val UNSET_WEBPAGE_VISIT = WebpageVisit(url = "", appPackageName = "", time = LocalDateTime.MIN)
val UNSET_TRANSLATION_EVENT =
    TranslationEvent(fromText = "", toText = "", timestamp = LocalDateTime.MIN, databaseId = 0)

interface EntityMapper<DOMAIN, DATA> {
    fun toDataEntity(domainEntity: DOMAIN): DATA
    fun toDomainEntity(dataEntity: DATA): DOMAIN
}