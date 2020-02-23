package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.di.ServiceScope
import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.util.stripFragmentFromUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@ServiceScope
class ProcessAccessibilityEventUseCase @Inject constructor(
    private val logWebpageVisitBufferUseCase: LogWebpageVisitBufferUseCase,
    private val coroutineScope: CoroutineScope
) {
    fun onAccessibilityEvent(eventInfo: AccessibilityEventDescriptor) {
        if (eventInfo.viewInfo.viewResourceIdName.contains("id/url_bar") && eventInfo.viewInfo.text.isEmpty().not()) {
            coroutineScope.launch(Dispatchers.Default) {
                logWebpageVisitBufferUseCase.logWebpageVisit(
                    WebpageVisit(
                        url = eventInfo.viewInfo.text.stripFragmentFromUrl(),
                        appPackageName = eventInfo.packageName
                    )
                )
            }
        }
    }

}