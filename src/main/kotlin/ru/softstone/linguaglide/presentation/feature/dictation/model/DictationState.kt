package ru.softstone.linguaglide.presentation.feature.dictation.model

import androidx.compose.runtime.Immutable

@Immutable
data class DictationState(
    val previews: List<PreviewItemState> = emptyList(),
    val typedText: String = "",
    val chatText: String = "",
    val audioLoading: Boolean = false,
    val chatLoading: Boolean = false,
    val markedText: String = "",
    val speed: Float = 1f,
)

@Immutable
data class PreviewItemState(
    val id: Int,
    val isSelected: Boolean = false,
    val text: String = "",
)