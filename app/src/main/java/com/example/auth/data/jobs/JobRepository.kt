package com.example.auth.data.jobs

import com.example.auth.data.local.JobDao
import com.example.auth.data.local.entity.JobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Refresh cache every 3 hours
private const val CACHE_TTL_MS = 3 * 60 * 60 * 1000L

@Singleton
class JobRepository @Inject constructor(
    private val dao: JobDao,
    private val scraper: JobScraper
) {
    fun getAllJobs(): Flow<List<JobEntity>> = dao.getAllJobs()

    /** Scrape fresh data if cache is empty or older than 3 hours */
    suspend fun refreshIfStale() = withContext(Dispatchers.IO) {
        val count     = dao.getCount()
        val lastScrape = dao.getLastScrapedTime() ?: 0L
        val isStale   = System.currentTimeMillis() - lastScrape > CACHE_TTL_MS
        if (count == 0 || isStale) {
            val fresh = scraper.scrapeAll()
            if (fresh.isNotEmpty()) { dao.clearAll(); dao.insertAll(fresh) }
        }
    }

    /** Force refresh regardless of cache age */
    suspend fun forceRefresh() = withContext(Dispatchers.IO) {
        val fresh = scraper.scrapeAll()
        if (fresh.isNotEmpty()) { dao.clearAll(); dao.insertAll(fresh) }
    }
}
