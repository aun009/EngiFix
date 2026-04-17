package com.example.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "explore_sheets")
data class ExploreSheetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val totalQuestions: Int,
    val category: String,       // e.g. "DSA", "System Design", "SQL"
    val imageUrl: String = ""
)
