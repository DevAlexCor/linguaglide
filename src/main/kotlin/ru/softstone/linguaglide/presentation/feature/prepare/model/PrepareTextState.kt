package ru.softstone.linguaglide.presentation.feature.prepare.model

import androidx.compose.runtime.Immutable

@Immutable
data class PrepareTextState(
    val text: String = "",
    val textLength: Int = 0,
    val maxLength: Int = 0,
    val loading: Boolean = false,
)