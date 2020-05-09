package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.data.repository.WebpageTranslationRepository
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.usecase.monitoring.FilterWebpageVisitUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import timber.log.Timber
import javax.inject.Inject

class DownloadLastWebpagesAndStoreTranslationsUseCase @Inject constructor(
    private val webpageVisitRepository: WebpageVisitRepository,
    private val webpageTranslationRepository: WebpageTranslationRepository,
    private val downloadUseCase: DownloadWebpageAndExtractTranslationUseCase,
    private val deleteWebpageVisitUseCase: DeleteWebpageVisitUseCase,
    private val filterWebpageVisitUseCase: FilterWebpageVisitUseCase
) {

    suspend fun downloadLastWebpagesAndStoreTranslations() {
        val visits = webpageVisitRepository.getUnparsedWebpageVisits(BATCH_SIZE)
        withContext(Dispatchers.IO) {
            visits.map { webpageVisit ->
                async {
                    if (!filterWebpageVisitUseCase.isWebpageVisitValid(webpageVisit.url)) {
                        deleteWebpageVisitUseCase.deleteWebpageVisit(webpageVisit)
                        return@async
                    }

                    val translations: List<WebpageTranslation>
                    try {
                        Timber.d("Downloading webpage ${webpageVisit.url}")
                        translations =
                            downloadUseCase.downloadWebpageAndExtractTranslation(webpageVisit)
                    } catch (ex: IllegalArgumentException) {
                        Timber.e(ex, "Error downloading webpage")
                        return@async
                    }

                    yield()
                    webpageTranslationRepository.addWebpageTranslationsForWebpageVisit(
                        webpageVisit,
                        translations
                    )
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