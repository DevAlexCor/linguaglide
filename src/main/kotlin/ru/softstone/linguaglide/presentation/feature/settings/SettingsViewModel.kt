package ru.softstone.linguaglide.presentation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.presentation.core.CommandDelegate
import ru.softstone.linguaglide.presentation.core.HasCommand
import ru.softstone.linguaglide.presentation.core.HasState
import ru.softstone.linguaglide.presentation.core.StateDelegate
import ru.softstone.linguaglide.presentation.dialog.DialogDelegate
import ru.softstone.linguaglide.presentation.feature.settings.model.SettingsCommand
import ru.softstone.linguaglide.presentation.feature.settings.model.SettingsState

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val openAIProvider: OpenAIProvider,
) : ViewModel(),
    HasState<SettingsState> by StateDelegate(SettingsState()),
    HasCommand<SettingsCommand> by CommandDelegate() {

    val dialogDelegate = DialogDelegate()

    init {
        viewModelScope.launch {
            state = state.copy(
                token = settingsRepository.getToken() ?: "",
                teacherPrompt = settingsRepository.getEnglishTeacherPrompt(),
                textFormatterPrompt = settingsRepository.getTextFormatterPrompt()
            )
        }
    }

    fun onTokenChange(token: String) {
        state = state.copy(token = token)
    }

    fun onTeacherPromptChange(text: String) {
        state = state.copy(teacherPrompt = text)
    }

    fun onFormatterPromptChange(text: String) {
        state = state.copy(textFormatterPrompt = text)
    }

    fun onDone() {
        if (state.token.isBlank()) {
            dialogDelegate.showDialog(
                title = "Token is empty",
                message = "Please enter a token",
                positiveButton = "OK"
            )
        } else {
            viewModelScope.launch {
                settingsRepository.saveEnglishTeacherPrompt(state.teacherPrompt)
                settingsRepository.saveTextFormatterPrompt(state.textFormatterPrompt)
                try {
                    val savedToken = settingsRepository.getToken()
                    if (savedToken != state.token) {
                        settingsRepository.saveToken(state.token)
                        // Check if the token is valid by fetching models
                        openAIProvider.getClient().models()
                    }
                    sendCommand(SettingsCommand.NavigateBack)
                } catch (e: Exception) {
                    dialogDelegate.showDialog(
                        title = "Error",
                        message = e.message ?: "Unknown error",
                        positiveButton = "OK"
                    )
                }
            }
        }
    }
}
