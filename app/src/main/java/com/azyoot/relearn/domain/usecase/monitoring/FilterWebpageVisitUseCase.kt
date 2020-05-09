package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.util.UrlProcessing
import java.util.regex.Pattern
import javax.inject.Inject

class FilterWebpageVisitUseCase @Inject constructor(private val urlProcessing: UrlProcessing) {
    fun isWebpageVisitValid(url: String) =
        Pattern.compile(
            ".+?wiktionary\\.org/[\\p{L}/]+$"
        ).matcher(url).matches()
                && urlProcessing.isValidUrl(urlProcessing.ensureStartsWithHttpsScheme(url))
}