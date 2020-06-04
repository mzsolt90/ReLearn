package com.azyoot.relearn.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_CREATED
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_DESTROYED
import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.usecase.monitoring.ProcessAccessibilityEventUseCase
import com.azyoot.relearn.di.service.ServiceSubcomponent
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
        Timber.i("Monitoring service created")
    }

    override fun onInterrupt() {
    }

    private fun AccessibilityNodeInfo.toViewInfo() = AccessibilityEventViewInfo(
        viewIdResourceName ?: "",
        text?.toString() ?: "",
        isVisibleToUser,
        parent?.viewIdResourceName ?: "",
        (0 until (parent?.childCount ?: 0)).map { parent?.getChild(it) }.indexOf(this)
    )

    private fun getViewFlaggingTraverser(
        nodesToRecycle: MutableList<AccessibilityNodeInfo>,
        rootNode: AccessibilityNodeInfo
    ): (flagger: ViewInfoFlagger) -> List<AccessibilityEventViewInfo> =
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
            }

            traverseChildrenAndFlag(rootNode)
            nodesToRecycle.add(rootNode)
            rootNode.parent?.apply { nodesToRecycle.add(parent) }

            flaggedViews
        }

    private fun traverseNodeForDebug(nodeInfo: AccessibilityNodeInfo) {
        Timber.d(
            "id ${nodeInfo.viewIdResourceName} hint: ${nodeInfo.hintText} which child: ${(0 until (nodeInfo.parent?.childCount ?: 0)).map {
                nodeInfo.parent?.getChild(it)
            }
                .indexOf(nodeInfo)} parent: ${nodeInfo.parent?.viewIdResourceName} text: ${nodeInfo.text}"
        )
        repeat(nodeInfo.childCount) {
            val child = nodeInfo.getChild(it)
            traverseNodeForDebug(child)
        }
        try {
            nodeInfo.recycle()
        } catch (ex: Exception) {
            Timber.w(ex, "Error recycling node")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodesToRecycle = mutableListOf<AccessibilityNodeInfo>()

        val source = event?.source ?: return
        nodesToRecycle.add(source)
        nodesToRecycle.add(rootInActiveWindow)

        val descriptor = AccessibilityEventDescriptor(
            event.packageName.toString(),
            source.toViewInfo()
        )

        val processingResult = processAccessibilityEventUseCase.onAccessibilityEvent(
            descriptor,
            getViewFlaggingTraverser(nodesToRecycle, rootInActiveWindow ?: source)
        )

        if(processingResult == ProcessAccessibilityEventUseCase.ProcessResult.PROCESSED) {
            rescheduleWebpageDownloadJob()
        }

        nodesToRecycle.distinct().forEach {
            try {
                it.recycle()
            } catch (ex: Exception) {
                Timber.w(ex, "Error recycling node")
            }
        }
    }

    private fun rescheduleWebpageDownloadJob() {
        WebpageDownloadWorker.schedule(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()

        firebaseAnalytics.logEvent(EVENT_SERVICE_DESTROYED, Bundle.EMPTY)
        Timber.i("Monitoring service destroyed")
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