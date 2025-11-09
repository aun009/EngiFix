package com.example.auth.presentation.features.contest

import androidx.compose.ui.graphics.Color

object PlatformColors {
    // Codeforces - Blue theme
    val CODEFORCES = Color(0xFF1F75FE)
    
    // CodeChef - Orange theme
    val CODECHEF = Color(0xFFFF6B35)
    
    // AtCoder - Green theme
    val ATCODER = Color(0xFF00B894)
    
    // LeetCode - Yellow theme
    val LEETCODE = Color(0xFFFFA116)
    
    // Get color by platform name
    fun getColorForPlatform(platformName: String): Color {
        return when (platformName.lowercase()) {
            "codeforces" -> CODEFORCES
            "codechef" -> CODECHEF
            "atcoder" -> ATCODER
            "leetcode" -> LEETCODE
            else -> Color(0xFF6C757D) // Default gray
        }
    }
}
