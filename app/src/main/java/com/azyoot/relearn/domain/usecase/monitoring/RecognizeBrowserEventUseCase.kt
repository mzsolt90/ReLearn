package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import javax.inject.Inject

class RecognizeBrowserEventUseCase @Inject constructor() {

    private val urlBarMap = mapOf(
        "com.android.chrome" to "id/url_bar",
        "org.mozilla.firefox" to "id/url_bar_title"
    )

    fun isImportant(
        eventInfo: AccessibilityEventDescriptor,
        viewInfo: AccessibilityEventViewInfo
    ) = viewInfo.text.isBlank().not() && urlBarMap.any {
        eventInfo.packageName == it.key &&
                viewInfo.viewResourceIdName.contains(it.value)
    }
}