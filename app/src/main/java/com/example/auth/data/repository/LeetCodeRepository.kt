package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.json.JSONObject

/**
 * Repository for fetching LeetCode user stats
 * Uses HTML scraping (LeetCode GraphQL requires authentication)
 */
class LeetCodeRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Try using a public LeetCode stats API first
            try {
                val apiUrl = "https://leetcode-stats-api.herokuapp.com/$username"
                val response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                
                val jsonResponse = response.body()
                val json = JSONObject(jsonResponse)
                
                if (json.has("status") && json.getString("status") == "success") {
                    val ranking = json.optInt("ranking", -1)
                    val totalSolved = json.optInt("totalSolved", 0)
                    val easySolved = json.optInt("easySolved", 0)
                    val mediumSolved = json.optInt("mediumSolved", 0)
                    val hardSolved = json.optInt("hardSolved", 0)
                    
                    // Show problems solved as main stat
                    val problemsSolvedStr = totalSolved.toString()
                    
                    // Try to get profile image from LeetCode profile page
                    var profileImageUrl: String? = null
                    try {
                        val profileUrl = "https://leetcode.com/$username/"
                        val doc = Jsoup.connect(profileUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(5000)
                            .get()
                        val imgElement = doc.selectFirst("img[alt*='avatar'], img[class*='avatar'], img[src*='avatar']")
                        profileImageUrl = imgElement?.attr("src")
                        if (profileImageUrl != null && !profileImageUrl.startsWith("http")) {
                            profileImageUrl = "https://leetcode.com$profileImageUrl"
                        }
                    } catch (e: Exception) {
                        // Ignore image fetch errors
                    }
                    
                    return@withContext PlatformStats(
                        rating = problemsSolvedStr,
                        statsLabel = "Problems Solved",
                        additionalInfo = mapOf(
                            "Ranking" to (if (ranking > 0) ranking.toString() else "N/A"),
                            "Easy" to easySolved.toString(),
                            "Medium" to mediumSolved.toString(),
                            "Hard" to hardSolved.toString()
                        ),
                        profileImageUrl = profileImageUrl
                    )
                }
            } catch (e: Exception) {
                Log.d("LeetCodeRepository", "API failed, trying HTML scraping: ${e.message}")
            }
            
            // Fallback: HTML scraping from profile page
            val profileUrl = "https://leetcode.com/$username/"
            val doc: Document = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            // Try to find contest rating or ranking
            var rating = "N/A"
            var label = "Rating"
            
            // Look for contest rating
            val contestRatingElement = doc.selectFirst("div:contains(Contest Rating), span:contains(Contest Rating)")
            if (contestRatingElement != null) {
                val parent = contestRatingElement.parent()
                val ratingText = parent?.text() ?: ""
                val ratingMatch = Regex("(\\d+)").find(ratingText)
                rating = ratingMatch?.value ?: "N/A"
                label = "Contest Rating"
            }
            
            // If no contest rating, try to find ranking
            if (rating == "N/A") {
                val rankingElements = doc.select("div, span")
                    .filter { it.text().contains("Ranking", ignoreCase = true) || 
                             it.text().contains("Rank", ignoreCase = true) }
                
                for (element in rankingElements) {
                    val text = element.text()
                    val numberMatch = Regex("(\\d{1,3}(?:,\\d{3})*)").find(text)
                    if (numberMatch != null) {
                        rating = numberMatch.value.replace(",", "")
                        label = "Ranking"
                        break
                    }
                }
            }
            
            // Prioritize finding problems solved
            var problemsSolved = "N/A"
            val solvedElements = doc.select("div, span")
                .filter { it.text().contains("Solved", ignoreCase = true) || 
                         it.text().contains("problem", ignoreCase = true) }
            
            for (element in solvedElements) {
                val text = element.text()
                // Look for patterns like "123 Solved" or "123 problems"
                val numberMatch = Regex("(\\d+)\\s*(?:Solved|problems?)", RegexOption.IGNORE_CASE).find(text)
                if (numberMatch != null) {
                    problemsSolved = numberMatch.groupValues[1]
                    break
                }
                // Also try just finding a number near "Solved"
                val simpleMatch = Regex("(\\d+)").find(text)
                if (simpleMatch != null && text.contains("Solved", ignoreCase = true)) {
                    problemsSolved = simpleMatch.value
                    break
                }
            }
            
            // Try to get profile image
            var profileImageUrl: String? = null
            try {
                val imgElement = doc.selectFirst("img[alt*='avatar'], img[class*='avatar'], img[src*='avatar']")
                profileImageUrl = imgElement?.attr("src")
                if (profileImageUrl != null && !profileImageUrl.startsWith("http")) {
                    profileImageUrl = "https://leetcode.com$profileImageUrl"
                }
            } catch (e: Exception) {
                // Ignore image fetch errors
            }
            
            // If we found problems solved, use that as main stat
            if (problemsSolved != "N/A") {
                return@withContext PlatformStats(
                    rating = problemsSolved,
                    statsLabel = "Problems Solved",
                    additionalInfo = mapOf(
                        "Ranking" to (if (rating != "N/A") rating else "N/A")
                    ),
                    profileImageUrl = profileImageUrl
                )
            }
            
            // Fallback to rating/ranking if problems solved not found
            PlatformStats(
                rating = rating,
                statsLabel = label,
                additionalInfo = emptyMap(),
                profileImageUrl = profileImageUrl
            )
            
        } catch (e: Exception) {
            Log.e("LeetCodeRepository", "Error fetching LeetCode stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

