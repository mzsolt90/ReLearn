package com.azyoot.relearn.ui.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.service.receiver.ReLearnNotificationActionsReceiver
import com.azyoot.relearn.ui.common.RELEARN_ACCEPT
import com.azyoot.relearn.ui.common.RELEARN_LAUNCH
import com.azyoot.relearn.ui.common.RELEARN_SUPPRESS
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

    private fun getTitle(reLearnTranslation: ReLearnTranslation) = reLearnTranslation.sourceText

    private fun getText(reLearnTranslation: ReLearnTranslation) =
        reLearnTranslation.translations.joinToString("\n• ", "• ")

    private fun getSummaryText(reLearnTranslation: ReLearnTranslation) = (if(getTitle(reLearnTranslation).length > 8)"..." else "") +
            "${getTitle(reLearnTranslation).takeLast(8)} = " +
            reLearnTranslation.translations.first()
                .substringBefore(",")

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
            PendingIntent.getBroadcast(context, RELEARN_LAUNCH, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingAcceptIntent =
            PendingIntent.getBroadcast(context, RELEARN_ACCEPT, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingSuppressIntent =
            PendingIntent.getBroadcast(
                context,
                RELEARN_SUPPRESS,
                suppressIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getTitle(reLearnTranslation))
            .setContentText(reLearnTranslation.translations.first())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getText(reLearnTranslation))
                    .setSummaryText(getSummaryText(reLearnTranslation))
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