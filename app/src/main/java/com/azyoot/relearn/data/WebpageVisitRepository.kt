package com.azyoot.relearn.data

import androidx.paging.Config
import androidx.paging.toLiveData
import com.azyoot.relearn.di.AppScope
import com.azyoot.relearn.util.executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executor
import javax.inject.Inject
import com.azyoot.relearn.data.entity.WebpageVisit as DataWebpageVisit
import com.azyoot.relearn.domain.entity.WebpageVisit as DomainWebpageVisit

@AppScope
class WebpageVisitRepository @Inject constructor(private val database: AppDatabase) {

    private fun DomainWebpageVisit.toDataEntity() = com.azyoot.relearn.data.entity.WebpageVisit(
        url = this.url,
        appPackageName = this.appPackageName,
        timestamp = this.time.toInstant(ZoneOffset.UTC).toEpochMilli()
    )

    private fun DataWebpageVisit.toDomainEntity() = DomainWebpageVisit(
        databaseId = this.id,
        url = this.url,
        appPackageName = this.appPackageName,
        time = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                this.timestamp
            ), ZoneOffset.UTC
        )
    )

    fun getWebpageVisitsByTimeDesc(coroutineScope: CoroutineScope) =
        database.webpageVisitDao().getAllWebpagesByDate().map { it.toDomainEntity() }.toLiveData(
            config = Config(pageSize = 10, enablePlaceholders = false),
            fetchExecutor = coroutineScope.executor()
        )

    suspend fun getLastWebpageVisitForUrl(url: String) =
        database.webpageVisitDao().getLastWebpageVisitByUrl(url)?.toDomainEntity()

    suspend fun saveWebpageVisit(webpageVisit: DomainWebpageVisit) {
        database.webpageVisitDao().addWebpageVisit(webpageVisit.toDataEntity())
    }
}