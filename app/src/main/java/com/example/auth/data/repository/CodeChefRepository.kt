package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONObject

/**
 * Repository for fetching CodeChef user stats
 * Uses unofficial API or HTML scraping
 */
class CodeChefRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Try unofficial API first
            try {
                val apiUrl = "https://codechef-api.vercel.app/user/$username"
                val response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                
                val jsonResponse = response.body()
                val json = JSONObject(jsonResponse)
                
                if (json.has("rating")) {
                    val rating = json.optInt("rating", -1)
                    val globalRank = json.optString("globalRank", "N/A")
                    val countryRank = json.optString("countryRank", "N/A")
                    val stars = json.optString("stars", "N/A")
                    val fullySolved = json.optString("fullySolved", "0")
                    val profileImageUrl = json.optString("profileImage", null)
                    
                    val ratingStr = if (rating > 0) rating.toString() else "Unrated"
                    
                    return@withContext PlatformStats(
                        rating = ratingStr,
                        statsLabel = stars.ifEmpty { "Rating" },
                        additionalInfo = mapOf(
                            "Global Rank" to globalRank,
                            "Country Rank" to countryRank,
                            "Problems Solved" to fullySolved
                        ),
                        profileImageUrl = profileImageUrl
                    )
                }
            } catch (e: Exception) {
                Log.d("CodeChefRepository", "API failed, trying HTML scraping: ${e.message}")
            }
            
            // Fallback: HTML scraping
            val profileUrl = "https://www.codechef.com/users/$username"
            val doc = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            
            // Try to find rating
            val ratingElement = doc.selectFirst("div.rating-number, span.rating")
            val rating = ratingElement?.text()?.replace(Regex("[^0-9]"), "") ?: "N/A"
            
            // Try to find stars
            val starsElement = doc.selectFirst("span.rating")
            val stars = starsElement?.text()?.takeIf { it.contains("★") } ?: ""
            
            // Try to find profile image
            var profileImageUrl: String? = null
            try {
                val imgElement = doc.selectFirst("img[class*='user'], img[alt*='user'], img[src*='avatar']")
                profileImageUrl = imgElement?.attr("src")
                if (profileImageUrl != null && !profileImageUrl.startsWith("http")) {
                    profileImageUrl = "https://www.codechef.com$profileImageUrl"
                }
            } catch (e: Exception) {
                // Ignore image fetch errors
            }
            
            PlatformStats(
                rating = rating,
                statsLabel = if (stars.isNotEmpty()) stars else "Rating",
                additionalInfo = emptyMap(),
                profileImageUrl = profileImageUrl
            )
            
        } catch (e: Exception) {
            Log.e("CodeChefRepository", "Error fetching CodeChef stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

