package ru.softstone.linguaglide.presentation.feature.dictation.model

sealed interface DictationCommand {
    data class ScrollToItem(val item: Int) : DictationCommand
    data object NavigateToPrepareText : DictationCommand
    data object NavigateToSettings : DictationCommand
}