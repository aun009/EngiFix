package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONObject

/**
 * Repository for fetching AtCoder user stats
 * Uses k3lcoder API or HTML scraping
 */
class AtCoderRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Try k3lcoder API first
            try {
                val apiUrl = "https://kcoder.cool/atcoder-api/v1/user-info?user=$username"
                val response = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                
                val jsonResponse = response.body()
                val json = JSONObject(jsonResponse)
                
                if (json.has("rating")) {
                    val rating = json.optInt("rating", -1)
                    val highestRating = json.optInt("highestRating", -1)
                    val problemsSolved = json.optString("problemsSolved", "0")
                    
                    val ratingStr = if (rating > 0) rating.toString() else "Unrated"
                    
                    return@withContext PlatformStats(
                        rating = ratingStr,
                        statsLabel = "Rating",
                        additionalInfo = mapOf(
                            "Highest Rating" to (if (highestRating > 0) highestRating.toString() else "N/A"),
                            "Problems Solved" to problemsSolved
                        )
                    )
                }
            } catch (e: Exception) {
                Log.d("AtCoderRepository", "API failed, trying alternative: ${e.message}")
                
                // Try alternative API
                try {
                    val altApiUrl = "https://atcoder-api.herokuapp.com/results?user=$username"
                    val response = Jsoup.connect(altApiUrl)
                        .ignoreContentType(true)
                        .timeout(10000)
                        .execute()
                    
                    val jsonResponse = response.body()
                    // Parse response if needed
                } catch (e2: Exception) {
                    Log.d("AtCoderRepository", "Alternative API also failed: ${e2.message}")
                }
            }
            
            // Fallback: HTML scraping
            val profileUrl = "https://atcoder.jp/users/$username"
            val doc = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            
            // Try to find rating
            val ratingElement = doc.selectFirst("span[class*='rating'], td:contains(Rating)")
            val rating = ratingElement?.text()?.replace(Regex("[^0-9]"), "") ?: "N/A"
            
            // Try to find profile image
            var profileImageUrl: String? = null
            try {
                val imgElement = doc.selectFirst("img[class*='avatar'], img[class*='user'], img[alt*='user']")
                profileImageUrl = imgElement?.attr("src")
                if (profileImageUrl != null && !profileImageUrl.startsWith("http")) {
                    profileImageUrl = "https://atcoder.jp$profileImageUrl"
                }
            } catch (e: Exception) {
                // Ignore image fetch errors
            }
            
            PlatformStats(
                rating = rating,
                statsLabel = "Rating",
                additionalInfo = emptyMap(),
                profileImageUrl = profileImageUrl
            )
            
        } catch (e: Exception) {
            Log.e("AtCoderRepository", "Error fetching AtCoder stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

