package com.example.auth.data.local

import com.example.auth.data.local.entity.DsaQuestionEntity
import com.example.auth.data.local.entity.ExploreSheetEntity
import com.example.auth.data.local.sync.SheetSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository wrapping DsaDao and SheetSyncManager.
 * All data content now comes from dsa_sheets.json (GitHub → Room).
 * User progress (isCompleted) lives exclusively in Room.
 */
@Singleton
class DsaRepository @Inject constructor(
    private val dao: DsaDao,
    private val syncManager: SheetSyncManager
) {
    // ─── Sheets ───────────────────────────────────────────────────────────────

    fun getAllSheets(): Flow<List<ExploreSheetEntity>> = dao.getAllSheets()

    fun getSheetsWithProgress(): Flow<List<SheetProgress>> = dao.getSheetsWithProgress()

    fun getSheetById(sheetId: String): Flow<ExploreSheetEntity?> = dao.getSheetById(sheetId)

    // ─── Questions ─────────────────────────────────────────────────────────────

    fun getQuestionsForSheet(sheetId: String): Flow<List<DsaQuestionEntity>> =
        dao.getQuestionsForSheet(sheetId)

    fun getTopicsForSheet(sheetId: String): Flow<List<String>> =
        dao.getTopicsForSheet(sheetId)

    fun getCompletedCount(sheetId: String): Flow<Int> = dao.getCompletedCount(sheetId)

    fun getTotalCount(sheetId: String): Flow<Int> = dao.getTotalCount(sheetId)

    suspend fun toggleCompletion(questionId: String, isCompleted: Boolean) =
        withContext(Dispatchers.IO) { dao.updateCompletion(questionId, isCompleted) }

    // ─── Sync ──────────────────────────────────────────────────────────────────

    /**
     * Called on app launch. Downloads latest JSON from GitHub (if newer),
     * otherwise falls back to the bundled assets/dsa_sheets.json.
     * Safe to call repeatedly — skips work if already on latest version.
     */
    suspend fun syncIfNeeded() = syncManager.syncIfNeeded()
}
