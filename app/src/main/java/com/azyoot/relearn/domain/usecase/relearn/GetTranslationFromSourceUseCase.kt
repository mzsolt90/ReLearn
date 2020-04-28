package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.WebpageTranslationRepository
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetTranslationFromSourceUseCase @Inject constructor(private val webpageTranslationRepository: WebpageTranslationRepository) {

    suspend fun getTranslationFromSource(source: ReLearnSource): ReLearnTranslation {
        if (source.translationEvent != null) {
            return ReLearnTranslation(source, listOf(source.translationEvent.toText))
        }
        return withContext(Dispatchers.IO) {
            Timber.d("Getting translations for ${source.sourceText}")

            val translations =
                webpageTranslationRepository.getTranslationsForWebpageVisit(source.webpageVisit!!)
            ReLearnTranslation(source, translations.map { it.toText })
        }
    }
}