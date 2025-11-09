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
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        
        val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = todayFormat.format(today)
        val tomorrowStr = todayFormat.format(tomorrow)
        
        return mapOf(
            "Codeforces" to listOf(
                ContestItem(
                    id = 1,
                    event = "Codeforces Round #123",
                    start = "${todayStr}T10:00:00",
                    end = "${todayStr}T12:00:00",
                    duration = "2 hours",
                    resource = "codeforces.com",
                    resource_id = 1,
                    host = "codeforces.com",
                    href = "https://codeforces.com/contests/123"
                )
            ),
            "LeetCode" to listOf(
                ContestItem(
                    id = 3,
                    event = "Weekly Contest 123",
                    start = "${tomorrowStr}T10:00:00",
                    end = "${tomorrowStr}T11:30:00",
                    duration = "1 hour 30 minutes",
                    resource = "leetcode.com",
                    resource_id = 102,
                    host = "leetcode.com",
                    href = "https://leetcode.com/contest/weekly-contest-123"
                )
            )
        )
    }
}
