package com.example.auth.presentation.features.contest

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

class ContestRepository @Inject constructor(private val api: ClistApi) {
    suspend fun getContestsSortedByPlatform(): Map<String, List<ContestItem>> {
        try {
            println("🔍 Fetching contests from API...")
            val result = api.getContests()
            println("✅ API Response received: ${result.objects.size} contests")
            println("📊 First few contests: ${result.objects.take(3).map { "${it.resource}: ${it.event}" }}")
            
            // Debug: Print date formats from API
            result.objects.take(5).forEach { contest ->
                println("📅 Date format check - Contest: ${contest.event}")
                println("   Start: '${contest.start}' (type: ${contest.start::class.simpleName})")
                println("   End: '${contest.end}'")
            }
            
            val filteredContests = result.objects.filter { contest -> 
                // Only include contests from our target platforms
                contest.resource in listOf("codeforces.com", "codechef.com", "atcoder.jp", "leetcode.com")
            }
            println("🎯 Filtered contests: ${filteredContests.size} from target platforms")
            
            val groupedContests = filteredContests.groupBy { contest ->
                // Map to friendly names
                when (contest.resource) {
                    "codeforces.com" -> "Codeforces"
                    "codechef.com" -> "CodeChef"
                    "atcoder.jp" -> "AtCoder"
                    "leetcode.com" -> "LeetCode"
                    else -> contest.resource
                }
            }
            
            val sortedContests = groupedContests.mapValues { (_, contests) ->
                // Sort contests by start time (earliest first)
                contests.sortedBy { it.start }
            }
            
            println("🎉 Final result: ${sortedContests.keys} with ${sortedContests.values.sumOf { it.size }} contests")
            return sortedContests
            
        } catch (e: Exception) {
            println("❌ API Error: ${e.message}")
            e.printStackTrace()
            println("🔄 Falling back to mock data...")
            // Return mock data for testing
            return getMockContests()
        }
    }
    

    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
    
    private fun getMockContests(): Map<String, List<ContestItem>> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Get tomorrow
        val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrow = calTomorrow.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(today)
        val tomorrowStr = dateFormat.format(tomorrow)
        
        return mapOf(
            "Codeforces" to listOf(
                ContestItem(
                    id = 1,
                    event = "Codeforces Round #940 (Div. 2)",
                    start = "${todayStr}T14:35:00", // 14:35 UTC = 8:05 PM IST
                    end = "${todayStr}T16:35:00",   // 16:35 UTC = 10:05 PM IST
                    duration = "2 hours",
                    resource = "codeforces.com",
                    resource_id = 1,
                    host = "codeforces.com",
                    href = "https://codeforces.com/contests"
                )
            ),
            "CodeChef" to listOf(
                ContestItem(
                    id = 2,
                    event = "CodeChef Starters 130 (Div. 3)",
                    start = "${todayStr}T12:30:00", // 12:30 UTC = 6:00 PM IST
                    end = "${todayStr}T15:00:00",   // 15:00 UTC = 8:30 PM IST
                    duration = "2 hours 30 minutes",
                    resource = "codechef.com",
                    resource_id = 2,
                    host = "codechef.com",
                    href = "https://www.codechef.com/contests"
                )
            ),
            "LeetCode" to listOf(
                ContestItem(
                    id = 3,
                    event = "Weekly Contest 395",
                    start = "${tomorrowStr}T02:30:00", // 02:30 UTC = 8:00 AM IST
                    end = "${tomorrowStr}T04:00:00",   // 04:00 UTC = 9:30 AM IST
                    duration = "1 hour 30 minutes",
                    resource = "leetcode.com",
                    resource_id = 102,
                    host = "leetcode.com",
                    href = "https://leetcode.com/contest"
                )
            ),
            "AtCoder" to listOf(
                ContestItem(
                    id = 4,
                    event = "AtCoder Beginner Contest 350",
                    start = "${tomorrowStr}T12:00:00", // 12:00 UTC = 5:30 PM IST
                    end = "${tomorrowStr}T13:40:00",   // 13:40 UTC = 7:10 PM IST
                    duration = "1 hour 40 minutes",
                    resource = "atcoder.jp",
                    resource_id = 3,
                    host = "atcoder.jp",
                    href = "https://atcoder.jp/contests"
                )
            )
        )
    }
}
