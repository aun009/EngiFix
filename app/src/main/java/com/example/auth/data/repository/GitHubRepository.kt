package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONObject

/**
 * Repository for fetching GitHub user stats
 * Uses GitHub REST API
 */
class GitHubRepository {
    
    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // GitHub official REST API (no auth needed for public data)
            val apiUrl = "https://api.github.com/users/$username"
            
            val response = Jsoup.connect(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .ignoreContentType(true)
                .timeout(10000)
                .execute()
            
            val jsonResponse = response.body()
            val json = JSONObject(jsonResponse)
            
            // Check if user exists (GitHub API returns 404 if not found, but Jsoup might not throw)
            if (json.has("message") && json.getString("message").contains("Not Found")) {
                return@withContext PlatformStats.error()
            }
            
            // Get public repos count
            val publicRepos = json.optInt("public_repos", 0)
            val followers = json.optInt("followers", 0)
            val following = json.optInt("following", 0)
            // Use GitHub's direct avatar URL which always works for public profiles
            val avatarUrl = "https://github.com/$username.png"
            
            // GitHub doesn't have a "rating" system, so we use repos as the main stat
            return@withContext PlatformStats(
                rating = publicRepos.toString(),
                statsLabel = "Public Repos",
                additionalInfo = mapOf(
                    "Followers" to followers.toString(),
                    "Following" to following.toString()
                ),
                profileImageUrl = avatarUrl
            )
            
        } catch (e: Exception) {
            Log.e("GitHubRepository", "Error fetching GitHub stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

