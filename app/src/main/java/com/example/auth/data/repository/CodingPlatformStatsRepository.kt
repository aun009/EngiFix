package com.example.auth.data.repository

import android.util.Log
import com.example.auth.data.PlatformStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Combined repository that fetches stats from all coding platforms
 */
class CodingPlatformStatsRepository(
    private val leetCodeRepository: LeetCodeRepository = LeetCodeRepository(),
    private val codeforcesRepository: CodeforcesRepository = CodeforcesRepository(),
    private val codeChefRepository: CodeChefRepository = CodeChefRepository(),
    private val atCoderRepository: AtCoderRepository = AtCoderRepository(),
    private val hackerRankRepository: HackerRankRepository = HackerRankRepository(),
    private val gitHubRepository: GitHubRepository = GitHubRepository()
) {
    
    /**
     * Fetches stats for a given platform and username
     * @param platformName Name of the platform (LeetCode, Codeforces, etc.)
     * @param username Username on that platform
     * @return PlatformStats with rating and additional info
     */
    suspend fun fetchStats(platformName: String, username: String): PlatformStats = withContext(Dispatchers.IO) {
        try {
            when (platformName.lowercase()) {
                "leetcode" -> leetCodeRepository.getUserStats(username)
                "codeforces" -> codeforcesRepository.getUserStats(username)
                "codechef" -> codeChefRepository.getUserStats(username)
                "atcoder" -> atCoderRepository.getUserStats(username)
                "hackerrank" -> hackerRankRepository.getUserStats(username)
                "github" -> gitHubRepository.getUserStats(username)
                else -> {
                    Log.w("CodingPlatformStatsRepository", "Unknown platform: $platformName")
                    PlatformStats.error()
                }
            }
        } catch (e: Exception) {
            Log.e("CodingPlatformStatsRepository", "Error fetching stats for $platformName/$username: ${e.message}", e)
            PlatformStats.error()
        }
    }
}

