package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kras_tv_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_STARTUP_SOUND = booleanPreferencesKey("startup_sound_enabled")
        val KEY_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
        val KEY_AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next_episode")
        val KEY_SUBTITLES_DEFAULT = booleanPreferencesKey("subtitles_default_enabled")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val code = prefs[KEY_LANGUAGE] ?: "ar" // Arabic is the default on first launch!
        AppLanguage.fromCode(code)
    }

    val startupSoundFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_STARTUP_SOUND] ?: true
    }

    val defaultAspectRatioFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ASPECT_RATIO] ?: "FIT"
    }

    val autoPlayNextFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTOPLAY_NEXT] ?: true
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun setStartupSound(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STARTUP_SOUND] = enabled
        }
    }

    suspend fun setDefaultAspectRatio(ratio: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ASPECT_RATIO] = ratio
        }
    }

    suspend fun setAutoPlayNext(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTOPLAY_NEXT] = enabled
        }
    }
}
