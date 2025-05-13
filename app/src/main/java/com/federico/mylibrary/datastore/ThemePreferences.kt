package com.federico.mylibrary.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.federico.mylibrary.ui.theme.AppThemeStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

object ThemePreferences {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    private val THEME_STYLE_KEY = stringPreferencesKey("theme_style")

    fun darkModeFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    fun themeStyleFlow(context: Context): Flow<AppThemeStyle> {
        return context.dataStore.data.map { prefs ->
            val name = prefs[THEME_STYLE_KEY] ?: AppThemeStyle.SYSTEM.name
            runCatching { AppThemeStyle.valueOf(name) }.getOrDefault(AppThemeStyle.SYSTEM)
        }
    }

    suspend fun setThemeStyle(context: Context, theme: AppThemeStyle) {
        context.dataStore.edit { prefs ->
            prefs[THEME_STYLE_KEY] = theme.name
        }
    }
}
