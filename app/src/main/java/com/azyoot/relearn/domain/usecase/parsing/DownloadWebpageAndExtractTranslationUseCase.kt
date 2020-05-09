package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.util.UrlProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class DownloadWebpageAndExtractTranslationUseCase @Inject constructor(
    private val httpClient: OkHttpClient,
    private val extractUseCase: ExtractWiktionaryTranslationUseCase,
    private val urlProcessing: UrlProcessing
) {

    suspend fun downloadWebpageAndExtractTranslation(webpageVisit: WebpageVisit): List<WebpageTranslation> {
        val webpageText = withContext(Dispatchers.IO) {
            val fixedUrl = urlProcessing.ensureStartsWithHttpsScheme(webpageVisit.url)

            val response = httpClient.newCall(Request.Builder().url(fixedUrl).build())
                .execute()
            when {
                response.isSuccessful && response.code == 200 -> response.body?.string()
                response.code == 404 -> response.body?.string() //TODO delete webpage visit
                else -> throw IOException("Unable to download webpage")
            }
        }
        return webpageText?.let { text ->
            withContext(Dispatchers.Default) {
                extractUseCase.extractTranslationsFromWiktionaryPage(webpageVisit, text)
            }
        } ?: listOf()
    }


}