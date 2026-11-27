package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.time.ZonedDateTime

private const val REMINDER_PREFIX = "studyReminder:"
private const val CHANNEL = "study_reminders"

internal class StudyReminders(private val context: Context) {
    private val prefs = context.getSharedPreferences("study_mentor", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun time(owner: String): Int? = prefs.getInt(REMINDER_PREFIX + owner, -1).takeIf { it in 0..1439 }

    fun save(owner: String, minuteOfDay: Int) {
        prefs.edit().putInt(REMINDER_PREFIX + owner, minuteOfDay).apply()
        schedule(owner)
    }

    fun remove(owner: String) {
        prefs.edit().remove(REMINDER_PREFIX + owner).apply()
        alarms.cancel(alarmIntent(owner))
        context.getSystemService(NotificationManager::class.java).cancel(owner, 1)
    }

    fun scheduleAll() {
        prefs.all.keys.filter { it.startsWith(REMINDER_PREFIX) }
            .forEach { schedule(it.removePrefix(REMINDER_PREFIX)) }
    }

    fun schedule(owner: String, afterDelivery: Boolean = false) {
        val minuteOfDay = time(owner) ?: return
        val now = ZonedDateTime.now()
        var next = now.toLocalDate().atTime(minuteOfDay / 60, minuteOfDay % 60).atZone(now.zone)
        if (afterDelivery || !next.isAfter(now)) {
            next = now.toLocalDate().plusDays(1).atTime(minuteOfDay / 60, minuteOfDay % 60).atZone(now.zone)
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), alarmIntent(owner))
    }

    private fun alarmIntent(owner: String): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, StudyReminderReceiver::class.java).apply {
            data = Uri.Builder().scheme("studyai").authority("reminder").appendPath(owner).build()
        }, PendingIntent.FLAG_IMMUTABLE
    )
}

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminders = StudyReminders(context)
        if (intent.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) {
            reminders.scheduleAll()
            return
        }
        val owner = intent.data?.lastPathSegment ?: return
        if (reminders.time(owner) == null) return
        reminders.schedule(owner, afterDelivery = true)
        val notifications = context.getSystemService(NotificationManager::class.java)
        if (!notifications.areNotificationsEnabled()) return
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL, "Nhắc lịch học", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Đến giờ học rồi")
            .setContentText("Mở StudyAI Mentor để ôn lại bài học của bạn.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notifications.notify(owner, 1, notification)
    }
}
