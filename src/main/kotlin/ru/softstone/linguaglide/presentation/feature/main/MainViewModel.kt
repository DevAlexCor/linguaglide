package ru.softstone.linguaglide.presentation.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.softstone.linguaglide.data.agent.EnglishTeacherAgent
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.domain.repository.SpeechRepository
import ru.softstone.linguaglide.domain.repository.TextRepository
import ru.softstone.linguaglide.presentation.AudioPlayer
import ru.softstone.linguaglide.presentation.core.CommandDelegate
import ru.softstone.linguaglide.presentation.core.HasCommand
import ru.softstone.linguaglide.presentation.core.HasState
import ru.softstone.linguaglide.presentation.core.StateDelegate
import ru.softstone.linguaglide.presentation.dialog.DialogDelegate
import ru.softstone.linguaglide.presentation.feature.main.model.MainCommand
import ru.softstone.linguaglide.presentation.feature.main.model.MainCommand.NavigateToPrepareText
import ru.softstone.linguaglide.presentation.feature.main.model.MainCommand.NavigateToSettings
import ru.softstone.linguaglide.presentation.feature.main.model.MainState
import ru.softstone.linguaglide.presentation.feature.main.model.PreviewItemState
import ru.softstone.linguaglide.presentation.feature.main.model.TextState

class MainViewModel(
    private val textRepository: TextRepository,
    private val speechRepository: SpeechRepository,
    private val englishTeacherAgent: EnglishTeacherAgent,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel(),
    HasState<MainState> by StateDelegate(MainState()),
    HasCommand<MainCommand> by CommandDelegate() {

    val dialogDelegate = DialogDelegate()

    private var selectedLine = 0
    private var textLines = emptyList<String>()

    private var playerJob: Job? = null

    init {
        viewModelScope.launch {
            if (hasToken()) {
                loadText()
            } else {
                sendCommand(NavigateToSettings)
            }
        }
    }

    fun onLineSelected(line: Int) {
        selectLine(line)
    }

    fun onTypedTextChange(textState: TextState) {
        val currentLineText = textLines[selectedLine]
        if (textState.typedText == currentLineText) {
            selectLine(
                line = selectedLine + 1,
                forceScroll = true
            )
            return
        }
        if (currentLineText.contains(textState.typedText) || textState.typedText.isEmpty()) {
            state = state.copy(textState = textState)
        } else {
            resetLine()
        }
    }

    fun onExplainClick() {
        if (state.textState.selectedRange != null) {
            // Get several lines around selected text in order to provide context for explanation
            // and mark selected text with <explain> tag
            val selectedRange = state.textState.selectedRange!!
            val selectedText = state.textState.text.substring(selectedRange.first, selectedRange.second)

            if (selectedText.isBlank()) {
                dialogDelegate.showDialog(
                    title = "Nothing to explain",
                    message = "Please select text to explain first.",
                    positiveButton = "OK",
                )
                return
            }

            val selectedLineIndex = selectedLine

            val startLineIndex = maxOf(selectedLineIndex - 3, 0)
            val endLineIndex = minOf(selectedLineIndex + 3, textLines.size - 1)
            val surroundingLines = textLines.subList(startLineIndex, endLineIndex + 1).toMutableList()

            val currentLineText = textLines[selectedLineIndex]
            val beforeSelectedText = currentLineText.substring(0, selectedRange.first)
            val afterSelectedText = currentLineText.substring(selectedRange.second)
            val markedLine = "$beforeSelectedText<explain>$selectedText</explain>$afterSelectedText"

            surroundingLines[selectedLineIndex - startLineIndex] = markedLine

            val textToExplain = surroundingLines.joinToString(" ")

            viewModelScope.launch {
                state = state.copy(chatLoading = true)
                try {
                    englishTeacherAgent.explain(textToExplain).collect {
                        state = state.copy(chatText = it)
                    }
                    playText(state.chatText)
                } catch (e: Exception) {
                    e.printStackTrace()
                    dialogDelegate.showDialog(
                        title = "Error",
                        message = e.message ?: "Unknown error",
                        positiveButton = "OK",
                    )
                }
                state = state.copy(chatLoading = false)
            }
        } else {
            dialogDelegate.showDialog(
                title = "Noting to explain",
                message = "Please select text to explain first.",
                positiveButton = "OK",
            )
        }
    }

    fun onPlayClick() {
        val textToPlay = if (state.textState.selectedRange != null) {
            val selectedRange = state.textState.selectedRange!!
            val selectedText = state.textState.text.substring(selectedRange.first, selectedRange.second)
            selectedText
        } else {
            textLines[selectedLine]
        }
        if (textToPlay.isBlank()) {
            dialogDelegate.showDialog(
                title = "Nothing to play",
                message = "Please select text to play first.",
                positiveButton = "OK",
            )
        } else {
            playText(textToPlay)
        }
    }

    fun onNewTextClick() {
        viewModelScope.launch {
            sendCommand(NavigateToPrepareText)
        }
    }

    private fun selectLine(line: Int, forceScroll: Boolean = false) {
        selectedLine = if (line >= textLines.size) {
            0
        } else {
            line
        }
        state = state.copy(
            textState = TextState(
                text = textLines[selectedLine],
                selectedRange = null
            ),
            previews = textLines
                .mapIndexed { index, text ->
                    PreviewItemState(
                        id = index,
                        isSelected = index == selectedLine,
                        text = text
                    )
                }
        )
        if (forceScroll) {
            viewModelScope.launch {
                sendCommand(MainCommand.ScrollToItem(selectedLine))
            }
        }
        if (textLines[selectedLine].isNotBlank()) {
            playText(textLines[selectedLine])
        }
        // preload next line
        viewModelScope.launch {
            val nextLine = textLines.getOrNull(selectedLine + 1)
            if (!nextLine.isNullOrBlank()) {
                speechRepository.getMp3(nextLine)
            }
        }

    }

    private fun loadText() {
        val text = textRepository.getText()
        if (text.isNotBlank()) {
            textLines = text.split("\n")
                .map { "$it " } // add space for natural transition between lines when typing
            selectLine(0)
        }
    }

    private fun resetLine() {
        state = state.copy(
            textState = TextState(
                text = textLines[selectedLine],
                typedText = "",
                selectedRange = null
            )
        )
    }

    private fun playText(text: String) {
        playerJob?.cancel()
        playerJob = viewModelScope.launch {
            state = state.copy(audioLoading = true)
            try {
                audioPlayer.playMp3(speechRepository.getMp3(text))
            } catch (e: Exception) {
                e.printStackTrace()
                dialogDelegate.showDialog(
                    title = "Error",
                    message = e.message ?: "Unknown error",
                    positiveButton = "OK",
                )
            }
            state = state.copy(audioLoading = false)
        }
    }

    private suspend fun hasToken(): Boolean {
        val token = settingsRepository.getToken()
        return !token.isNullOrBlank()
    }
}