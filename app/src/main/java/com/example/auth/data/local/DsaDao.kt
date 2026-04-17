package com.example.auth.data.local

import androidx.room.*
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.example.auth.data.local.entity.ExploreSheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DsaDao {

    // ─── Sheets ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM explore_sheets ORDER BY title ASC")
    fun getAllSheets(): Flow<List<ExploreSheetEntity>>

    @Query("SELECT * FROM explore_sheets WHERE id = :sheetId")
    fun getSheetById(sheetId: String): Flow<ExploreSheetEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSheets(sheets: List<ExploreSheetEntity>)

    @Query("SELECT COUNT(*) FROM explore_sheets")
    suspend fun getSheetCount(): Int

    // ─── Questions ─────────────────────────────────────────────────────────────

    @Query("SELECT * FROM dsa_questions WHERE sheetId = :sheetId ORDER BY topic ASC, id ASC")
    fun getQuestionsForSheet(sheetId: String): Flow<List<DsaQuestionEntity>>

    /** Returns distinct topic names ordered alphabetically for a given sheet */
    @Query("SELECT DISTINCT topic FROM dsa_questions WHERE sheetId = :sheetId ORDER BY topic ASC")
    fun getTopicsForSheet(sheetId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<DsaQuestionEntity>)

    @Query("UPDATE dsa_questions SET isCompleted = :isCompleted WHERE id = :questionId")
    suspend fun updateCompletion(questionId: String, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM dsa_questions WHERE sheetId = :sheetId AND isCompleted = 1")
    fun getCompletedCount(sheetId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM dsa_questions WHERE sheetId = :sheetId")
    fun getTotalCount(sheetId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM dsa_questions WHERE isCompleted = 1")
    suspend fun getTotalSolvedGlobal(): Int

    @Query("SELECT COUNT(*) FROM dsa_questions")
    suspend fun getTotalQuestionsGlobal(): Int
}
