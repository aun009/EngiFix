package com.example.auth.data.jobs

import android.util.Log
import com.example.auth.data.local.entity.JobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JobScraper"
private const val BASE_URL = "https://www.thejobcompany.co.in"
private const val TIMEOUT_MS = 15_000
private val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@Singleton
class JobScraper @Inject constructor() {

    /**
     * Scrape internship listings + batch-specific pages (which include mixed roles).
     * Category is derived from the role title — not the URL — for accuracy.
     */
    suspend fun scrapeAll(maxPages: Int = 3): List<JobEntity> = withContext(Dispatchers.IO) {
        val seen    = mutableSetOf<String>()
        val results = mutableListOf<JobEntity>()

        // 1. Internship page (most fresh listings)
        results += scrapeCategory("job-category/internships", maxPages, seen)

        // 2. Batch pages (capture full-time & trainee roles not on internships page)
        for (year in listOf(2027, 2026, 2025)) {
            results += scrapeCategory("job-category/batch/$year", maxPages = 2, seen = seen)
        }

        Log.d(TAG, "Total scraped: ${results.size} jobs")
        results
    }

    // ── Core ──────────────────────────────────────────────────────────────────

    private fun scrapeCategory(
        path: String,
        maxPages: Int,
        seen: MutableSet<String>
    ): List<JobEntity> {
        val results = mutableListOf<JobEntity>()
        for (page in 1..maxPages) {
            val url = if (page == 1) "$BASE_URL/$path" else "$BASE_URL/$path?page=$page"
            val pageJobs = scrapePage(url, seen)
            if (pageJobs.isEmpty()) break
            results += pageJobs
            if (page < maxPages) Thread.sleep(1_200)
        }
        return results
    }

    private fun scrapePage(url: String, seen: MutableSet<String>): List<JobEntity> {
        return try {
            Log.d(TAG, "Fetching $url")
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .header("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", BASE_URL)
                .followRedirects(true)
                .get()

            val cards = doc.select("a.applyBtn")
            Log.d(TAG, "${cards.size} cards on $url")

            val jobs = mutableListOf<JobEntity>()
            cards.forEach { card ->
                try {
                    val rawHref  = card.attr("href").trim()

                    // Resolve relative URLs like ../frontend/job_details.php?job_id=5786
                    val applyUrl = when {
                        rawHref.startsWith("http") -> rawHref
                        rawHref.startsWith("../")  -> "$BASE_URL/${rawHref.removePrefix("../")}"
                        rawHref.startsWith("/")    -> "$BASE_URL$rawHref"
                        else                       -> "$BASE_URL/$rawHref"
                    }

                    // Higher job_id = newer posting → used for sort
                    val jobId = Regex("job_id=(\\d+)").find(rawHref)
                        ?.groupValues?.get(1)?.toIntOrNull() ?: 0

                    // Title: "Company is hiring Role"
                    val titleText = (card.selectFirst("p.company-title") ?: card.select("p").first())
                        ?.text()?.trim() ?: return@forEach

                    val (company, role) = if ("is hiring" in titleText.lowercase()) {
                        val parts = titleText.split("is hiring", ignoreCase = true)
                        parts[0].trim() to parts.getOrElse(1) { "" }.trim()
                    } else "Company" to titleText

                    if (role.isBlank()) return@forEach

                    // Meta fields
                    fun meta(label: String) = card.select("p:contains($label)")
                        .firstOrNull()?.text()?.substringAfter(":")?.trim() ?: ""

                    val batch    = meta("Batch")
                    val location = meta("Location")
                    val qual     = meta("Qualification")
                    val salary   = meta("Salary")

                    // Category from role title — NOT the URL (internship page ≠ all intern)
                    val category = detectCategory(role)

                    // Dedup
                    val id = md5("$company|$role|$location")
                    if (id in seen) return@forEach
                    seen += id

                    jobs += JobEntity(
                        id            = id,
                        jobId         = jobId,
                        company       = company,
                        role          = role,
                        batchYears    = batch,
                        location      = location,
                        qualification = qual,
                        salary        = salary,
                        applyUrl      = applyUrl,
                        category      = category
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Parse error: ${e.message}")
                }
            }
            jobs
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed $url: ${e.message}")
            emptyList()
        }
    }

    private fun detectCategory(role: String): String {
        val r = role.lowercase()
        return when {
            "intern" in r || "internship" in r -> "Internship"
            "trainee" in r                     -> "Trainee"
            "fresher" in r || "fresher" in r   -> "Fresher"
            "part-time" in r || "part time" in r -> "Part-time"
            else                               -> "Full-time"
        }
    }

    private fun md5(input: String): String =
        MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
