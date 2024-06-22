package ru.softstone.linguaglide.presentation.feature.main.model

sealed interface MainCommand {
    data class ScrollToItem(val item: Int) : MainCommand
    data object NavigateToPrepareText : MainCommand
    data object NavigateToSettings : MainCommand
}