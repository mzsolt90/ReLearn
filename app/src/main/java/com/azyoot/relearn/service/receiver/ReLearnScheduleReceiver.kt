package com.azyoot.relearn.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.azyoot.relearn.service.worker.ReLearnWorker
import timber.log.Timber

class ReLearnScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action != ACTION) return

        Timber.d("Scheduling ReLearn worker")

        ReLearnWorker.run(context)
    }

    companion object {
        const val ACTION = "run_relearn_scheduled"
    }
}