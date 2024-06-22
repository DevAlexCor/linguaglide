package ru.softstone.linguaglide.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import ru.softstone.linguaglide.data.agent.EnglishTeacherAgent
import ru.softstone.linguaglide.data.agent.TextFormatterAgent
import ru.softstone.linguaglide.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_ENGLISH_TEACHER_PROMPT = stringPreferencesKey("english_teacher_prompt")
        private val KEY_TEXT_FORMATTER_PROMPT = stringPreferencesKey("text_formatter_prompt")
    }

    override suspend fun getToken(): String? {
        return dataStore.data.first()[KEY_TOKEN]
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    override suspend fun getEnglishTeacherPrompt(): String {
        return dataStore.data.first()[KEY_ENGLISH_TEACHER_PROMPT]?.ifBlank { null }
            ?: EnglishTeacherAgent.DEFAULT_PROMPT
    }

    override suspend fun saveEnglishTeacherPrompt(prompt: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ENGLISH_TEACHER_PROMPT] = prompt
        }
    }

    override suspend fun getTextFormatterPrompt(): String {
        return dataStore.data.first()[KEY_TEXT_FORMATTER_PROMPT]?.ifBlank { null } ?: TextFormatterAgent.DEFAULT_PROMPT
    }

    override suspend fun saveTextFormatterPrompt(prompt: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TEXT_FORMATTER_PROMPT] = prompt
        }
    }
}