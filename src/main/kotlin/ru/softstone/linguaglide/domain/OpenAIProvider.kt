package ru.softstone.linguaglide.domain

import com.aallam.openai.client.OpenAI

interface OpenAIProvider {
    suspend fun getClient(): OpenAI
}