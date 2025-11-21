package com.example.auth.data

/**
 * Data class representing stats fetched from a coding platform
 */
data class PlatformStats(
    val rating: String = "N/A",
    val statsLabel: String = "Rating",
    val additionalInfo: Map<String, String> = emptyMap(), // For extra data like problems solved, rank, etc.
    val profileImageUrl: String? = null // Profile image URL from the platform
) {
    companion object {
        fun error(): PlatformStats = PlatformStats(
            rating = "Error",
            statsLabel = "Failed to load"
        )
        
        fun loading(): PlatformStats = PlatformStats(
            rating = "N/A",
            statsLabel = "Loading..."
        )
    }
}

