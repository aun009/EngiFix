package com.example.auth.presentation.inApp.profilescreen

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CollegeState(
    val name: String,
    val slug: String
)

data class CollegeSuggestion(
    val name: String,
    val state: String,
    val city: String,
    val address: String
) {
    val subtitle: String = listOf(city, state).filter { it.isNotBlank() }.joinToString(", ")
    val normalizedName: String = name.normalizedCollegeKey()
}

data class CollegeSearchResponse(
    val colleges: List<CollegeSuggestion>,
    val isFromCache: Boolean = false,
    val warning: String? = null
)

class CollegeDirectoryRepository(
    context: Context? = null,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(18, TimeUnit.SECONDS)
        .build()
) {
    private val prefs = context
        ?.applicationContext
        ?.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)

    private val memorySearchCache = object : LinkedHashMap<String, List<CollegeSuggestion>>(
        MAX_RECENT_SEARCHES,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, List<CollegeSuggestion>>?
        ): Boolean = size > MAX_RECENT_SEARCHES
    }
    private var memoryStates: List<CollegeState>? = null

    suspend fun getStates(forceRefresh: Boolean = false): Result<List<CollegeState>> =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                memoryStates?.takeIf { it.isNotEmpty() }?.let { return@withContext Result.success(it) }
                readCachedStates()?.takeIf { it.isNotEmpty() }?.let { cached ->
                    memoryStates = cached
                    return@withContext Result.success(cached)
                }
            }

            runCatching {
                val request = Request.Builder()
                    .url("$BASE_URL/api/institutions/states")
                    .get()
                    .build()

                val states = retryNetwork {
                    parseStates(executeBody(request))
                }.ifEmpty { fallbackStates }

                memoryStates = states
                cacheStates(states)
                states
            }.recover {
                readCachedStates()?.takeIf { cached -> cached.isNotEmpty() } ?: fallbackStates
            }
        }

    suspend fun searchColleges(
        query: String,
        state: String,
        limit: Int = DEFAULT_LIMIT,
        forceRefresh: Boolean = false
    ): Result<CollegeSearchResponse> = withContext(Dispatchers.IO) {
        val cleanQuery = query.sanitizedCollegeInput()
        val cleanState = state.sanitizedCollegeInput()
        val safeLimit = limit.coerceIn(1, DEFAULT_LIMIT)

        if (cleanQuery.length < MIN_QUERY_LENGTH) {
            return@withContext Result.success(
                CollegeSearchResponse(
                    colleges = emptyList(),
                    warning = "Type at least 3 characters."
                )
            )
        }

        val localMatches = popularColleges.filter(cleanQuery, cleanState, safeLimit)
        val cacheKey = searchCacheKey(cleanState, cleanQuery, safeLimit)

        if (!forceRefresh) {
            readCachedSearch(cacheKey)?.takeIf { it.isNotEmpty() }?.let { cached ->
                return@withContext Result.success(
                    CollegeSearchResponse(colleges = cached, isFromCache = true)
                )
            }
        }

        if (cleanState.isBlank()) {
            return@withContext Result.success(
                CollegeSearchResponse(
                    colleges = localMatches,
                    warning = "Select a state for complete API results."
                )
            )
        }

        runCatching {
            val encodedState = URLEncoder.encode(cleanState, "UTF-8")
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
            val request = Request.Builder()
                .url("$BASE_URL/api/institutions/search?state=$encodedState&q=$encodedQuery&page=1&limit=$safeLimit")
                .get()
                .build()

            val remoteMatches = retryNetwork {
                parseSearchResults(executeBody(request))
            }

            val colleges = (remoteMatches + localMatches)
                .distinctBy { it.normalizedName }
                .take(safeLimit)

            val resolved = colleges.ifEmpty { localMatches }
            cacheSearch(cacheKey, resolved)
            CollegeSearchResponse(colleges = resolved)
        }.recoverCatching { error ->
            val cached = readCachedSearch(cacheKey)
            when {
                !cached.isNullOrEmpty() -> CollegeSearchResponse(
                    colleges = cached,
                    isFromCache = true,
                    warning = "Network issue. Showing cached results."
                )
                localMatches.isNotEmpty() -> CollegeSearchResponse(
                    colleges = localMatches,
                    warning = "Network issue. Showing offline suggestions."
                )
                else -> throw IllegalStateException(error.message ?: "Could not load colleges.")
            }
        }
    }

    private fun executeBody(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("College API returned ${response.code}.")
            }
            if (body.isBlank()) {
                throw IllegalStateException("College API returned an empty response.")
            }
            return body
        }
    }

    private suspend fun <T> retryNetwork(block: () -> T): T {
        var lastError: Throwable? = null
        var waitMs = FIRST_RETRY_DELAY_MS

        repeat(MAX_RETRIES) { attempt ->
            try {
                return block()
            } catch (error: Throwable) {
                lastError = error
                if (attempt < MAX_RETRIES - 1) {
                    delay(waitMs)
                    waitMs *= 2
                }
            }
        }

        throw lastError ?: IllegalStateException("Network request failed.")
    }

    private fun readCachedSearch(key: String): List<CollegeSuggestion>? {
        synchronized(memorySearchCache) {
            memorySearchCache[key]?.let { return it }
        }

        val cached = prefs?.getString(key, null)
            ?.let(::decodeSuggestions)
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        synchronized(memorySearchCache) {
            memorySearchCache[key] = cached
        }
        return cached
    }

    private fun cacheSearch(key: String, colleges: List<CollegeSuggestion>) {
        if (colleges.isEmpty()) return
        synchronized(memorySearchCache) {
            memorySearchCache[key] = colleges
        }
        prefs?.edit()?.putString(key, encodeSuggestions(colleges))?.apply()
    }

    private fun readCachedStates(): List<CollegeState>? =
        prefs?.getString(STATES_CACHE_KEY, null)
            ?.let(::decodeStates)
            ?.takeIf { it.isNotEmpty() }

    private fun cacheStates(states: List<CollegeState>) {
        if (states.isEmpty()) return
        prefs?.edit()?.putString(STATES_CACHE_KEY, encodeStates(states))?.apply()
    }

    private fun List<CollegeSuggestion>.filter(
        query: String,
        state: String,
        limit: Int
    ): List<CollegeSuggestion> {
        val normalized = query.normalizedCollegeKey()
        val compact = normalized.replace(" ", "")
        val normalizedState = state.normalizedCollegeKey()

        return filter { college ->
            val name = college.name.normalizedCollegeKey()
            val collegeState = college.state.normalizedCollegeKey()
            val haystack = listOf(name, college.city.normalizedCollegeKey(), collegeState)
                .joinToString(" ")
            val acronym = name.split(" ")
                .filterNot { word -> word in acronymStopWords }
                .mapNotNull { word -> word.firstOrNull()?.toString() }
                .joinToString("")
            val matchesQuery = haystack.contains(normalized) ||
                    acronym.contains(compact) ||
                    compact.contains(acronym) ||
                    normalized.split(" ").all { token ->
                        token.length <= 1 || haystack.contains(token)
                    }
            val matchesState = normalizedState.isBlank() || collegeState == normalizedState
            matchesQuery && matchesState
        }.take(limit)
    }

    private companion object {
        const val BASE_URL = "https://indian-colleges-list.vercel.app"
        const val CACHE_PREFS = "college_directory_cache"
        const val STATES_CACHE_KEY = "states"
        const val DEFAULT_LIMIT = 20
        const val MIN_QUERY_LENGTH = 3
        const val MAX_RETRIES = 3
        const val MAX_RECENT_SEARCHES = 36
        const val FIRST_RETRY_DELAY_MS = 250L

        val acronymStopWords = setOf("of", "and", "the", "for", "in")

        val fallbackStates = listOf(
            "Andaman and Nicobar Islands",
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chandigarh",
            "Chhattisgarh",
            "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jammu and Kashmir",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Ladakh",
            "Lakshadweep",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Puducherry",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttar Pradesh",
            "Uttarakhand",
            "West Bengal"
        ).map { state ->
            CollegeState(
                name = state,
                slug = state.normalizedCollegeKey().replace(" ", "-")
            )
        }

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

private fun parseStates(body: String): List<CollegeState> {
    val root = runCatching { JSONTokener(body).nextValue() }.getOrNull()
    val states = when (root) {
        is JSONObject -> root.firstArray("states", "data", "items", "results")
        is JSONArray -> root
        else -> null
    } ?: return emptyList()

    return buildList {
        for (index in 0 until states.length()) {
            when (val item = states.opt(index)) {
                is JSONObject -> {
                    val name = item.optFirstString("name", "state", "State")
                    if (name.isNotBlank()) {
                        add(
                            CollegeState(
                                name = name,
                                slug = item.optFirstString("slug", "id").ifBlank {
                                    name.normalizedCollegeKey().replace(" ", "-")
                                }
                            )
                        )
                    }
                }
                is String -> {
                    val name = item.trim()
                    if (name.isNotBlank()) {
                        add(
                            CollegeState(
                                name = name,
                                slug = name.normalizedCollegeKey().replace(" ", "-")
                            )
                        )
                    }
                }
            }
        }
    }.distinctBy { it.name.normalizedCollegeKey() }
        .sortedBy { it.name }
}

private fun parseSearchResults(body: String): List<CollegeSuggestion> {
    val root = runCatching { JSONTokener(body).nextValue() }.getOrNull()
    val institutions = when (root) {
        is JSONObject -> root.firstArray("institutions", "results", "data", "items", "colleges")
        is JSONArray -> root
        else -> null
    } ?: return emptyList()

    return buildList {
        for (index in 0 until institutions.length()) {
            val item = institutions.optJSONObject(index) ?: continue
            val name = item.optFirstString(
                "name",
                "Name",
                "institutionName",
                "institution_name",
                "college",
                "College Name"
            )
            if (name.isBlank()) continue

            val state = item.optFirstString("state", "State", "stateName", "state_name")
            val city = item.optFirstString("city", "City", "district", "District")
            val address = listOf(
                item.optFirstString("address", "Address"),
                item.optFirstString("addressLine1", "Address_line1", "address_line1"),
                item.optFirstString("addressLine2", "Address_line2", "address_line2")
            ).filter { it.isNotBlank() }.joinToString(" ")

            add(
                CollegeSuggestion(
                    name = name,
                    state = state,
                    city = city,
                    address = address
                )
            )
        }
    }.distinctBy { it.normalizedName }
}

private fun JSONObject.firstArray(vararg keys: String): JSONArray? {
    keys.forEach { key ->
        optJSONArray(key)?.let { return it }
    }
    return null
}

private fun JSONObject.optFirstString(vararg keys: String): String {
    keys.forEach { key ->
        val value = optString(key).trim()
        if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) return value
    }
    return ""
}

private fun encodeSuggestions(colleges: List<CollegeSuggestion>): String =
    JSONArray().apply {
        colleges.forEach { college ->
            put(
                JSONObject()
                    .put("name", college.name)
                    .put("state", college.state)
                    .put("city", college.city)
                    .put("address", college.address)
            )
        }
    }.toString()

private fun decodeSuggestions(raw: String): List<CollegeSuggestion> =
    runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (name.isBlank()) continue
                add(
                    CollegeSuggestion(
                        name = name,
                        state = item.optString("state").trim(),
                        city = item.optString("city").trim(),
                        address = item.optString("address").trim()
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

private fun encodeStates(states: List<CollegeState>): String =
    JSONArray().apply {
        states.forEach { state ->
            put(JSONObject().put("name", state.name).put("slug", state.slug))
        }
    }.toString()

private fun decodeStates(raw: String): List<CollegeState> =
    runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (name.isBlank()) continue
                add(
                    CollegeState(
                        name = name,
                        slug = item.optString("slug").ifBlank {
                            name.normalizedCollegeKey().replace(" ", "-")
                        }
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

private fun searchCacheKey(state: String, query: String, limit: Int): String =
    "search_${state.normalizedCollegeKey()}_${query.normalizedCollegeKey()}_$limit"

fun String.sanitizedCollegeInput(): String =
    replace(Regex("[^\\p{L}\\p{N}\\s.&'(),-]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(120)

fun String.normalizedCollegeKey(): String =
    trim()
        .lowercase(Locale.ROOT)
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
