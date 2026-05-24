package com.example.auth.data.local.sync

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.auth.data.local.DsaDao
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.example.auth.data.local.entity.ExploreSheetEntity
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

// ── DataStore extension ────────────────────────────────────────────────────────
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sheet_sync")
private val KEY_SYNCED_VERSION = intPreferencesKey("synced_version")

private const val TAG = "SheetSyncManager"

/**
 * ──────────────────────────────────────────────────────────────────────────────
 * SheetSyncManager
 *
 * Sync strategy:
 *  1. Read local synced_version from DataStore (0 = never synced)
 *  2. Try downloading dsa_sheets.json from GitHub Raw URL
 *  3. If download succeeds AND remote version > local → upsert into Room
 *  4. If download fails (no internet) → fall back to assets/dsa_sheets.json
 *  5. isCompleted data is NEVER touched — content is updated in place
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Singleton
class SheetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DsaDao,
    private val gson: Gson
) {
    // ── DATA SOURCE ────────────────────────────────────────────────────────────
    private val remoteUrl = "https://raw.githubusercontent.com/aun009/engifix-data/main/dsa_sheets.json"
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Call this on app launch. Returns when sync is complete (or skipped).
     * Safe to call multiple times — skips if already on latest version.
     */
    suspend fun syncIfNeeded() = withContext(Dispatchers.IO) {
        val localVersion = getLocalVersion()
        val sheetCount = dao.getSheetCount()

        Log.d(TAG, "Local version: $localVersion | DB sheets: $sheetCount")

        if (sheetCount == 0) {
            Log.d(TAG, "DB empty — loading bundled sheets before remote refresh")
            loadFromAssets()
        }

        // Try remote after the bundle is available so the UI is never blocked by the network.
        val remoteJson = tryDownload(remoteUrl)

        if (remoteJson != null) {
            val parsed = tryParse(remoteJson)
            // If we have a newer version OR the database is completely empty (due to migration wipe)
            if (parsed != null && (parsed.version > localVersion || sheetCount == 0)) {
                Log.d(TAG, "Remote version ${parsed.version} > local $localVersion (or DB empty) — syncing")
                upsertToRoom(parsed)
                saveLocalVersion(parsed.version)
                return@withContext
            } else if (parsed != null) {
                Log.d(TAG, "Already on version ${parsed.version} — skipping")
                return@withContext
            }
        }

        // Fallback: assets (always works, offline)
        // Load from assets if we have no remote data AND (never synced before OR DB is wiped)
        if (localVersion == 0 || dao.getSheetCount() == 0) {
            Log.d(TAG, "No internet / DB empty — loading from assets bundle")
            loadFromAssets()
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun tryDownload(url: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8_000
            conn.readTimeout    = 15_000
            conn.requestMethod  = "GET"
            conn.useCaches = false
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Download failed: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun tryParse(json: String): SheetJsonFile? {
        return try {
            gson.fromJson(json, SheetJsonFile::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            null
        }
    }

    private suspend fun loadFromAssets() {
        try {
            val json = context.assets.open("dsa_sheets.json").bufferedReader().readText()
            val parsed = tryParse(json) ?: return
            Log.d(TAG, "Loaded ${parsed.sheets.orEmpty().size} sheets from assets")
            upsertToRoom(parsed)
            saveLocalVersion(parsed.version)
        } catch (e: Exception) {
            Log.e(TAG, "Assets load failed: ${e.message}")
        }
    }

    /**
     * Merges sheets and questions without replacing rows.
     * This means:
     *   - New sheets/questions are added.
     *   - Existing titles, topics, difficulty and URLs are repaired.
     *   - Existing questions keep their isCompleted value untouched.
     */
    private suspend fun upsertToRoom(file: SheetJsonFile) {
        val validSheets = file.sheets.orEmpty()
            .filter { it.id.orEmpty().isNotBlank() && it.title.orEmpty().isNotBlank() }
            .distinctBy { it.id.orEmpty().trim() }

        val sheets = validSheets.map { s ->
            ExploreSheetEntity(
                id             = s.id.orEmpty().trim(),
                title          = s.title.orEmpty().trim(),
                description    = s.description.orEmpty().trim(),
                totalQuestions = s.questions.orEmpty().size.takeIf { it > 0 } ?: s.totalQuestions.coerceAtLeast(0),
                category       = s.category.orEmpty().trim().ifBlank { "DSA" }
            )
        }
        val questions = validSheets.flatMap { s ->
            s.questions.orEmpty()
                .filter { q -> q.id.orEmpty().isNotBlank() && q.title.orEmpty().isNotBlank() }
                .distinctBy { q -> q.id.orEmpty().trim() }
                .map { q ->
                    DsaQuestionEntity(
                        id          = q.id.orEmpty().trim(),
                        sheetId     = s.id.orEmpty().trim(),
                        topic       = q.topic.orEmpty().trim().ifBlank { "General" },
                        title       = q.title.orEmpty().trim(),
                        difficulty  = q.difficulty.orEmpty().trim().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }.ifBlank { "Medium" },
                        problemUrl  = q.problemUrl.orEmpty().trim()
                        // isCompleted defaults to false for new questions only
                    )
                }
        }

        dao.mergeSheetsAndQuestions(sheets, questions)
        Log.d(TAG, "Upserted ${sheets.size} sheets, ${questions.size} questions into Room")
    }

    private suspend fun getLocalVersion(): Int =
        context.dataStore.data.map { it[KEY_SYNCED_VERSION] ?: 0 }.first()

    private suspend fun saveLocalVersion(version: Int) {
        context.dataStore.edit { it[KEY_SYNCED_VERSION] = version }
    }
}
