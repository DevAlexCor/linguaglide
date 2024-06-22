package ru.softstone.linguaglide.presentation.feature.prepare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.softstone.linguaglide.data.agent.TextFormatterAgent
import ru.softstone.linguaglide.domain.repository.TextRepository
import ru.softstone.linguaglide.normalizeNewLines
import ru.softstone.linguaglide.presentation.core.CommandDelegate
import ru.softstone.linguaglide.presentation.core.HasCommand
import ru.softstone.linguaglide.presentation.core.HasState
import ru.softstone.linguaglide.presentation.core.StateDelegate
import ru.softstone.linguaglide.presentation.dialog.DialogDelegate
import ru.softstone.linguaglide.presentation.feature.prepare.model.PrepareTextCommand
import ru.softstone.linguaglide.presentation.feature.prepare.model.PrepareTextState
import ru.softstone.linguaglide.removeNonQwertyChars
import ru.softstone.linguaglide.replaceNonKeyboardChars

class PrepareTextViewModel(
    private val textRepository: TextRepository,
    private val textFormatterAgent: TextFormatterAgent,
) : ViewModel(),
    HasState<PrepareTextState> by StateDelegate(initialState),
    HasCommand<PrepareTextCommand> by CommandDelegate() {

    val dialogDelegate = DialogDelegate()

    companion object {
        private const val MAX_LENGTH = 10000
        private val initialState = PrepareTextState(
            maxLength = MAX_LENGTH,
        )
    }


    fun onTypedTextChange(text: String) {
        state = state.copy(
            text = text.take(MAX_LENGTH),
            textLength = text.length,
        )
    }

    fun onNext() {
        viewModelScope.launch {
            val text = state.text
                .normalizeNewLines()
                .replaceNonKeyboardChars()
                .removeNonQwertyChars()

            textRepository.setText(text)
            sendCommand(PrepareTextCommand.NavigateNext)
        }
    }

    fun onFormatText() {
        viewModelScope.launch {
            processText(state.text)
        }
    }

    private suspend fun processText(text: String) {
        state = state.copy(loading = true)

        try {
            textFormatterAgent.prepareTextForTyping(text).collect { processedText ->
                state = state.copy(text = processedText)
            }
        } catch (e: Exception) {
            dialogDelegate.showDialog(
                title = "Error",
                message = "${e.message}",
                positiveButton = "OK",
            )
        }
        state = state.copy(loading = false)
    }
}
