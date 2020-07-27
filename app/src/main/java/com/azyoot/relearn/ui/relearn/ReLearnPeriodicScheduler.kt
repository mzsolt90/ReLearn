package com.azyoot.relearn.ui.relearn

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.azyoot.relearn.service.receiver.ReLearnScheduleReceiver
import com.azyoot.relearn.ui.common.RELEARN_SCHEDULE
import com.azyoot.relearn.util.DateTimeMapper
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReLearnPeriodicScheduler @Inject constructor(
    val context: Context,
    val dateTimeMapper: DateTimeMapper
) {
    fun schedule() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReLearnScheduleReceiver::class.java).apply {
            action = ReLearnScheduleReceiver.ACTION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RELEARN_SCHEDULE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val now = ZonedDateTime.now()
        val scheduledTime = now
            .withHour(SCHEDULE_AT_HOUR)
            .withMinute(SCHEDULE_AT_MINUTE)
            .let {
                if (now.isAfter(it)) {
                    it.plusDays(1)
                } else it
            }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            dateTimeMapper.mapToTimestamp(scheduledTime),
            TimeUnit.DAYS.toMillis(1),
            pendingIntent
        )
    }

    companion object {
        const val SCHEDULE_AT_HOUR = 20
        const val SCHEDULE_AT_MINUTE = 30
    }
}