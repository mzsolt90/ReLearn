package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo

interface FlagAndSaveEventDataUseCase {
    fun needsHierarchy(eventDescriptor: AccessibilityEventDescriptor): Boolean
    fun isImportant(viewInfo: AccessibilityEventViewInfo): Boolean

    suspend fun saveEventData(eventDescriptor: AccessibilityEventDescriptor, viewNodes: List<AccessibilityEventViewInfo>)
}