package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONObject

/**
 * Repository for fetching HackerRank user stats
 * Uses HTML scraping (no public API)
 */
class HackerRankRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Try unofficial API first
            try {
                val apiUrl = "https://www.hackerrank.com/rest/hackers/$username/profile"
                val response = Jsoup.connect(apiUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                
                val jsonResponse = response.body()
                val json = JSONObject(jsonResponse)
                
                if (json.has("model")) {
                    val model = json.getJSONObject("model")
                    val score = model.optInt("score", -1)
                    val badges = model.optInt("badges", 0)
                    val avatar = model.optString("avatar", null)
                    
                    val scoreStr = if (score > 0) score.toString() else "0"
                    
                    return@withContext PlatformStats(
                        rating = scoreStr,
                        statsLabel = "Score",
                        additionalInfo = mapOf(
                            "Badges" to badges.toString()
                        ),
                        profileImageUrl = avatar
                    )
                }
            } catch (e: Exception) {
                Log.d("HackerRankRepository", "API failed, trying HTML scraping: ${e.message}")
            }
            
            // Fallback: HTML scraping
            val profileUrl = "https://www.hackerrank.com/$username"
            val doc = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            
            // Try to find score or badges
            val scoreElement = doc.selectFirst("div[class*='score'], span[class*='score']")
            val badgesElement = doc.selectFirst("svg.badge-icon")
            
            val score = scoreElement?.text()?.replace(Regex("[^0-9]"), "") ?: "0"
            val badgesCount = doc.select("svg.badge-icon").size.toString()
            
            // Try to find profile image
            var profileImageUrl: String? = null
            try {
                val imgElement = doc.selectFirst("img[class*='avatar'], img[class*='profile'], img[alt*='profile']")
                profileImageUrl = imgElement?.attr("src")
                if (profileImageUrl != null && !profileImageUrl.startsWith("http")) {
                    profileImageUrl = "https://www.hackerrank.com$profileImageUrl"
                }
            } catch (e: Exception) {
                // Ignore image fetch errors
            }
            
            PlatformStats(
                rating = score,
                statsLabel = "Score",
                additionalInfo = mapOf(
                    "Badges" to badgesCount
                ),
                profileImageUrl = profileImageUrl
            )
            
        } catch (e: Exception) {
            Log.e("HackerRankRepository", "Error fetching HackerRank stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

