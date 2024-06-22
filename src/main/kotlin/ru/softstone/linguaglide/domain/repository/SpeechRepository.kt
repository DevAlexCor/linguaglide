package ru.softstone.linguaglide.domain.repository

import java.io.File

interface SpeechRepository {
    suspend fun getMp3(text: String): File
}