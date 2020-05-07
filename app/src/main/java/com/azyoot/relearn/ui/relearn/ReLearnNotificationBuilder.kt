package com.azyoot.relearn.ui.relearn

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.ui.notification.CHANNEL_ID
import com.azyoot.relearn.ui.notification.ID_RELEARN
import com.azyoot.relearn.ui.notification.ensureChannelCreated
import javax.inject.Inject


class ReLearnNotificationBuilder @Inject constructor() {

    private fun ReLearnSource.putInIntent(intent: Intent) {
        intent.putExtra(ReLearnNotificationActionsReceiver.EXTRA_SOURCE_ID, latestSourceId)
        intent.putExtra(ReLearnNotificationActionsReceiver.EXTRA_SOURCE_TYPE, sourceType)

        if (sourceType == SourceType.WEBPAGE_VISIT) {
            intent.putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_LAUNCH_URL,
                webpageVisit?.url
            )
        } else {
            intent.putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_TRANSLATE_TEXT,
                sourceText
            )
        }
    }

    fun createAndNotify(reLearnTranslation: ReLearnTranslation, context: Context) {
        ensureChannelCreated(context)

        val launchIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java)
            .apply {
                putExtra(
                    ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                    ReLearnNotificationActionsReceiver.TYPE_LAUNCH
                )
                reLearnTranslation.source.putInIntent(this)
            }
        val acceptIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java).apply {
            putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                ReLearnNotificationActionsReceiver.TYPE_ACCEPT
            )
            reLearnTranslation.source.putInIntent(this)
        }
        val suppressIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java).apply {
            putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                ReLearnNotificationActionsReceiver.TYPE_SUPPRESS
            )
            reLearnTranslation.source.putInIntent(this)
        }

        val pendingLaunchIntent =
            PendingIntent.getBroadcast(context, 1, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingAcceptIntent =
            PendingIntent.getBroadcast(context, 2, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingSuppressIntent =
            PendingIntent.getBroadcast(
                context,
                3,
                suppressIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(reLearnTranslation.source.sourceText)
            .setContentText(reLearnTranslation.translations.first())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(reLearnTranslation.translations.joinToString("\n• ", "• "))
                    .setSummaryText(
                        (if(reLearnTranslation.source.sourceText.length > 8)"..." else "") +
                                "${reLearnTranslation.source.sourceText.takeLast(8)} = " +
                                reLearnTranslation.translations.first()
                            .substringBefore(",")
                    )
            )
            .addAction(
                NotificationCompat.Action(
                    null,
                    context.resources.getString(R.string.action_notification_dismiss),
                    pendingAcceptIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    null,
                    context.resources.getString(R.string.action_notification_suppress),
                    pendingSuppressIntent
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingLaunchIntent)

        NotificationManagerCompat.from(context).notify(ID_RELEARN, builder.build())
    }
}