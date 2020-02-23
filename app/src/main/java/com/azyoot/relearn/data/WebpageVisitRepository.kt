package com.azyoot.relearn.data

import com.azyoot.relearn.di.AppScope
import com.azyoot.relearn.domain.entity.WebpageVisit
import java.time.ZoneOffset
import javax.inject.Inject

@AppScope
class WebpageVisitRepository @Inject constructor(private val database: AppDatabase) {

    suspend fun getLastWebpageVisitForUrl(url: String) = database.webpageVisitDao().getLastWebpageVisitByUrl(url)

    suspend fun saveWebpageVisit(webpageVisit: WebpageVisit){
        database.webpageVisitDao().addWebpageVisit(com.azyoot.relearn.data.entity.WebpageVisit(
            url = webpageVisit.url,
            appPackageName = webpageVisit.appPackageName,
            timestamp = webpageVisit.time.toInstant(ZoneOffset.UTC).toEpochMilli()
        ))
    }
}