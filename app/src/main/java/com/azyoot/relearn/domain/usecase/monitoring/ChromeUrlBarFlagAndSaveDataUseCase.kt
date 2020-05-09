package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.di.service.ServiceScope
import com.azyoot.relearn.util.UrlProcessing
import timber.log.Timber
import javax.inject.Inject

@ServiceScope
class ChromeUrlBarFlagAndSaveDataUseCase @Inject constructor(
    private val saveDataUseCase: LogWebpageVisitBufferUseCase,
    private val filterWebpageVisitUseCase: FilterWebpageVisitUseCase,
    private val urlProcessing: UrlProcessing
) : FlagAndSaveEventDataUseCase {
    override fun needsHierarchy(eventDescriptor: AccessibilityEventDescriptor) = false

    override fun isImportant(
        eventInfo: AccessibilityEventDescriptor,
        viewInfo: AccessibilityEventViewInfo
    ) =
        eventInfo.packageName == "com.android.chrome" &&
                viewInfo.viewResourceIdName.contains("id/url_bar") &&
                viewInfo.text.isBlank().not()

    override suspend fun saveEventData(
        eventDescriptor: AccessibilityEventDescriptor,
        viewNodes: List<AccessibilityEventViewInfo>
    ) {
        val url = eventDescriptor.sourceViewInfo.text
            .let { urlProcessing.stripFragmentFromUrl(it) }
            .let { urlProcessing.ensureStartsWithHttpsScheme(it) }

        if (!filterWebpageVisitUseCase.isWebpageVisitValid(url)) {
            Timber.v("Filtering out $url")
            return
        }

        saveDataUseCase.logWebpageVisit(
            WebpageVisit(
                url = url,
                appPackageName = eventDescriptor.packageName,
                lastParseVersion = 0
            )
        )
    }

}