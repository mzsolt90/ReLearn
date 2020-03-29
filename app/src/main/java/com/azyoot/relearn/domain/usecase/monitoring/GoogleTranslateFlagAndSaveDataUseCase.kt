package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.entity.TranslationEvent
import com.azyoot.relearn.service.di.ServiceScope
import javax.inject.Inject

@ServiceScope
class GoogleTranslateFlagAndSaveDataUseCase @Inject constructor(private val saveDataUseCase: LogTranslationEventWithBufferUseCase) :
    FlagAndSaveEventDataUseCase {
    override fun needsHierarchy(eventDescriptor: AccessibilityEventDescriptor) =
        eventDescriptor.packageName == "com.google.android.apps.translate"

    override fun isImportant(
        eventInfo: AccessibilityEventDescriptor,
        viewInfo: AccessibilityEventViewInfo
    ) = eventInfo.packageName == "com.google.android.apps.translate" &&
            viewInfo.viewResourceIdName.endsWith("text1") &&
            viewInfo.parentViewResourceIdName.endsWith("resultScrollView")


    override suspend fun saveEventData(
        eventDescriptor: AccessibilityEventDescriptor,
        viewNodes: List<AccessibilityEventViewInfo>
    ) {
        val candidateNodes = viewNodes.filter {
            it.viewResourceIdName.endsWith("text1") && it.parentViewResourceIdName.endsWith("resultScrollView")
        }
        val fromText = candidateNodes.minBy { it.orderInChildrenOfParent }?.text
        val toText = candidateNodes.maxBy { it.orderInChildrenOfParent }?.text

        if (fromText.isNullOrEmpty() || toText.isNullOrEmpty()) return

        saveDataUseCase.logTranslationEvent(
            TranslationEvent(
                fromText = fromText,
                toText = toText
            )
        )
    }

}