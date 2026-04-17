package com.example.auth.di

import android.content.Context
import androidx.room.Room
import com.example.auth.data.local.DsaDao
import com.example.auth.data.local.DsaDatabase
import com.example.auth.data.local.JobDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDsaDatabase(@ApplicationContext context: Context): DsaDatabase =
        Room.databaseBuilder(
            context,
            DsaDatabase::class.java,
            "engifix_dsa.db"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideDsaDao(db: DsaDatabase): DsaDao = db.dsaDao()

    @Provides
    @Singleton
    fun provideJobDao(db: DsaDatabase): JobDao = db.jobDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
