package com.example.auth.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.example.auth.data.local.entity.ExploreSheetEntity
import com.example.auth.data.local.entity.JobEntity

@Database(
    entities = [
        ExploreSheetEntity::class,
        DsaQuestionEntity::class,
        JobEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class DsaDatabase : RoomDatabase() {
    abstract fun dsaDao(): DsaDao
    abstract fun jobDao(): JobDao
}
