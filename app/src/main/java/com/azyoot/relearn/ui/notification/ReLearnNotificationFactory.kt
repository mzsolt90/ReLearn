package com.azyoot.relearn.ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import com.azyoot.relearn.R
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.service.receiver.ReLearnNotificationActionsReceiver
import com.azyoot.relearn.ui.common.RELEARN_ANOTHER
import com.azyoot.relearn.ui.common.RELEARN_LAUNCH
import com.azyoot.relearn.ui.relearn.ReLearnTranslationFormatter
import javax.inject.Inject


class ReLearnNotificationFactory @Inject constructor(
    private val context: Context,
    private val reLearnTranslationFormatter: ReLearnTranslationFormatter
) {

    private fun getTitle(reLearnTranslation: ReLearnTranslation) =
        SpannableString(reLearnTranslation.sourceText).apply {
            setSpan(
                StyleSpan(BOLD),
                0,
                reLearnTranslation.sourceText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

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

    private fun getPendingIntentForAction(
        reLearnTranslation: ReLearnTranslation,
        actionType: String,
        requestCode: Int
    ): PendingIntent {
        val launchIntent = Intent(context, ReLearnNotificationActionsReceiver::class.java)
            .apply {
                putExtra(
                    ReLearnNotificationActionsReceiver.EXTRA_ACTION_TYPE,
                    actionType
                )
                reLearnTranslation.source.putInIntent(this)
            }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getLaunchPendingIntent(reLearnTranslation: ReLearnTranslation) =
        getPendingIntentForAction(
            reLearnTranslation,
            ReLearnNotificationActionsReceiver.TYPE_LAUNCH,
            RELEARN_LAUNCH
        )

    private fun getAnotherPendingIntent(reLearnTranslation: ReLearnTranslation) =
        getPendingIntentForAction(
            reLearnTranslation,
            ReLearnNotificationActionsReceiver.TYPE_ANOTHER,
            RELEARN_ANOTHER
        )

    fun create(reLearnTranslation: ReLearnTranslation): Notification {
        val pendingLaunchIntent = getLaunchPendingIntent(reLearnTranslation)
        val pendingAnotherIntent = getAnotherPendingIntent(reLearnTranslation)

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_logo_standalone)
            .setContentTitle(getTitle(reLearnTranslation))
            .setContentText(reLearnTranslation.translations.first())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        reLearnTranslationFormatter.formatTranslationTextForNotification(
                            reLearnTranslation
                        )
                    )
                    .setSummaryText(getSummaryText(reLearnTranslation))
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_notification_refresh_24dp,
                    context.resources.getString(R.string.action_notification_another),
                    pendingAnotherIntent
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingLaunchIntent)

        return builder.build();
    }
}