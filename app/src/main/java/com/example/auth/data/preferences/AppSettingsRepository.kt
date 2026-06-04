package com.example.auth.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appSettingsDataStore by preferencesDataStore(name = "engifix_app_settings")

class AppSettingsRepository(
    context: Context
) {
    private val appContext = context.applicationContext

    val isDarkTheme: Flow<Boolean> = appContext.appSettingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: false
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        appContext.appSettingsDataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}
