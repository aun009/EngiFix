package com.example.auth.di

import com.example.auth.presentation.features.contest.ClistApi
import com.example.auth.presentation.features.contest.ContestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContestModule {

    @Provides
    @Singleton
    fun provideClistApi(retrofit: Retrofit): ClistApi {
        return retrofit.create(ClistApi::class.java)
    }

    @Provides
    @Singleton
    fun provideContestRepository(api: ClistApi): ContestRepository {
        return ContestRepository(api)
    }
}
