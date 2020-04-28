package com.azyoot.relearn.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.azyoot.relearn.service.worker.CheckAccessibilityServiceWorker

class AccessibilityCheckNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        CheckAccessibilityServiceWorker.schedule(context!!)

        if (intent?.getBooleanExtra(EXTRA_OPEN_ACCESSIBILITY_SETTINGS, false) == true) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            })
        }
    }

    companion object {
        const val EXTRA_OPEN_ACCESSIBILITY_SETTINGS = "open_accessibility_settings"
        const val ACTION = "accessibility_settings_check"
    }
}