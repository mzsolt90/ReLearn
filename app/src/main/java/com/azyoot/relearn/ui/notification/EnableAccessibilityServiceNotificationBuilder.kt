package com.azyoot.relearn.ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.service.receiver.AccessibilityCheckNotificationReceiver
import javax.inject.Inject

class EnableAccessibilityServiceNotificationFactory @Inject constructor() {

    fun create(context: Context): Notification {
        val intent = Intent(AccessibilityCheckNotificationReceiver.ACTION)
            .apply {
                putExtra(
                    AccessibilityCheckNotificationReceiver.EXTRA_OPEN_ACCESSIBILITY_SETTINGS,
                    true
                )
            }
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_standalone)
            .setContentTitle(context.resources.getString(R.string.app_name))
            .setContentText(context.resources.getString(R.string.dialog_enable_accessibility_service))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        return builder.build();
    }
}