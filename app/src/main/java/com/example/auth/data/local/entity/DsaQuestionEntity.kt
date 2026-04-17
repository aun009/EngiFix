package com.example.auth.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dsa_questions",
    foreignKeys = [
        ForeignKey(
            entity = ExploreSheetEntity::class,
            parentColumns = ["id"],
            childColumns = ["sheetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sheetId"), Index("topic")]
)
data class DsaQuestionEntity(
    @PrimaryKey val id: String,
    val sheetId: String,
    val topic: String,            // e.g. "Arrays & Hashing", "Two Pointers"
    val title: String,
    val difficulty: String,       // "Easy" | "Medium" | "Hard"
    val problemUrl: String,
    val isCompleted: Boolean = false
)
