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

    @Query(
        """
        SELECT
            s.id,
            s.title,
            s.description,
            s.totalQuestions,
            s.category,
            s.imageUrl,
            CAST(COUNT(q.id) AS INTEGER) AS availableQuestions,
            CAST(COALESCE(SUM(CASE WHEN q.isCompleted = 1 THEN 1 ELSE 0 END), 0) AS INTEGER) AS completedQuestions
        FROM explore_sheets s
        LEFT JOIN dsa_questions q ON q.sheetId = s.id
        GROUP BY s.id
        ORDER BY s.title ASC
        """
    )
    fun getSheetsWithProgress(): Flow<List<SheetProgress>>

    @Query("SELECT * FROM explore_sheets WHERE id = :sheetId")
    fun getSheetById(sheetId: String): Flow<ExploreSheetEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSheetsIgnoringExisting(sheets: List<ExploreSheetEntity>)

    @Query(
        """
        UPDATE explore_sheets
        SET title = :title,
            description = :description,
            totalQuestions = :totalQuestions,
            category = :category,
            imageUrl = :imageUrl
        WHERE id = :id
        """
    )
    suspend fun updateSheetContent(
        id: String,
        title: String,
        description: String,
        totalQuestions: Int,
        category: String,
        imageUrl: String
    )

    @Query("SELECT COUNT(*) FROM explore_sheets")
    suspend fun getSheetCount(): Int

    // ─── Questions ─────────────────────────────────────────────────────────────

    @Query("SELECT * FROM dsa_questions WHERE sheetId = :sheetId ORDER BY topic ASC, id ASC")
    fun getQuestionsForSheet(sheetId: String): Flow<List<DsaQuestionEntity>>

    /** Returns distinct topic names ordered alphabetically for a given sheet */
    @Query("SELECT DISTINCT topic FROM dsa_questions WHERE sheetId = :sheetId ORDER BY topic ASC")
    fun getTopicsForSheet(sheetId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestionsIgnoringExisting(questions: List<DsaQuestionEntity>)

    @Query(
        """
        UPDATE dsa_questions
        SET sheetId = :sheetId,
            topic = :topic,
            title = :title,
            difficulty = :difficulty,
            problemUrl = :problemUrl
        WHERE id = :id
        """
    )
    suspend fun updateQuestionContent(
        id: String,
        sheetId: String,
        topic: String,
        title: String,
        difficulty: String,
        problemUrl: String
    )

    @Transaction
    suspend fun mergeSheetsAndQuestions(
        sheets: List<ExploreSheetEntity>,
        questions: List<DsaQuestionEntity>
    ) {
        insertSheetsIgnoringExisting(sheets)
        sheets.forEach { sheet ->
            updateSheetContent(
                id = sheet.id,
                title = sheet.title,
                description = sheet.description,
                totalQuestions = sheet.totalQuestions,
                category = sheet.category,
                imageUrl = sheet.imageUrl
            )
        }
        insertQuestionsIgnoringExisting(questions)
        questions.forEach { question ->
            updateQuestionContent(
                id = question.id,
                sheetId = question.sheetId,
                topic = question.topic,
                title = question.title,
                difficulty = question.difficulty,
                problemUrl = question.problemUrl
            )
        }
    }

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
