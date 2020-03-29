package com.azyoot.relearn.data

import com.azyoot.relearn.data.mapper.WebpageVisitMapper
import com.azyoot.relearn.di.AppScope
import javax.inject.Inject
import com.azyoot.relearn.domain.entity.WebpageVisit as DomainWebpageVisit

@AppScope
class WebpageVisitRepository @Inject constructor(
    private val database: AppDatabase,
    private val mapper: WebpageVisitMapper
) {

    suspend fun getWebpageVisitsByTimeDesc(limit: Int = -1) =
        database.webpageVisitDao().getAllWebpagesByDate(limit).map { mapper.toDomainEntity(it) }

    suspend fun getLastWebpageVisitForUrl(url: String) =
        database.webpageVisitDao().getLastWebpageVisitByUrl(url)?.let { mapper.toDomainEntity(it) }

    suspend fun saveWebpageVisit(webpageVisit: DomainWebpageVisit) {
        database.webpageVisitDao().addWebpageVisit(mapper.toDataEntity(webpageVisit))
    }

    suspend fun getUnparsedWebpageVisits(limit: Int = 10) =
        database.webpageVisitDao().getUnparsedWebpageVisits(limit = limit)
            .map { mapper.toDomainEntity(it) }

    suspend fun getUnparsedWebpageVisitCount() =
        database.webpageVisitDao().getUnparsedWebpageVisitCount()

}