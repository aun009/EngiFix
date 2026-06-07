package com.example.auth.presentation.features.contest

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ContestDateTimeFormatter {
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    fun parseToCalendar(dateString: String, now: Calendar = Calendar.getInstance()): Calendar? {
        val cleanDate = normalizeMeridiem(dateString.trim())
        if (cleanDate.isBlank()) return null

        cleanDate.toLongOrNull()?.let { timestamp ->
            val date = if (timestamp > 1_000_000_000_000L) Date(timestamp) else Date(timestamp * 1000L)
            return Calendar.getInstance().apply { time = date }
        }

        datePatterns().forEach { datePattern ->
            val parsed = runCatching {
                val parser = SimpleDateFormat(datePattern.pattern, Locale.ENGLISH).apply {
                    isLenient = false
                    datePattern.timeZone?.let { timeZone = it }
                }
                val position = ParsePosition(0)
                val date = parser.parse(cleanDate, position)
                date.takeIf { position.index == cleanDate.length }
            }.getOrNull() ?: return@forEach

            return calendarFromParsedDate(parsed, datePattern.hasYear, now)
        }

        return null
    }

    fun parseMillis(dateString: String): Long? = parseToCalendar(dateString)?.timeInMillis

    fun formatCardTime(calendar: Calendar): String {
        return SimpleDateFormat("h:mm a", Locale.US).format(calendar.time)
    }

    fun formatDetailDateTime(calendar: Calendar): String {
        return SimpleDateFormat("MMM d, yyyy\nh:mm a", Locale.US).format(calendar.time)
    }

    fun formatDuration(duration: String): String {
        val cleanDuration = duration.trim()
        if (cleanDuration.isBlank()) return "TBD"

        cleanDuration.toLongOrNull()?.let { seconds ->
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${seconds}s"
            }
        }

        return cleanDuration
            .replace(" hours", "h")
            .replace(" hour", "h")
            .replace(" minutes", "m")
            .replace(" minute", "m")
    }

    private fun calendarFromParsedDate(parsed: Date, hasYear: Boolean, now: Calendar): Calendar {
        val calendar = Calendar.getInstance().apply { time = parsed }
        if (!hasYear) {
            calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))

            val staleThreshold = now.clone() as Calendar
            staleThreshold.add(Calendar.DAY_OF_YEAR, -1)
            if (calendar.before(staleThreshold)) {
                calendar.add(Calendar.YEAR, 1)
            }
        }
        return calendar
    }

    private fun normalizeMeridiem(value: String): String {
        return value.replace(Regex("\\s+"), " ")
            .replace(Regex("(?i)\\b(am|pm)\\b")) { it.value.uppercase(Locale.US) }
    }

    private fun datePatterns(): List<DatePattern> {
        return listOf(
            DatePattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm:ssXXX", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mmXXX", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd HH:mm:ssXXX", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd HH:mmXXX", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm:ss'Z'", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm'Z'", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm:ss.SSS", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm:ss", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'HH:mm", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd HH:mm:ss", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd HH:mm", hasYear = true, timeZone = utc),
            DatePattern("yyyy-MM-dd'T'h:mm a", hasYear = true),
            DatePattern("yyyy-MM-dd h:mm a", hasYear = true),
            DatePattern("dd.MM EEE HH:mm", hasYear = false),
            DatePattern("d.MM EEE HH:mm", hasYear = false),
            DatePattern("dd.M EEE HH:mm", hasYear = false),
            DatePattern("d.M EEE HH:mm", hasYear = false),
            DatePattern("dd.MM EEE h:mm a", hasYear = false),
            DatePattern("d.MM EEE h:mm a", hasYear = false),
            DatePattern("dd.M EEE h:mm a", hasYear = false),
            DatePattern("d.M EEE h:mm a", hasYear = false),
            DatePattern("dd.MM HH:mm", hasYear = false),
            DatePattern("d.MM HH:mm", hasYear = false),
            DatePattern("dd.M HH:mm", hasYear = false),
            DatePattern("d.M HH:mm", hasYear = false),
            DatePattern("dd.MM h:mm a", hasYear = false),
            DatePattern("d.MM h:mm a", hasYear = false),
            DatePattern("dd.M h:mm a", hasYear = false),
            DatePattern("d.M h:mm a", hasYear = false)
        )
    }

    private data class DatePattern(
        val pattern: String,
        val hasYear: Boolean,
        val timeZone: TimeZone? = null
    )
}
