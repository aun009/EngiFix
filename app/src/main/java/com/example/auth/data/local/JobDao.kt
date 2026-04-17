package com.example.auth.data.local

import androidx.room.*
import com.example.auth.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    /** All jobs, newest posting first (jobId DESC) */
    @Query("SELECT * FROM jobs ORDER BY jobId DESC, scrapedAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getCount(): Int

    @Query("SELECT MAX(scrapedAt) FROM jobs")
    suspend fun getLastScrapedTime(): Long?
}
