package com.example.auth.presentation.features.contest

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "contest_notifications"
        const val CHANNEL_NAME = "Contest reminders"
        const val CHANNEL_DESCRIPTION = "Alerts 25 minutes before coding contests"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(false)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showContestReminder(
        contest: ContestItem,
        platformName: String,
        contestStartMillis: Long? = ContestReminderPolicy.parseStartMillis(contest.start)
    ): Boolean {
        val prefs = context.getSharedPreferences("contest_notifications", Context.MODE_PRIVATE)
        val notificationKey = ContestReminderPolicy.notifiedKey(contest, contestStartMillis)

        if (prefs.getBoolean(notificationKey, false)) {
            android.util.Log.d("NotificationHelper", "Reminder already sent for: ${contest.event}")
            return false
        }

        if (!canPostNotifications()) {
            android.util.Log.w("NotificationHelper", "Notifications are disabled or not permitted")
            return false
        }

        val eventName = ContestReminderPolicy.cleanContestName(contest.event)
        val startLabel = ContestReminderPolicy.formatStartTime(contestStartMillis)
        val contentIntent = buildContentPendingIntent(contest)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(ContestReminderPolicy.notificationTitle(contestStartMillis))
            .setContentText("$eventName • $platformName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$eventName\n$platformName • $startLabel")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setColor(0xFF5865F2.toInt())
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId(contest, contestStartMillis), notification)
            prefs.edit().putBoolean(notificationKey, true).apply()
            android.util.Log.d("NotificationHelper", "Contest reminder sent for: ${contest.event}")
            true
        } catch (securityException: SecurityException) {
            android.util.Log.w("NotificationHelper", "Missing notification permission", securityException)
            false
        }
    }

    fun checkAndNotifyUpcomingContests(contests: List<ContestItem>, platformName: String) {
        contests.forEach { contest ->
            val startMillis = ContestReminderPolicy.parseStartMillis(contest.start)
            if (startMillis == null) {
                android.util.Log.w("NotificationHelper", "Could not parse contest start: ${contest.start}")
                return@forEach
            }

            if (ContestReminderPolicy.shouldNotifyNow(startMillis)) {
                showContestReminder(contest, platformName, startMillis)
            }
        }
    }

    private fun buildContentPendingIntent(contest: ContestItem): PendingIntent {
        val intent = if (contest.href.isNotBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(contest.href))
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            contest.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationId(contest: ContestItem, startMillis: Long?): Int {
        return listOf(contest.id, startMillis ?: 0L, ContestReminderPolicy.REMINDER_MINUTES).hashCode()
    }
}
