package ru.softstone.linguaglide.presentation.feature.dictation.model

import androidx.compose.runtime.Immutable

@Immutable
data class DictationState(
    val previews: List<PreviewItemState> = emptyList(),
    val textState: TextState = TextState(),
    val chatText: String = "",
    val audioLoading: Boolean = false,
    val chatLoading: Boolean = false,
)

@Immutable
data class TextState(
    val text: String = "",
    val typedText: String = "",
    val selectedRange: Pair<Int, Int>? = null,
)

@Immutable
data class PreviewItemState(
    val id: Int,
    val isSelected: Boolean = false,
    val text: String = "",
)