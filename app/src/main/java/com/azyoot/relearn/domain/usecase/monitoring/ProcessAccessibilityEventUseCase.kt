package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.service.ViewInfoFlagger
import com.azyoot.relearn.di.service.ServiceScope
import kotlinx.coroutines.*
import javax.inject.Inject

typealias ViewHierarchyProvider = (ViewInfoFlagger) -> List<AccessibilityEventViewInfo>

@ServiceScope
@JvmSuppressWildcards
class ProcessAccessibilityEventUseCase @Inject constructor(
    private val usecases: Set<FlagAndSaveEventDataUseCase>,
    private val coroutineScope: CoroutineScope
) {
    sealed class ProcessResult {
        object NOT_IMPORTANT: ProcessResult()
        object PROCESSED: ProcessResult()
    }

    fun onAccessibilityEvent(
        eventInfo: AccessibilityEventDescriptor,
        viewHierarchyProvider: ViewHierarchyProvider
    ): ProcessResult {
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

        if (importantNodes.isEmpty()) return ProcessResult.NOT_IMPORTANT

        coroutineScope.launch(Dispatchers.Default) {
            usecases.map { useCase ->
                async {
                    if (importantNodes.any { useCase.isImportant(eventInfo, it) }) {
                        useCase.saveEventData(eventInfo, importantNodes)
                    }
                }
            }.forEach {
                yield()
                it.await()
            }
        }

        return ProcessResult.PROCESSED
    }

}