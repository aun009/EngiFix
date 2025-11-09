package com.example.auth.presentation.features.contest

import retrofit2.http.GET
import retrofit2.http.Query

interface ClistApi {
    @GET("contest/")
    suspend fun getContests(
        @Query("format") format: String = "json",
        @Query("upcoming") upcoming: Boolean = true,
        @Query("format_time") formatTime: Boolean = true
    ): ContestResult
}