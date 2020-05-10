package com.azyoot.relearn.ui.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.service.receiver.ReLearnNotificationActionsReceiver
import com.azyoot.relearn.ui.common.RELEARN_ACCEPT
import com.azyoot.relearn.ui.common.RELEARN_LAUNCH
import com.azyoot.relearn.ui.common.RELEARN_SUPPRESS
import javax.inject.Inject


class ReLearnNotificationBuilder @Inject constructor(private val context: Context) {

    private val bulletSpan: BulletSpan
        get() = BulletSpan(
            context.resources.getDimensionPixelSize(R.dimen.notification_bullet_gap_width),
            ContextCompat.getColor(context, R.color.notification_bullet_color)
        )

    private fun getTitle(reLearnTranslation: ReLearnTranslation) =
        SpannableString(reLearnTranslation.sourceText).apply {
            setSpan(
                StyleSpan(BOLD),
                0,
                reLearnTranslation.sourceText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

    private fun getText(reLearnTranslation: ReLearnTranslation) =
        SpannableStringBuilder().apply {
            reLearnTranslation.translations.forEach { translation ->
                append(translation, bulletSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                append("\n")
            }
        }.trim()

    private fun ellipsizeText(string: CharSequence, length: Int = 8) =
        (if (string.length > length) "..." else "") + string.takeLast(length)

    private fun getSummaryText(reLearnTranslation: ReLearnTranslation) =
        ellipsizeText(getTitle(reLearnTranslation)) + " = " +
                reLearnTranslation.translations
                    .first()
                    .substringBefore(",")

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

    private fun getLaunchPendingIntent(reLearnTranslation: ReLearnTranslation): PendingIntent {
        val launchIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java)
            .apply {
                putExtra(
                    ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                    ReLearnNotificationActionsReceiver.TYPE_LAUNCH
                )
                reLearnTranslation.source.putInIntent(this)
            }
        return PendingIntent.getBroadcast(
            context,
            RELEARN_LAUNCH,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getAcceptPendingIntent(reLearnTranslation: ReLearnTranslation): PendingIntent {
        val acceptIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java).apply {
            putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                ReLearnNotificationActionsReceiver.TYPE_ACCEPT
            )
            reLearnTranslation.source.putInIntent(this)
        }
        return PendingIntent.getBroadcast(
            context,
            RELEARN_ACCEPT,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getSuppressPendingIntent(reLearnTranslation: ReLearnTranslation): PendingIntent {
        val suppressIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java).apply {
            putExtra(
                ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                ReLearnNotificationActionsReceiver.TYPE_SUPPRESS
            )
            reLearnTranslation.source.putInIntent(this)
        }
        return PendingIntent.getBroadcast(
            context,
            RELEARN_SUPPRESS,
            suppressIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun createAndNotify(reLearnTranslation: ReLearnTranslation) {
        ensureChannelCreated(context)

        val pendingLaunchIntent = getLaunchPendingIntent(reLearnTranslation)
        val pendingAcceptIntent = getAcceptPendingIntent(reLearnTranslation)
        val pendingSuppressIntent = getSuppressPendingIntent(reLearnTranslation)

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