package com.example.auth.presentation.features.contest

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ContestReminderPolicy {
    const val REMINDER_MINUTES = 25L
    private val reminderLeadMillis = TimeUnit.MINUTES.toMillis(REMINDER_MINUTES)

    fun parseStartMillis(start: String): Long? {
        return ContestDateTimeFormatter.parseMillis(start)
    }

    fun reminderTimeMillis(startMillis: Long): Long = startMillis - reminderLeadMillis

    fun shouldNotifyNow(startMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val remainingMillis = startMillis - nowMillis
        return remainingMillis in 1..reminderLeadMillis
    }

    fun scheduledKey(contest: ContestItem, startMillis: Long): String {
        return "contest_reminder_scheduled_v2_${REMINDER_MINUTES}_${contest.id}_$startMillis"
    }

    fun notifiedKey(contest: ContestItem, startMillis: Long?): String {
        val timeIdentity = startMillis?.toString() ?: contest.start.ifBlank { contest.event }
        return "contest_reminder_notified_v2_${REMINDER_MINUTES}_${contest.id}_$timeIdentity"
    }

    fun cleanContestName(event: String): String {
        return event.replace("\\s+".toRegex(), " ").trim().ifBlank { "Coding contest" }
    }

    fun notificationTitle(startMillis: Long?): String {
        val remainingMinutes = startMillis
            ?.let { ((it - System.currentTimeMillis()) / TimeUnit.MINUTES.toMillis(1)).coerceIn(1L, REMINDER_MINUTES) }
            ?: REMINDER_MINUTES

        return "Contest in $remainingMinutes min"
    }

    fun formatStartTime(startMillis: Long?): String {
        if (startMillis == null) return "Starting soon"

        val contest = Calendar.getInstance().apply { time = Date(startMillis) }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val time = SimpleDateFormat("h:mm a", Locale.US).format(Date(startMillis))

        return when {
            contest.isSameDay(today) -> "Today, $time"
            contest.isSameDay(tomorrow) -> "Tomorrow, $time"
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(startMillis))
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}
