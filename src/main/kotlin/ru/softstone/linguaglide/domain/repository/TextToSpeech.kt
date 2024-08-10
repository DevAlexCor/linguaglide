package ru.softstone.linguaglide.domain.repository

interface TextToSpeech {
    suspend fun generateSpeech(text: String, speed: Double): ByteArray
}