package ru.softstone.linguaglide.data.agent

import kotlinx.coroutines.flow.Flow

interface CompletionAgent {
    suspend fun complete(prompt: String, message: String, temperature: Double = 1.0): Flow<String>
}