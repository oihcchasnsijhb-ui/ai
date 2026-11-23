package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.ai.AiEngine
import com.example.data.ai.JarvisLanguage
import com.example.data.ai.JarvisTone
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.jarvisDataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_preferences")

data class JarvisPreferences(
    val memoryEnabled: Boolean = true,
    val preferredLanguage: JarvisLanguage = JarvisLanguage.AUTO,
    val personalityTone: JarvisTone = JarvisTone.COMPANION,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val selectedEngine: AiEngine = AiEngine.GEMINI
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val PERSONALITY_TONE = stringPreferencesKey("personality_tone")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_ENGINE = stringPreferencesKey("selected_engine")
    }

    val preferencesFlow: Flow<JarvisPreferences> = context.jarvisDataStore.data.map { prefs ->
        JarvisPreferences(
            memoryEnabled = prefs[Keys.MEMORY_ENABLED] ?: true,
            preferredLanguage = prefs[Keys.PREFERRED_LANGUAGE]?.let { name ->
                JarvisLanguage.entries.firstOrNull { it.name == name }
            } ?: JarvisLanguage.AUTO,
            personalityTone = prefs[Keys.PERSONALITY_TONE]?.let { name ->
                JarvisTone.entries.firstOrNull { it.name == name }
            } ?: JarvisTone.COMPANION,
            themeMode = prefs[Keys.THEME_MODE]?.let { name ->
                ThemeMode.entries.firstOrNull { it.name == name }
            } ?: ThemeMode.DARK,
            selectedEngine = prefs[Keys.SELECTED_ENGINE]?.let { name ->
                AiEngine.entries.firstOrNull { it.name == name }
            } ?: AiEngine.GEMINI
        )
    }

    suspend fun setMemoryEnabled(enabled: Boolean) {
        context.jarvisDataStore.edit { it[Keys.MEMORY_ENABLED] = enabled }
    }

    suspend fun setPreferredLanguage(language: JarvisLanguage) {
        context.jarvisDataStore.edit { it[Keys.PREFERRED_LANGUAGE] = language.name }
    }

    suspend fun setPersonalityTone(tone: JarvisTone) {
        context.jarvisDataStore.edit { it[Keys.PERSONALITY_TONE] = tone.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.jarvisDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSelectedEngine(engine: AiEngine) {
        context.jarvisDataStore.edit { it[Keys.SELECTED_ENGINE] = engine.name }
    }
}
