package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.service.di.ServiceScope
import com.azyoot.relearn.util.stripFragmentFromUrl
import javax.inject.Inject

@ServiceScope
class ChromeUrlBarFlagAndSaveDataUseCase @Inject constructor(
    private val logWebpageVisitBufferUseCase: LogWebpageVisitBufferUseCase
): FlagAndSaveEventDataUseCase {
    override fun needsHierarchy(eventDescriptor: AccessibilityEventDescriptor) = false

    override fun isImportant(viewInfo: AccessibilityEventViewInfo) = viewInfo.viewResourceIdName.contains("id/url_bar") && viewInfo.text.isBlank().not()

    override suspend fun saveEventData(
        eventDescriptor: AccessibilityEventDescriptor,
        viewNodes: List<AccessibilityEventViewInfo>
    ) {
        logWebpageVisitBufferUseCase.logWebpageVisit(
            WebpageVisit(
                url = eventDescriptor.sourceViewInfo.text.stripFragmentFromUrl(),
                appPackageName = eventDescriptor.packageName
            )
        )
    }

}