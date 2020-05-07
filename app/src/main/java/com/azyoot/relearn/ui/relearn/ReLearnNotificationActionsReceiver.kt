package com.azyoot.relearn.ui.relearn

import android.content.*
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.service.worker.AcceptOrSuppressReLearnWorker
import com.azyoot.relearn.ui.notification.ID_RELEARN
import com.azyoot.relearn.util.ensureStartsWithHttpsScheme
import com.azyoot.relearn.util.isValidUrl
import timber.log.Timber

class ReLearnNotificationActionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE)
        if (actionType.isNullOrEmpty()) return

        val sourceId = intent.getLongExtra(EXTRA_SOURCE_ID, -1)
        if (sourceId < 0) return
        val sourceType = intent.getSerializableExtra(EXTRA_SOURCE_TYPE) as SourceType

        dismissNotification(context)

        when (actionType) {
            TYPE_LAUNCH -> {
                val url = intent.getStringExtra(EXTRA_LAUNCH_URL) ?: ""
                if (url.isEmpty().not()) {
                    launchUrl(context, url)
                }
                val text = intent.getStringExtra(EXTRA_TRANSLATE_TEXT) ?: ""
                if (text.isEmpty().not()) {
                    launchTranslation(context, text)
                }

                scheduleAccept(context, sourceId, sourceType)
            }
            TYPE_ACCEPT -> scheduleAccept(context, sourceId, sourceType)
            TYPE_SUPPRESS -> scheduleSuppress(context, sourceId, sourceType)
        }
    }

    private fun dismissNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_RELEARN)
    }

    private fun scheduleAccept(context: Context, sourceId: Long, sourceType: SourceType) {
        AcceptOrSuppressReLearnWorker.schedule(context, sourceId, sourceType, isAccept = true)
    }

    private fun scheduleSuppress(context: Context, sourceId: Long, sourceType: SourceType) {
        AcceptOrSuppressReLearnWorker.schedule(context, sourceId, sourceType, isSuppress = true)
    }

    private fun launchUrl(context: Context, url: String) {
        if (url.ensureStartsWithHttpsScheme().isValidUrl().not()) {
            Timber.w("Invalid url $url")
            return
        }

        Timber.d("Launching url for relearn $url")

        val intent = Intent(context, ReLearnLaunchUrlActivity::class.java).apply {
            putExtra(ReLearnLaunchUrlActivity.EXTRA_URL, url.ensureStartsWithHttpsScheme())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun launchTranslation(context: Context, text: String) {
        if (Build.VERSION.SDK_INT >= 29) {
            val intent = Intent(Intent.ACTION_TRANSLATE).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
                return
            } catch (ex: ActivityNotFoundException) {
                Timber.w(ex, "No activity found to handle ACTION_TRANSLATE")
            }
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            component = ComponentName(
                "com.google.android.apps.translate",
                "com.google.android.apps.translate.TranslateActivity"
            )
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra("key_text_input", text)
            putExtra("key_suggest_translation", "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(sendIntent)
            return
        } catch (ex: ActivityNotFoundException) {
            Timber.w(ex, "No activity found to handle ACTION_SEND")
        }
    }

    companion object {
        const val EXTRA_ACTION_TYPE = "action_type"
        const val TYPE_LAUNCH = "launch"
        const val TYPE_ACCEPT = "accept"
        const val TYPE_SUPPRESS = "suppress"

        const val EXTRA_LAUNCH_URL = "launch_url"
        const val EXTRA_TRANSLATE_TEXT = "translate_text"
        const val EXTRA_SOURCE_ID = "source_id"
        const val EXTRA_SOURCE_TYPE = "source_type"
    }
}