package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.service.ViewInfoFlagger
import com.azyoot.relearn.service.di.ServiceScope
import kotlinx.coroutines.*
import javax.inject.Inject

typealias ViewHierarchyProvider = (ViewInfoFlagger) -> List<AccessibilityEventViewInfo>

@ServiceScope
@JvmSuppressWildcards
class ProcessAccessibilityEventUseCase @Inject constructor(
    private val usecases: Set<FlagAndSaveEventDataUseCase>,
    private val coroutineScope: CoroutineScope
) {
    fun onAccessibilityEvent(
        eventInfo: AccessibilityEventDescriptor,
        viewHierarchyProvider: ViewHierarchyProvider
    ) {
        val needsHierarchy = usecases.any { it.needsHierarchy(eventInfo) }
        val jointFlagger = { viewInfo: AccessibilityEventViewInfo ->
            val flagged = usecases.any { it.isImportant(eventInfo, viewInfo) }
            flagged
        }

        val importantNodes = listOf<AccessibilityEventViewInfo>()
            .let { if (jointFlagger(eventInfo.sourceViewInfo)) it.plus(eventInfo.sourceViewInfo) else it }
            .let {
                if (needsHierarchy) it.plus(
                    viewHierarchyProvider.invoke(jointFlagger)
                )
                else it
            }

        if (importantNodes.isEmpty()) return

        coroutineScope.launch(Dispatchers.Default) {
            usecases.map { useCase ->
                async {
                    if (importantNodes.any { useCase.isImportant(eventInfo, it) }) {
                        useCase.saveEventData(eventInfo, importantNodes)
                    }
                }
            }.forEach {
                if (!isActive) return@forEach
                it.await()
            }
        }
    }

}