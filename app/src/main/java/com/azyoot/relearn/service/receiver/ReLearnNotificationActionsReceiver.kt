package com.azyoot.relearn.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.service.common.ReLearnLauncher
import com.azyoot.relearn.service.worker.AcceptOrSuppressReLearnWorker
import com.azyoot.relearn.service.worker.ReLearnWorker
import com.azyoot.relearn.ui.main.MainActivity
import com.azyoot.relearn.ui.notification.ID_RELEARN
import javax.inject.Inject

class ReLearnNotificationActionsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reLearnLauncher: ReLearnLauncher

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as ReLearnApplication).appComponent.inject(this)

        val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE)
        if (actionType.isNullOrEmpty()) return

        val sourceId = intent.getLongExtra(EXTRA_SOURCE_ID, -1)
        if (sourceId < 0) return
        val sourceType = intent.getSerializableExtra(EXTRA_SOURCE_TYPE) as SourceType

        dismissNotification(context)

        when (actionType) {
            TYPE_LAUNCH -> {
                launchReLearn(context)
            }
            TYPE_ACCEPT -> scheduleAccept(context, sourceId, sourceType)
            TYPE_SUPPRESS -> scheduleSuppress(context, sourceId, sourceType)
            TYPE_ANOTHER -> scheduleAnother(context, sourceId, sourceType)
        }
    }

    private fun launchReLearn(context: Context){
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
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

    private fun scheduleAnother(context: Context, sourceId: Long, sourceType: SourceType) {
        WorkManager.getInstance(context).beginWith(
            AcceptOrSuppressReLearnWorker.getRequest(
                sourceId,
                sourceType,
                isAccept = true
            )
        )
            .then(OneTimeWorkRequest.from(ReLearnWorker::class.java))
            .enqueue()
    }


    companion object {
        const val EXTRA_ACTION_TYPE = "action_type"
        const val TYPE_LAUNCH = "launch"
        const val TYPE_ACCEPT = "accept"
        const val TYPE_SUPPRESS = "suppress"
        const val TYPE_ANOTHER = "another"

        const val EXTRA_LAUNCH_URL = "launch_url"
        const val EXTRA_TRANSLATE_TEXT = "translate_text"
        const val EXTRA_SOURCE_ID = "source_id"
        const val EXTRA_SOURCE_TYPE = "source_type"
    }
}