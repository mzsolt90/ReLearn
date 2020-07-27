package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.di.service.ServiceScope
import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.service.ViewHierarchyProvider
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@ServiceScope
@JvmSuppressWildcards
class ProcessAccessibilityEventUseCase @Inject constructor(
    private val usecases: Set<FlagAndSaveEventDataUseCase>,
    private val coroutineScope: CoroutineScope
) {
    sealed class ProcessResult {
        object NOT_IMPORTANT : ProcessResult()
        object PROCESSED : ProcessResult()
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

        Timber.d("Found ${importantNodes.size} important nodes")

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