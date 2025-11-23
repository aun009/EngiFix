package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

/**
 * Repository for fetching AtCoder user stats
 * Uses official AtCoder API + HTML scraping for profile image
 */
class AtCoderRepository {

    suspend fun getUserStats(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            // Method 1: Use Official AtCoder History API (MOST RELIABLE!)
            val ratingData = fetchRatingFromHistory(username)

            // Method 2: Fetch profile image from user page
            val profileImageUrl = fetchProfileImage(username)

            if (ratingData != null) {
                return@withContext PlatformStats(
                    rating = ratingData.currentRating.toString(),
                    statsLabel = "Rating",
                    additionalInfo = mapOf(
                        "Highest Rating" to ratingData.maxRating.toString(),
                        "Contests" to ratingData.contestCount.toString()
                    ),
                    profileImageUrl = profileImageUrl
                )
            }

            // Fallback: Try k3lcoder API
            val apiStats = fetchFromK3lcoderAPI(username)
            if (apiStats != null) {
                return@withContext apiStats.copy(profileImageUrl = profileImageUrl)
            }

            // Last resort: HTML scraping
            return@withContext fetchFromHTMLScraping(username)

        } catch (e: Exception) {
            Log.e("AtCoderRepository", "Error fetching AtCoder stats for $username: ${e.message}", e)
            PlatformStats.error()
        }
    }

    /**
     * Fetch rating from official AtCoder history API (BEST METHOD!)
     */
    private suspend fun fetchRatingFromHistory(username: String): RatingData? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://atcoder.jp/users/$username/history/json"
            val jsonText = URL(apiUrl).readText()
            val jsonArray = JSONArray(jsonText)

            if (jsonArray.length() == 0) {
                // User exists but hasn't participated in any rated contests
                return@withContext RatingData(
                    currentRating = 0,
                    maxRating = 0,
                    contestCount = 0
                )
            }

            // Get latest rating (last element)
            val lastContest = jsonArray.getJSONObject(jsonArray.length() - 1)
            val currentRating = lastContest.getInt("NewRating")

            // Find max rating across all contests
            var maxRating = currentRating
            for (i in 0 until jsonArray.length()) {
                val contest = jsonArray.getJSONObject(i)
                val rating = contest.getInt("NewRating")
                if (rating > maxRating) {
                    maxRating = rating
                }
            }

            RatingData(
                currentRating = currentRating,
                maxRating = maxRating,
                contestCount = jsonArray.length()
            )
        } catch (e: Exception) {
            Log.d("AtCoderRepository", "Failed to fetch from history API: ${e.message}")
            null
        }
    }

    /**
     * Fetch profile image from user page
     */
    private suspend fun fetchProfileImage(username: String): String? = withContext(Dispatchers.IO) {
        try {
            val profileUrl = "https://atcoder.jp/users/$username"
            val doc = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            // Look for profile image - AtCoder uses different selectors
            val imgElement = doc.selectFirst("img.avatar")
                ?: doc.selectFirst("img[alt*='avatar']")
                ?: doc.selectFirst(".avatar img")

            val imgSrc = imgElement?.attr("src")

            when {
                imgSrc == null -> null
                imgSrc.startsWith("//") -> "https:$imgSrc"
                imgSrc.startsWith("/") -> "https://atcoder.jp$imgSrc"
                else -> imgSrc
            }
        } catch (e: Exception) {
            Log.d("AtCoderRepository", "Failed to fetch profile image: ${e.message}")
            null
        }
    }

    /**
     * Fallback: Try k3lcoder API
     */
    private suspend fun fetchFromK3lcoderAPI(username: String): PlatformStats? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://kcoder.cool/atcoder-api/v1/user-info?user=$username"
            val response = Jsoup.connect(apiUrl)
                .ignoreContentType(true)
                .timeout(10000)
                .execute()

            val jsonResponse = response.body()
            val json = JSONObject(jsonResponse)

            if (json.has("rating")) {
                val rating = json.optInt("rating", 0)
                val highestRating = json.optInt("highestRating", 0)
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
            null
        } catch (e: Exception) {
            Log.d("AtCoderRepository", "k3lcoder API failed: ${e.message}")
            null
        }
    }

    /**
     * Last resort: HTML scraping (least reliable)
     */
    private suspend fun fetchFromHTMLScraping(username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            val profileUrl = "https://atcoder.jp/users/$username"
            val doc = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            // Try multiple selectors for rating
            var rating = "N/A"

            // Method 1: Look in user info table
            val ratingRow = doc.selectFirst("table.dl-table tr:has(th:contains(Rating))")
            if (ratingRow != null) {
                val ratingText = ratingRow.selectFirst("td")?.text()
                rating = ratingText?.replace(Regex("[^0-9]"), "") ?: "N/A"
            }

            // Method 2: Look for rating span
            if (rating == "N/A") {
                val ratingSpan = doc.selectFirst("span.user-rating")
                rating = ratingSpan?.text()?.replace(Regex("[^0-9]"), "") ?: "N/A"
            }

            // Try to find profile image
            var profileImageUrl: String? = null
            try {
                val imgElement = doc.selectFirst("img.avatar")
                    ?: doc.selectFirst("img[alt*='avatar']")

                val imgSrc = imgElement?.attr("src")
                if (imgSrc != null) {
                    profileImageUrl = when {
                        imgSrc.startsWith("//") -> "https:$imgSrc"
                        imgSrc.startsWith("/") -> "https://atcoder.jp$imgSrc"
                        else -> imgSrc
                    }
                }
            } catch (e: Exception) {
                Log.d("AtCoderRepository", "Image fetch error: ${e.message}")
            }

            PlatformStats(
                rating = rating,
                statsLabel = "Rating",
                additionalInfo = emptyMap(),
                profileImageUrl = profileImageUrl
            )

        } catch (e: Exception) {
            Log.e("AtCoderRepository", "HTML scraping failed: ${e.message}", e)
            PlatformStats.error()
        }
    }

    /**
     * Data class to hold rating information
     */
    private data class RatingData(
        val currentRating: Int,
        val maxRating: Int,
        val contestCount: Int
    )
}