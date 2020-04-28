package com.azyoot.relearn.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.azyoot.relearn.R

const val CHANNEL_ID = "ReLearnNotifications"

fun ensureChannelCreated(context: Context){
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if(notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

    val name = context.getString(R.string.channel_name)
    val descriptionText = ""
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }

    notificationManager.createNotificationChannel(channel)
}