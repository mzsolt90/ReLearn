package com.azyoot.relearn.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_CREATED
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_DESTROYED
import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.usecase.ProcessAccessibilityEventUseCase
import com.azyoot.relearn.service.di.ServiceSubcomponent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

typealias ViewInfoFlagger = (AccessibilityEventViewInfo) -> Boolean

class MonitoringService : AccessibilityService() {

    private lateinit var component: ServiceSubcomponent

    @Inject
    lateinit var processAccessibilityEventUseCase: ProcessAccessibilityEventUseCase

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineJob)

    override fun onCreate() {
        super.onCreate()

        component =
            (applicationContext as ReLearnApplication).appComponent.serviceSubcomponentBuilder()
                .coroutineScope(coroutineScope)
                .build()
        component.inject(this)

        firebaseAnalytics.logEvent(EVENT_SERVICE_CREATED, Bundle.EMPTY)
    }

    override fun onInterrupt() {

    }

    private fun AccessibilityNodeInfo.toViewInfo() = AccessibilityEventViewInfo(
        this.viewIdResourceName ?: "",
        this.text?.toString() ?: ""
    )

    private fun getViewFlaggingTraverser(rootNode: AccessibilityNodeInfo): (flagger: ViewInfoFlagger) -> List<AccessibilityEventViewInfo> =
        { flagger ->
            val flaggedViews = mutableListOf<AccessibilityEventViewInfo>()

            fun traverseChildrenAndFlag(nodeInfo: AccessibilityNodeInfo) {
                repeat(nodeInfo.childCount) {
                    nodeInfo.getChild(it).also { childNodeInfo ->
                        childNodeInfo.toViewInfo().also { childViewInfoDescriptor ->
                            if (flagger(childViewInfoDescriptor)) {
                                flaggedViews.add(childViewInfoDescriptor)
                            }
                        }
                    }

                    traverseChildrenAndFlag(nodeInfo.getChild(it))
                }

                try {
                    nodeInfo.recycle()
                } catch (ex: IllegalStateException) {
                }
            }

            traverseChildrenAndFlag(rootNode)

            flaggedViews
        }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.contains("com.android.chrome") != true &&
            event?.packageName?.contains("com.google.android.apps.translate") != true
        ) return // TODO move to service config
        val source = event.source ?: return

        val descriptor = AccessibilityEventDescriptor(
            event.packageName.toString(),
            source.toViewInfo()
        )
        processAccessibilityEventUseCase.onAccessibilityEvent(
            descriptor,
            getViewFlaggingTraverser(source)
        )

        try {
            source.recycle()
        } catch (ex: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()

        firebaseAnalytics.logEvent(EVENT_SERVICE_DESTROYED, Bundle.EMPTY)
    }

    companion object {
        fun isRunning(context: Context) =
            Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )?.contains(context.packageName.toString() + "/" + MonitoringService::class.java.name)
                ?: false
    }
}