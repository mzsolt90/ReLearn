package com.azyoot.relearn.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.ServiceSubcomponent
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_CREATED
import com.azyoot.relearn.domain.analytics.EVENT_SERVICE_DESTROYED
import com.azyoot.relearn.domain.entity.AccessibilityEventDescriptor
import com.azyoot.relearn.domain.entity.AccessibilityEventViewInfo
import com.azyoot.relearn.domain.usecase.ProcessAccessibilityEventUseCase
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject


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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.contains("com.android.chrome") != true) return // TODO move to service config
        val source = event.source ?: return

        val descriptor = AccessibilityEventDescriptor(
            event.packageName.toString(),
            AccessibilityEventViewInfo(
                source.viewIdResourceName ?: "",
                source.text?.toString() ?: ""
            )
        )
        processAccessibilityEventUseCase.onAccessibilityEvent(descriptor)

        source.recycle()
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