package com.example.auth.presentation.features.contest

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ContestEntryPoint {
    fun getContestRepository(): ContestRepository
}
