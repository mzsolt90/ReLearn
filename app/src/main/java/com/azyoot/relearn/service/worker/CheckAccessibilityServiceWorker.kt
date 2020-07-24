package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.ui.notification.EnableAccessibilityServiceNotificationFactory
import com.azyoot.relearn.ui.notification.ID_ACCESSIBILITY_CHECK
import com.azyoot.relearn.ui.notification.ensureChannelCreated
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CheckAccessibilityServiceWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val isEnabled: Boolean
        get() = MonitoringService.isRunning(applicationContext)

    @Inject
    lateinit var notificationFactory: EnableAccessibilityServiceNotificationFactory

    private val component: WorkerSubcomponent by lazy { (appContext.applicationContext as ReLearnApplication).appComponent.workerSubcomponent() }

    init {
        component.inject(this)
    }

    override fun doWork(): Result {
        Timber.d("Checking accessibility service")

        if (isEnabled) {
            Timber.d("It's running, let's see again in a week")
            schedule(applicationContext, 7)
        } else {
            Timber.d("It's NOT running, notifying")

            ensureChannelCreated(applicationContext)
            notificationFactory.create(applicationContext).also {
                NotificationManagerCompat.from(applicationContext).notify(ID_ACCESSIBILITY_CHECK, it)
            }

            schedule(applicationContext, 1)
        }

        return Result.success()
    }

    companion object {
        private const val NAME = "CheckAccessibilityServiceWorker"

        fun schedule(context: Context, days: Int = 1) {
            val request = OneTimeWorkRequestBuilder<CheckAccessibilityServiceWorker>()
                .setInitialDelay(days.toLong(), TimeUnit.DAYS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}