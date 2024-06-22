package ru.softstone.linguaglide.presentation.feature.settings.model

sealed interface SettingsCommand {
    data object NavigateBack : SettingsCommand
}