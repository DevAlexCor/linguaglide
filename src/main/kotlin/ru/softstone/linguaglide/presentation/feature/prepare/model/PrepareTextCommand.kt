package ru.softstone.linguaglide.presentation.feature.prepare.model

sealed interface PrepareTextCommand {
    data object NavigateNext : PrepareTextCommand
}