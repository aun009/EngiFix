package com.example.auth.presentation.inApp.profilescreen

import com.example.auth.data.local.DsaDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets ProfileScreen access DsaDao without a ViewModel (lifecycle-safe) */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DsaDaoEntryPoint {
    fun dsaDao(): DsaDao
}
