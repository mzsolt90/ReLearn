package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.data.WebpageTranslationRepository
import com.azyoot.relearn.data.WebpageVisitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject

class DownloadLastWebpagesAndStoreTranslationsUseCase @Inject constructor(
    private val webpageVisitRepository: WebpageVisitRepository,
    private val webpageTranslationRepository: WebpageTranslationRepository,
    private val usecase: DownloadWebpageAndExtractTranslationUseCase
) {

    suspend fun downloadLastWebpagesAndStoreTranslations() {
        val visits = webpageVisitRepository.getUnparsedWebpageVisits(BATCH_SIZE)
        withContext(Dispatchers.IO) {
            visits.map { webpageVisit ->
                async {
                    val translations = usecase.downloadWebpageAndExtractTranslation(webpageVisit)
                    yield()
                    webpageTranslationRepository.addWebpageTranslationsForWebpageVisit(webpageVisit, translations)
                }
            }.forEach {
                it.await()
            }
        }
    }

    companion object {
        const val BATCH_SIZE = 10
    }
}