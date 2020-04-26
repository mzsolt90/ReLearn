package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.data.repository.WebpageTranslationRepository
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

class DownloadLastWebpagesAndStoreTranslationsUseCase @Inject constructor(
    private val webpageVisitRepository: WebpageVisitRepository,
    private val webpageTranslationRepository: WebpageTranslationRepository,
    private val downloadUseCase: DownloadWebpageAndExtractTranslationUseCase,
    private val deleteInvalidWebpageVisitUseCase: DeleteInvalidWebpageVisitUseCase
) {

    suspend fun downloadLastWebpagesAndStoreTranslations() {
        val visits = webpageVisitRepository.getUnparsedWebpageVisits(BATCH_SIZE)
        withContext(Dispatchers.IO) {
            visits.map { webpageVisit ->
                async {
                    val translations: List<WebpageTranslation>
                    try {
                        Timber.d("Downloading webpage ${webpageVisit.url}")
                        translations = downloadUseCase.downloadWebpageAndExtractTranslation(webpageVisit)
                    } catch (ex: IllegalArgumentException){
                        Timber.w(ex, "Error downloading webpage")
                        deleteInvalidWebpageVisitUseCase.deleteWebpageVisitIfInvalid(webpageVisit)
                        return@async
                    }

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