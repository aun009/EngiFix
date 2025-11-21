package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for fetching Codeforces user stats
 * Uses official REST API
 */
class CodeforcesRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Codeforces official API
            val apiUrl = "https://codeforces.com/api/user.info?handles=$username"
            
            val response = Jsoup.connect(apiUrl)
                .ignoreContentType(true)
                .timeout(10000)
                .execute()
            
            val jsonResponse = response.body()
            val json = JSONObject(jsonResponse)
            
            if (json.getString("status") == "OK") {
                val resultArray = json.getJSONArray("result")
                if (resultArray.length() > 0) {
                    val user = resultArray.getJSONObject(0)
                    
                    val rating = user.optInt("rating", -1)
                    val maxRating = user.optInt("maxRating", -1)
                    val rank = user.optString("rank", "N/A")
                    val maxRank = user.optString("maxRank", "N/A")
                    val titlePhoto = user.optString("titlePhoto", null)
                    
                    val ratingStr = if (rating > 0) rating.toString() else "Unrated"
                    // Codeforces returns relative URL, need to prepend domain
                    val profileImageUrl = if (titlePhoto != null && titlePhoto.isNotEmpty() && !titlePhoto.startsWith("http")) {
                        "https:${titlePhoto}"
                    } else {
                        titlePhoto
                    }
                    
                    return@withContext PlatformStats(
                        rating = ratingStr,
                        statsLabel = rank.ifEmpty { "Unrated" },
                        additionalInfo = mapOf(
                            "Max Rating" to (if (maxRating > 0) maxRating.toString() else "N/A"),
                            "Max Rank" to maxRank
                        ),
                        profileImageUrl = profileImageUrl
                    )
                }
            }
            
            PlatformStats.error()
            
        } catch (e: Exception) {
            Log.e("CodeforcesRepository", "Error fetching Codeforces stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

