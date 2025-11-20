package com.example.myapplication.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.data.CalendarEvent

object ReminderScheduler {

    fun schedule(context: Context, event: CalendarEvent) {
        val minutesBefore = event.reminderMinutesBefore ?: return
        val triggerAt = event.startEpochMillis - minutesBefore * 60_000L
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }
        val pendingIntent = reminderPendingIntent(context, event)

        val alarmType = AlarmManager.RTC_WAKEUP
        alarmManager.setExactAndAllowWhileIdle(alarmType, triggerAt, pendingIntent)
    }

    fun cancel(context: Context, event: CalendarEvent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = reminderPendingIntent(context, event)
        alarmManager.cancel(pendingIntent)
    }

    private fun reminderPendingIntent(context: Context, event: CalendarEvent): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, event.title)
            val message = if (event.description.isNullOrBlank()) {
                "${event.title} 即将开始"
            } else {
                event.description
            }
            putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, event.id.toInt(), intent, flags)
    }
}


