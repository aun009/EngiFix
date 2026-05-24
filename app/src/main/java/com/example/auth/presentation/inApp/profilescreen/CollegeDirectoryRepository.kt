package com.example.auth.presentation.inApp.profilescreen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CollegeSuggestion(
    val name: String,
    val state: String,
    val city: String,
    val address: String
) {
    val subtitle: String = listOf(city, state).filter { it.isNotBlank() }.joinToString(", ")
    val normalizedName: String = name.normalizedCollegeKey()
}

class CollegeDirectoryRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
) {
    suspend fun searchColleges(query: String, limit: Int = 8): Result<List<CollegeSuggestion>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cleanQuery = query.trim()
                if (cleanQuery.length < 2) return@runCatching popularColleges.take(limit)

                val localMatches = popularColleges.filter(cleanQuery, limit)

                val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
                val request = Request.Builder()
                    .url("$BASE_URL/colleges?search=$encodedQuery&limit=$limit")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use localMatches
                    val body = response.body?.string().orEmpty()
                    val colleges = JSONObject(body).optJSONArray("colleges") ?: return@use localMatches
                    val remoteMatches = buildList {
                        for (index in 0 until colleges.length()) {
                            val item = colleges.optJSONObject(index) ?: continue
                            val name = item.optString("Name", item.optString("name")).trim()
                            if (name.isBlank()) continue
                            add(
                                CollegeSuggestion(
                                    name = name,
                                    state = item.optString("State", item.optString("state")).trim(),
                                    city = item.optString("City", item.optString("city")).trim(),
                                    address = listOf(
                                        item.optString("Address_line1", item.optString("addressLine1")).trim(),
                                        item.optString("Address_line2", item.optString("addressLine2")).trim()
                                    ).filter { it.isNotBlank() }.joinToString(" ")
                                )
                            )
                        }
                    }

                    (remoteMatches + localMatches)
                        .distinctBy { it.normalizedName }
                        .take(limit)
                        .ifEmpty { localMatches }
                }
            }.recover { popularColleges.filter(query, limit) }
        }

    private fun List<CollegeSuggestion>.filter(query: String, limit: Int): List<CollegeSuggestion> {
        val normalized = query.normalizedCollegeKey()
        val compact = normalized.replace(" ", "")
        return filter {
            val name = it.name.normalizedCollegeKey()
            val haystack = listOf(name, it.city.normalizedCollegeKey(), it.state.normalizedCollegeKey())
                .joinToString(" ")
            val acronym = name.split(" ")
                .filterNot { word -> word in acronymStopWords }
                .mapNotNull { word -> word.firstOrNull()?.toString() }
                .joinToString("")
            haystack.contains(normalized) ||
                    acronym.contains(compact) ||
                    compact.contains(acronym) ||
                    normalized.split(" ").all { token -> token.length <= 1 || haystack.contains(token) }
        }.take(limit)
    }

    private companion object {
        const val BASE_URL = "https://colleges-api.onrender.com"
        val acronymStopWords = setOf("of", "and", "the", "for", "in")

        val popularColleges = listOf(
            CollegeSuggestion("Indian Institute of Technology Bombay", "Maharashtra", "Mumbai", ""),
            CollegeSuggestion("Indian Institute of Technology Delhi", "Delhi", "New Delhi", ""),
            CollegeSuggestion("Indian Institute of Technology Madras", "Tamil Nadu", "Chennai", ""),
            CollegeSuggestion("Indian Institute of Technology Kanpur", "Uttar Pradesh", "Kanpur", ""),
            CollegeSuggestion("Indian Institute of Technology Kharagpur", "West Bengal", "Kharagpur", ""),
            CollegeSuggestion("National Institute of Technology Tiruchirappalli", "Tamil Nadu", "Tiruchirappalli", ""),
            CollegeSuggestion("National Institute of Technology Karnataka, Surathkal", "Karnataka", "Mangaluru", ""),
            CollegeSuggestion("Vellore Institute of Technology", "Tamil Nadu", "Vellore", ""),
            CollegeSuggestion("Birla Institute of Technology and Science Pilani", "Rajasthan", "Pilani", ""),
            CollegeSuggestion("Delhi Technological University", "Delhi", "New Delhi", ""),
            CollegeSuggestion("Indian Institute of Technology Roorkee", "Uttarakhand", "Roorkee", ""),
            CollegeSuggestion("Indian Institute of Technology Guwahati", "Assam", "Guwahati", ""),
            CollegeSuggestion("Indian Institute of Technology Hyderabad", "Telangana", "Hyderabad", ""),
            CollegeSuggestion("Indian Institute of Technology Indore", "Madhya Pradesh", "Indore", ""),
            CollegeSuggestion("Indian Institute of Technology BHU Varanasi", "Uttar Pradesh", "Varanasi", ""),
            CollegeSuggestion("Indian Institute of Technology Gandhinagar", "Gujarat", "Gandhinagar", ""),
            CollegeSuggestion("National Institute of Technology Warangal", "Telangana", "Warangal", ""),
            CollegeSuggestion("National Institute of Technology Rourkela", "Odisha", "Rourkela", ""),
            CollegeSuggestion("National Institute of Technology Calicut", "Kerala", "Kozhikode", ""),
            CollegeSuggestion("National Institute of Technology Durgapur", "West Bengal", "Durgapur", ""),
            CollegeSuggestion("National Institute of Technology Kurukshetra", "Haryana", "Kurukshetra", ""),
            CollegeSuggestion("National Institute of Technology Silchar", "Assam", "Silchar", ""),
            CollegeSuggestion("National Institute of Technology Hamirpur", "Himachal Pradesh", "Hamirpur", ""),
            CollegeSuggestion("National Institute of Technology Delhi", "Delhi", "New Delhi", ""),
            CollegeSuggestion("International Institute of Information Technology Hyderabad", "Telangana", "Hyderabad", ""),
            CollegeSuggestion("Indraprastha Institute of Information Technology Delhi", "Delhi", "New Delhi", ""),
            CollegeSuggestion("International Institute of Information Technology Bangalore", "Karnataka", "Bengaluru", ""),
            CollegeSuggestion("Indian Institute of Information Technology Allahabad", "Uttar Pradesh", "Prayagraj", ""),
            CollegeSuggestion("Indian Institute of Information Technology Design and Manufacturing Jabalpur", "Madhya Pradesh", "Jabalpur", ""),
            CollegeSuggestion("Jadavpur University", "West Bengal", "Kolkata", ""),
            CollegeSuggestion("Anna University", "Tamil Nadu", "Chennai", ""),
            CollegeSuggestion("College of Engineering Pune", "Maharashtra", "Pune", ""),
            CollegeSuggestion("Veermata Jijabai Technological Institute", "Maharashtra", "Mumbai", ""),
            CollegeSuggestion("PSG College of Technology", "Tamil Nadu", "Coimbatore", ""),
            CollegeSuggestion("Thapar Institute of Engineering and Technology", "Punjab", "Patiala", ""),
            CollegeSuggestion("Manipal Institute of Technology", "Karnataka", "Manipal", ""),
            CollegeSuggestion("SRM Institute of Science and Technology", "Tamil Nadu", "Chennai", ""),
            CollegeSuggestion("Amrita Vishwa Vidyapeetham", "Tamil Nadu", "Coimbatore", ""),
            CollegeSuggestion("Lovely Professional University", "Punjab", "Phagwara", ""),
            CollegeSuggestion("Chandigarh University", "Punjab", "Mohali", ""),
            CollegeSuggestion("Kalinga Institute of Industrial Technology", "Odisha", "Bhubaneswar", ""),
            CollegeSuggestion("Shiv Nadar University", "Uttar Pradesh", "Greater Noida", ""),
            CollegeSuggestion("Jaypee Institute of Information Technology", "Uttar Pradesh", "Noida", ""),
            CollegeSuggestion("Netaji Subhas University of Technology", "Delhi", "New Delhi", ""),
            CollegeSuggestion("Guru Gobind Singh Indraprastha University", "Delhi", "New Delhi", ""),
            CollegeSuggestion("PES University", "Karnataka", "Bengaluru", ""),
            CollegeSuggestion("RV College of Engineering", "Karnataka", "Bengaluru", ""),
            CollegeSuggestion("BMS College of Engineering", "Karnataka", "Bengaluru", ""),
            CollegeSuggestion("MS Ramaiah Institute of Technology", "Karnataka", "Bengaluru", ""),
            CollegeSuggestion("Sardar Patel Institute of Technology", "Maharashtra", "Mumbai", ""),
            CollegeSuggestion("Dwarkadas J. Sanghvi College of Engineering", "Maharashtra", "Mumbai", ""),
            CollegeSuggestion("K. J. Somaiya College of Engineering", "Maharashtra", "Mumbai", ""),
            CollegeSuggestion("Institute of Engineering and Management", "West Bengal", "Kolkata", ""),
            CollegeSuggestion("Heritage Institute of Technology", "West Bengal", "Kolkata", ""),
            CollegeSuggestion("Chitkara University", "Punjab", "Rajpura", ""),
            CollegeSuggestion("University of Petroleum and Energy Studies", "Uttarakhand", "Dehradun", "")
        )
    }
}

fun String.normalizedCollegeKey(): String =
    trim()
        .lowercase(Locale.ROOT)
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
