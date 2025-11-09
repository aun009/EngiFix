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
                        
                        // Determine the year: if parsed date (with current year) is in the past, use next year
                        tempCal.set(Calendar.YEAR, currentYear)
                        val testDate = tempCal.time
                        
                        // If the test date is before now, it's next year
                        val year = if (testDate.before(now.time)) {
                            currentYear + 1
                        } else {
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
        val contestCal = parseContestDate(contest.start)
        if (contestCal == null) {
            parseErrors++
            println("❌ Failed to parse date for: ${contest.event} - ${contest.start}")
            return@forEach
        }
        
        val contestYear = contestCal.get(Calendar.YEAR)
        val contestMonth = contestCal.get(Calendar.MONTH)
        val contestDay = contestCal.get(Calendar.DAY_OF_MONTH)
        
        val isToday = today.get(Calendar.YEAR) == contestYear &&
                today.get(Calendar.MONTH) == contestMonth &&
                today.get(Calendar.DAY_OF_MONTH) == contestDay
        
        val isTomorrow = tomorrow.get(Calendar.YEAR) == contestYear &&
                tomorrow.get(Calendar.MONTH) == contestMonth &&
                tomorrow.get(Calendar.DAY_OF_MONTH) == contestDay
        
        when {
            isToday -> {
                todayContests.add(contest)
                todayMatches++
                println("✅ TODAY: ${contest.event} - ${contest.start} -> ${contestYear}-${contestMonth+1}-${contestDay}")
            }
            isTomorrow -> {
                tomorrowContests.add(contest)
                tomorrowMatches++
                println("✅ TOMORROW: ${contest.event} - ${contest.start} -> ${contestYear}-${contestMonth+1}-${contestDay}")
            }
            else -> {
                skipped++
                println("⏭️ SKIPPED: ${contest.event} - Date: ${contestYear}-${contestMonth+1}-${contestDay}, Start: ${contest.start}")
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
