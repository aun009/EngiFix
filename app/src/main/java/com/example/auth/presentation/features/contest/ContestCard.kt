package com.example.auth.presentation.features.contest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

@Composable
fun ContestCard(
    contest: ContestItem,
    platformColor: Color,
    platformName: String,
    onClick: () -> Unit = {}
) {
    // Check if contest is currently running and calculate remaining time
    val isRunning = remember(contest.start, contest.end) {
        val startCal = parseContestDate(contest.start)
        val endCal = parseContestDate(contest.end)
        
        if (startCal != null && endCal != null) {
            val now = Calendar.getInstance()
            val startTime = startCal.time
            val endTime = endCal.time
            val currentTime = now.time
            !startTime.after(currentTime) && endTime.after(currentTime)
        } else {
            false
        }
    }
    
    val remainingTime = remember(contest.end, isRunning) {
        if (isRunning) {
            val endCal = parseContestDate(contest.end)
            if (endCal != null) {
                val now = Calendar.getInstance()
                val endTimeMillis = endCal.timeInMillis
                val currentTimeMillis = now.timeInMillis
                val diff = endTimeMillis - currentTimeMillis
                
                if (diff > 0) {
                    val hours = (diff / (1000 * 60 * 60)).toInt()
                    val minutes = ((diff % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                    
                    when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h"
                        minutes > 0 -> "${minutes}m"
                        else -> "Ending soon"
                    }
                } else {
                    "Ending soon"
                }
            } else {
                null
            }
        } else {
            null
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Platform header with color
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = platformName,
                    style = MaterialTheme.typography.labelLarge,
                    color = platformColor,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Show "Running" badge or remaining time if contest is running
                if (isRunning && remainingTime != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50) // Green for running
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Time left: $remainingTime",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = platformColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = formatDuration(contest.duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = platformColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contest title
            Text(
                text = contest.event,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Time information with better visual hierarchy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Starts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDateTime(contest.start),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ends",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatDateTime(contest.end),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Click hint
            Text(
                text = "Tap to view details",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PlatformContestSection(
    platformName: String,
    contests: List<ContestItem>,
    platformColor: Color,
    onContestClick: (ContestItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Platform header with enhanced design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = platformColor.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(platformColor)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = platformName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = platformColor
                    )
                    Text(
                        text = "${contests.size} contest${if (contests.size != 1) "s" else ""} available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = platformColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = contests.size.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Contest cards
        contests.forEach { contest ->
            ContestCard(
                contest = contest,
                platformColor = platformColor,
                platformName = platformName,
                onClick = { onContestClick(contest) }
            )
        }
    }
}

@Composable
fun DateSectionCard(
    title: String,
    contestCount: Int,
    sectionColor: Color,
    iconColor: Color,
    onContestClick: (ContestItem) -> Unit,
    contests: List<ContestItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Date section header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = sectionColor.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (title == "Today") Icons.Filled.Favorite else Icons.Filled.Warning,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$contestCount contest${if (contestCount != 1) "s" else ""} available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Badge button
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = iconColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Contest cards grouped by platform
        val contestsByPlatform = contests.groupBy { contest ->
            when (contest.resource) {
                "codeforces.com" -> "Codeforces"
                "codechef.com" -> "CodeChef"
                "atcoder.jp" -> "AtCoder"
                "leetcode.com" -> "LeetCode"
                else -> contest.resource
            }
        }
        
        contestsByPlatform.forEach { (platform, platformContests) ->
            platformContests.forEach { contest ->
                ContestCard(
                    contest = contest,
                    platformColor = PlatformColors.getColorForPlatform(platform),
                    platformName = platform,
                    onClick = { onContestClick(contest) }
                )
            }
        }
    }
}

// Date utility functions
private fun parseContestDate(dateString: String): Calendar? {
    return try {
        // First, check if it's a numeric timestamp (in case format_time=false)
        val timestamp = dateString.toLongOrNull()
        if (timestamp != null) {
            // It's a Unix timestamp (seconds), convert to milliseconds
            val date = if (timestamp > 1000000000000L) {
                // Already in milliseconds
                Date(timestamp)
            } else {
                // In seconds, convert to milliseconds
                Date(timestamp * 1000)
            }
            val localCal = Calendar.getInstance()
            localCal.time = date
            println("✅ Parsed timestamp: $dateString -> ${localCal.get(Calendar.YEAR)}-${localCal.get(Calendar.MONTH)+1}-${localCal.get(Calendar.DAY_OF_MONTH)}")
            return localCal
        }
        
        // Try multiple date formats that Clist API might return with format_time=true
        // Clist API with format_time=true returns: "10.11 Mon 20:35" (DD.MM EEE HH:mm)
        val formats = listOf(
            // Clist API formatted time format (MOST COMMON with format_time=true)
            "dd.MM EEE HH:mm",  // e.g., "10.11 Mon 20:35"
            "d.MM EEE HH:mm",   // e.g., "9.11 Sun 17:30" (single digit day)
            "dd.M EEE HH:mm",   // e.g., "10.1 Mon 20:35" (single digit month)
            "d.M EEE HH:mm",    // e.g., "9.1 Sun 17:30" (single digit both)
            // ISO formats with T separator
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            // Space-separated formats
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss.SSS'Z'",
            // With timezone
            "yyyy-MM-dd HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss.SSSXXX"
        )
        
        var parsedDate: Date? = null
        var usedFormat: String? = null
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH) // Use ENGLISH for day names (Mon, Tue, etc.)
                // For Clist formatted dates (dd.MM EEE HH:mm), we need to handle the year
                // The API doesn't include year, so we assume current year or next year if month < current month
                if (format.contains("dd.MM") || format.contains("d.M")) {
                    // This is a Clist formatted date without year: "10.11 Mon 20:35"
                    // Parse it and add the appropriate year
                    val parsed = sdf.parse(dateString)
                    if (parsed != null) {
                        val tempCal = Calendar.getInstance()
                        tempCal.time = parsed
                        
                        val now = Calendar.getInstance()
                        val currentYear = now.get(Calendar.YEAR)
                        val currentMonth = now.get(Calendar.MONTH) // 0-11
                        val currentDay = now.get(Calendar.DAY_OF_MONTH)
                        
                        val parsedMonth = tempCal.get(Calendar.MONTH) // 0-11
                        val parsedDay = tempCal.get(Calendar.DAY_OF_MONTH)
                        
                        // Determine the year: compare only date (month/day), not time
                        // If the parsed month/day is before today's month/day, assume next year
                        // Otherwise, assume current year
                        val year = if (parsedMonth < currentMonth || 
                                      (parsedMonth == currentMonth && parsedDay < currentDay)) {
                            // The date has already passed this year, so it must be next year
                            currentYear + 1
                        } else {
                            // The date is today or in the future this year
                            currentYear
                        }
                        
                        tempCal.set(Calendar.YEAR, year)
                        parsedDate = tempCal.time
                        usedFormat = format
                        break
                    }
                } else {
                    // ISO format - Clist API returns times in UTC
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(dateString)
                    if (parsedDate != null) {
                        usedFormat = format
                        break
                    }
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        if (parsedDate == null) {
            println("❌ Could not parse date with any format: $dateString")
            return null
        }
        
        // The parsedDate represents the UTC time as milliseconds since epoch
        // Create a calendar in local timezone - it will automatically convert
        val localCal = Calendar.getInstance() // Local timezone
        localCal.timeInMillis = parsedDate.time // This correctly converts UTC to local
        
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = parsedDate.time
        
        println("✅ Parsed date '$dateString' with format '$usedFormat'")
        println("   UTC: ${utcCal.get(Calendar.YEAR)}-${utcCal.get(Calendar.MONTH)+1}-${utcCal.get(Calendar.DAY_OF_MONTH)} ${utcCal.get(Calendar.HOUR_OF_DAY)}:${utcCal.get(Calendar.MINUTE)}")
        println("   Local: ${localCal.get(Calendar.YEAR)}-${localCal.get(Calendar.MONTH)+1}-${localCal.get(Calendar.DAY_OF_MONTH)} ${localCal.get(Calendar.HOUR_OF_DAY)}:${localCal.get(Calendar.MINUTE)}")
        
        localCal
    } catch (e: Exception) {
        println("❌ Error parsing date: $dateString - ${e.message}")
        e.printStackTrace()
        null
    }
}

// These functions are kept for backward compatibility but the main filtering
// is now done directly in filterContestsTodayAndTomorrow for better debugging
fun isContestToday(contest: ContestItem): Boolean {
    val contestCal = parseContestDate(contest.start) ?: return false
    val today = Calendar.getInstance()
    
    // Compare only the date part (year, month, day) in local timezone
    return today.get(Calendar.YEAR) == contestCal.get(Calendar.YEAR) &&
            today.get(Calendar.MONTH) == contestCal.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) == contestCal.get(Calendar.DAY_OF_MONTH)
}

fun isContestTomorrow(contest: ContestItem): Boolean {
    val contestCal = parseContestDate(contest.start) ?: return false
    val tomorrow = Calendar.getInstance().apply { 
        add(Calendar.DAY_OF_YEAR, 1)
    }
    
    // Compare only the date part (year, month, day) in local timezone
    return tomorrow.get(Calendar.YEAR) == contestCal.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.MONTH) == contestCal.get(Calendar.MONTH) &&
            tomorrow.get(Calendar.DAY_OF_MONTH) == contestCal.get(Calendar.DAY_OF_MONTH)
}

fun filterContestsTodayAndTomorrow(contestsByPlatform: Map<String, List<ContestItem>>): Pair<List<ContestItem>, List<ContestItem>> {
    val todayContests = mutableListOf<ContestItem>()
    val tomorrowContests = mutableListOf<ContestItem>()
    
    val allContests = contestsByPlatform.values.flatten()
    println("🔍 Filtering ${allContests.size} total contests for today and tomorrow...")
    
    // Get current date for comparison
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    println("📅 Current date - Today: ${today.get(Calendar.YEAR)}-${today.get(Calendar.MONTH)+1}-${today.get(Calendar.DAY_OF_MONTH)}")
    println("📅 Current date - Tomorrow: ${tomorrow.get(Calendar.YEAR)}-${tomorrow.get(Calendar.MONTH)+1}-${tomorrow.get(Calendar.DAY_OF_MONTH)}")
    
    var parseErrors = 0
    var todayMatches = 0
    var tomorrowMatches = 0
    var skipped = 0
    
    allContests.forEach { contest ->
        // Special logging for AtCoder contests
        val isAtCoder = contest.resource == "atcoder.jp"
        
        val contestStartCal = parseContestDate(contest.start)
        val contestEndCal = parseContestDate(contest.end)
        
        if (contestStartCal == null) {
            parseErrors++
            println("❌ Failed to parse start date for: ${contest.event} - ${contest.start}")
            if (isAtCoder) {
                println("   ⚠️ ATCODER CONTEST FAILED TO PARSE!")
            }
            return@forEach
        }
        
        val startYear = contestStartCal.get(Calendar.YEAR)
        val startMonth = contestStartCal.get(Calendar.MONTH)
        val startDay = contestStartCal.get(Calendar.DAY_OF_MONTH)
        
        // Check if contest starts today
        val startsToday = today.get(Calendar.YEAR) == startYear &&
                today.get(Calendar.MONTH) == startMonth &&
                today.get(Calendar.DAY_OF_MONTH) == startDay
        
        // Check if contest starts tomorrow
        val startsTomorrow = tomorrow.get(Calendar.YEAR) == startYear &&
                tomorrow.get(Calendar.MONTH) == startMonth &&
                tomorrow.get(Calendar.DAY_OF_MONTH) == startDay
        
        // Special logging for AtCoder contests (before running check)
        if (isAtCoder) {
            println("🏃 ATCODER CONTEST CHECK: ${contest.event}")
            println("   Start string: '${contest.start}' -> Parsed: ${contestStartCal.time}")
            println("   End string: '${contest.end}' -> Parsed: ${contestEndCal?.time ?: "NULL"}")
            println("   Starts today? $startsToday")
        }
        
        // Check if contest is currently running (started before now but hasn't ended yet)
        var isCurrentlyRunning = false
        if (contestEndCal != null) {
            val now = Calendar.getInstance()
            val startTime = contestStartCal.time
            val endTime = contestEndCal.time
            val currentTime = now.time
            
            // Contest is running if: started before or at now AND ends after now
            val started = !startTime.after(currentTime)  // startTime <= currentTime
            val notEnded = endTime.after(currentTime)    // endTime > currentTime
            isCurrentlyRunning = started && notEnded
            
            // Special logging for AtCoder contests (detailed)
            if (isAtCoder) {
                println("   Current time: $currentTime")
                println("   Started? (startTime <= now): $started")
                println("   Not ended? (endTime > now): $notEnded")
                println("   Is running? $isCurrentlyRunning")
            }
            
            // Debug logging for contests that have started
            if (started) {
                println("🔍 Contest started: ${contest.event}")
                println("   Start time: ${startTime} (${contest.start})")
                println("   Current time: ${currentTime}")
                println("   End time: ${endTime} (${contest.end})")
                println("   Started check: $started, Not ended check: $notEnded, Is running: $isCurrentlyRunning")
            }
        } else {
            println("⚠️ No end date for: ${contest.event} - ${contest.end}")
            if (isAtCoder) {
                println("   ⚠️ ATCODER CONTEST HAS NO END DATE!")
            }
        }
        
        // Show contest in "Today" if:
        // 1. It starts today, OR
        // 2. It's currently running (regardless of when it started, as long as it's active now)
        when {
            startsToday || isCurrentlyRunning -> {
                todayContests.add(contest)
                todayMatches++
                val status = if (isCurrentlyRunning && !startsToday) "RUNNING" else if (isCurrentlyRunning && startsToday) "RUNNING (started today)" else "TODAY"
                println("✅ $status: ${contest.event} - Start: ${contest.start}, End: ${contest.end}")
                if (isAtCoder) {
                    println("   🎯 ATCODER CONTEST ADDED TO TODAY!")
                }
            }
            startsTomorrow -> {
                tomorrowContests.add(contest)
                tomorrowMatches++
                println("✅ TOMORROW: ${contest.event} - ${contest.start} -> ${startYear}-${startMonth+1}-${startDay}")
            }
            else -> {
                skipped++
                println("⏭️ SKIPPED: ${contest.event} - Date: ${startYear}-${startMonth+1}-${startDay}, Start: ${contest.start}")
                if (isAtCoder) {
                    println("   ⚠️ ATCODER CONTEST WAS SKIPPED!")
                    println("   Starts today? $startsToday, Is running? $isCurrentlyRunning, Starts tomorrow? $startsTomorrow")
                }
            }
        }
    }
    
    // Sort by start time
    todayContests.sortBy { it.start }
    tomorrowContests.sortBy { it.start }
    
    println("✅ Filtered results: ${todayContests.size} today, ${tomorrowContests.size} tomorrow")
    println("📊 Stats: Parse errors: $parseErrors, Today matches: $todayMatches, Tomorrow matches: $tomorrowMatches, Skipped: $skipped")
    
    return Pair(todayContests, tomorrowContests)
}

private fun formatDateTime(dateTimeString: String): String {
    val contestCal = parseContestDate(dateTimeString)
    return contestCal?.let {
        val dayOfWeek = when (it.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> ""
        }
        String.format("%02d.%02d %s %02d:%02d", 
            it.get(Calendar.DAY_OF_MONTH),
            it.get(Calendar.MONTH) + 1,
            dayOfWeek,
            it.get(Calendar.HOUR_OF_DAY),
            it.get(Calendar.MINUTE)
        )
    } ?: dateTimeString
}

private fun formatDuration(duration: String): String {
    return duration.replace(" hours", "h")
        .replace(" hour", "h")
        .replace(" minutes", "m")
        .replace(" minute", "m")
}
