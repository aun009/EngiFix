package com.example.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,       // MD5 hash for dedup
    val jobId: Int = 0,               // From URL ?job_id= — higher = newer posting
    val company: String,
    val role: String,
    val batchYears: String,           // Raw string e.g. "2026 | 2025" or "Freshers"
    val location: String,
    val qualification: String,
    val salary: String,
    val applyUrl: String,
    val category: String,             // "Internship" | "Full-time"
    val scrapedAt: Long = System.currentTimeMillis()
)
