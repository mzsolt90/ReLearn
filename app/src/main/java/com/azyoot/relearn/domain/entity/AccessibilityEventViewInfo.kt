package com.azyoot.relearn.domain.entity

data class AccessibilityEventViewInfo(
    val viewResourceIdName: String,
    val text: String,
    val isVisible: Boolean,
    val parentViewResourceIdName: String,
    val orderInChildrenOfParent: Int
)