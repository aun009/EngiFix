package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repository for fetching LeetCode user stats
 * Uses official LeetCode GraphQL API (NO authentication needed for public profiles!)
 */
class LeetCodeRepository {

    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Method 1: Official LeetCode GraphQL API (BEST!)
            val graphqlStats = fetchFromGraphQL(username)
            if (graphqlStats != null) {
                return@withContext graphqlStats
            }

            // Fallback: Third-party API
            val apiStats = fetchFromThirdPartyAPI(username)
            if (apiStats != null) {
                return@withContext apiStats
            }

            // Last resort
            PlatformStats.error()

        } catch (e: Exception) {
            Log.e("LeetCodeRepository", "Error fetching LeetCode stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }

    /**
     * Fetch from official LeetCode GraphQL API (NO AUTH REQUIRED!)
     */
    private suspend fun fetchFromGraphQL(username: String): PlatformStats? = withContext(Dispatchers.IO) {
        try {
            // GraphQL query to get user profile data
            val query = """
                query getUserProfile(${'$'}username: String!) {
                    matchedUser(username: ${'$'}username) {
                        username
                        profile {
                            userAvatar
                            realName
                            ranking
                        }
                        submitStats {
                            acSubmissionNum {
                                difficulty
                                count
                            }
                        }
                    }
                }
            """.trimIndent()

            val variables = JSONObject().apply {
                put("username", username)
            }

            val requestBody = JSONObject().apply {
                put("query", query)
                put("variables", variables)
            }

            // Make HTTP request
            val url = URL("https://leetcode.com/graphql")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            // Send request
            conn.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }

            // Read response
            val responseCode = conn.responseCode
            if (responseCode != 200) {
                Log.d("LeetCodeRepository", "GraphQL API returned code: $responseCode")
                return@withContext null
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            // Parse response
            val data = json.optJSONObject("data")
            val matchedUser = data?.optJSONObject("matchedUser")

            if (matchedUser == null) {
                Log.d("LeetCodeRepository", "User not found in GraphQL response")
                return@withContext null
            }

            // Extract profile data
            val profile = matchedUser.optJSONObject("profile")
            val avatar = profile?.optString("userAvatar")
            val ranking = profile?.optInt("ranking", 0) ?: 0

            // Extract submission stats
            val submitStats = matchedUser.optJSONObject("submitStats")
            val acSubmissionNum = submitStats?.optJSONArray("acSubmissionNum")

            var totalSolved = 0
            var easySolved = 0
            var mediumSolved = 0
            var hardSolved = 0

            if (acSubmissionNum != null) {
                for (i in 0 until acSubmissionNum.length()) {
                    val item = acSubmissionNum.getJSONObject(i)
                    val difficulty = item.getString("difficulty")
                    val count = item.getInt("count")

                    when (difficulty) {
                        "All" -> totalSolved = count
                        "Easy" -> easySolved = count
                        "Medium" -> mediumSolved = count
                        "Hard" -> hardSolved = count
                    }
                }
            }

            // Format profile image URL
            val profileImageUrl = when {
                avatar.isNullOrEmpty() -> null
                avatar.startsWith("http") -> avatar
                else -> "https://leetcode.com$avatar"
            }

            PlatformStats(
                rating = totalSolved.toString(),
                statsLabel = "Problems Solved",
                additionalInfo = mapOf(
                    "Ranking" to if (ranking > 0) "#$ranking" else "N/A",
                    "Easy" to easySolved.toString(),
                    "Medium" to mediumSolved.toString(),
                    "Hard" to hardSolved.toString()
                ),
                profileImageUrl = profileImageUrl
            )

        } catch (e: Exception) {
            Log.e("LeetCodeRepository", "GraphQL fetch failed: ${e.message}", e)
            null
        }
    }

    /**
     * Fallback: Third-party API
     */
    private suspend fun fetchFromThirdPartyAPI(username: String): PlatformStats? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://leetcode-stats-api.herokuapp.com/$username"
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val responseCode = conn.responseCode
            if (responseCode != 200) {
                return@withContext null
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            if (json.has("status") && json.getString("status") == "success") {
                val ranking = json.optInt("ranking", 0)
                val totalSolved = json.optInt("totalSolved", 0)
                val easySolved = json.optInt("easySolved", 0)
                val mediumSolved = json.optInt("mediumSolved", 0)
                val hardSolved = json.optInt("hardSolved", 0)

                // Note: Third-party API doesn't provide profile images
                // We would need to make a separate request to LeetCode profile page

                return@withContext PlatformStats(
                    rating = totalSolved.toString(),
                    statsLabel = "Problems Solved",
                    additionalInfo = mapOf(
                        "Ranking" to if (ranking > 0) "#$ranking" else "N/A",
                        "Easy" to easySolved.toString(),
                        "Medium" to mediumSolved.toString(),
                        "Hard" to hardSolved.toString()
                    ),
                    profileImageUrl = null
                )
            }
            null
        } catch (e: Exception) {
            Log.d("LeetCodeRepository", "Third-party API failed: ${e.message}")
            null
        }
    }
}