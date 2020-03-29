package com.azyoot.relearn.data

import com.azyoot.relearn.data.mapper.WebpageTranslationMapper
import com.azyoot.relearn.data.mapper.WebpageVisitMapper
import com.azyoot.relearn.domain.entity.WebpageVisit
import javax.inject.Inject
import com.azyoot.relearn.domain.entity.WebpageTranslation as DomainEntity

class WebpageTranslationRepository @Inject constructor(
    private val database: AppDatabase,
    private val webpageTranslationMapper: WebpageTranslationMapper,
    private val webpageVisitMapper: WebpageVisitMapper
) {

    suspend fun addWebpageTranslationsForWebpageVisit(
        webpageVisit: WebpageVisit,
        webpageTranslations: List<DomainEntity>
    ) {
        database.webpageTranslationDao().addWebpageTranslationForWebpageVisit(
            webpageVisitMapper.toDataEntity(webpageVisit),
            webpageTranslations.map { webpageTranslationMapper.toDataEntity(it) })
    }
}