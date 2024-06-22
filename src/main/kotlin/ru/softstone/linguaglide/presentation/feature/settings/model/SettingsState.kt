package ru.softstone.linguaglide.presentation.feature.settings.model

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val token: String = "",
    val teacherPrompt: String = "",
    val textFormatterPrompt: String = "",
)