package ru.softstone.linguaglide.domain.repository

interface SettingsRepository {
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)

    suspend fun getEnglishTeacherPrompt(): String
    suspend fun saveEnglishTeacherPrompt(prompt: String)

    suspend fun getTextFormatterPrompt(): String
    suspend fun saveTextFormatterPrompt(prompt: String)
}