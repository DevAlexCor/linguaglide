package ru.softstone.linguaglide.domain.repository

interface TextRepository {
    fun setText(text: String)
    fun getText(): String
}