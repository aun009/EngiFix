package com.example.auth.presentation.features.contest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class ContestDateTimeFormatterTest {
    @Test
    fun parseToCalendar_acceptsFormattedTwelveHourClistTime() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 4, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val parsed = ContestDateTimeFormatter.parseToCalendar("06.06 Sat 8:30 PM", now)

        assertNotNull(parsed)
        assertEquals(2026, parsed!!.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, parsed.get(Calendar.MONTH))
        assertEquals(6, parsed.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, parsed.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, parsed.get(Calendar.MINUTE))
    }

    @Test
    fun formatDuration_acceptsSecondsAndWords() {
        assertEquals("1h 30m", ContestDateTimeFormatter.formatDuration("5400"))
        assertEquals("2h 15m", ContestDateTimeFormatter.formatDuration("2 hours 15 minutes"))
    }
}
