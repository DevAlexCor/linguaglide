package ru.softstone.linguaglide.presentation.feature.dictation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.difflib.text.DiffRowGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.softstone.linguaglide.data.agent.TeacherAgent
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.domain.repository.SpeechRepository
import ru.softstone.linguaglide.domain.repository.TextRepository
import ru.softstone.linguaglide.presentation.AudioPlayer
import ru.softstone.linguaglide.presentation.core.CommandDelegate
import ru.softstone.linguaglide.presentation.core.HasCommand
import ru.softstone.linguaglide.presentation.core.HasState
import ru.softstone.linguaglide.presentation.core.StateDelegate
import ru.softstone.linguaglide.presentation.dialog.DialogDelegate
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationCommand
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationCommand.NavigateToPrepareText
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationCommand.NavigateToSettings
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationState
import ru.softstone.linguaglide.presentation.feature.dictation.model.PreviewItemState

class DictationViewModel(
    private val textRepository: TextRepository,
    private val speechRepository: SpeechRepository,
    private val teacherAgent: TeacherAgent,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel(),
    HasState<DictationState> by StateDelegate(DictationState()),
    HasCommand<DictationCommand> by CommandDelegate() {

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

    fun onTypedTextChange(text: String) {
        state = state.copy(typedText = text)
    }

    fun onExplainClick() {
    }

    fun onPlayClick() {
        playText(textLines[selectedLine])
    }

    fun onTextDone() {
        viewModelScope.launch {
            if (textLines[selectedLine] == state.typedText) {
                selectLine(selectedLine + 1, true)
                state = state.copy(
                    markedText = "",
                    typedText = "",
                )
            } else {
                val diff = diff(textLines[selectedLine], state.typedText)
                state = state.copy(
                    markedText = diff,
                )
            }
        }
    }

    fun onNewTextClick() {
        viewModelScope.launch {
            sendCommand(NavigateToPrepareText)
        }
    }

    fun onSpeedChange(speed: Float) {
        state = state.copy(speed = speed)
    }

    private fun selectLine(line: Int, forceScroll: Boolean = false) {
        selectedLine = if (line >= textLines.size) {
            0
        } else {
            line
        }
        state = state.copy(
            previews = textLines
                .mapIndexed { index, text ->
                    PreviewItemState(
                        id = index,
                        isSelected = index == selectedLine,
                        text = text
                    )
                },
            markedText = textLines[selectedLine],
        )
        if (forceScroll) {
            viewModelScope.launch {
                sendCommand(DictationCommand.ScrollToItem(selectedLine))
            }
        }
        if (textLines[selectedLine].isNotBlank()) {
            playText(textLines[selectedLine])
        }
        // preload next line
        viewModelScope.launch {
            val nextLine = textLines.getOrNull(selectedLine + 1)
            if (!nextLine.isNullOrBlank()) {
                speechRepository.getMp3(nextLine, state.speed.toDouble())
            }
        }
        // explain the line
        viewModelScope.launch {
            state = state.copy(chatLoading = true)
            try {
                teacherAgent.explain(textLines[selectedLine]).collect {
                    state = state.copy(chatText = it)
                }
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
    }

    private fun loadText() {
        val text = textRepository.getText()
        if (text.isNotBlank()) {
            textLines = text.split("\n").map { it.trim() }
            selectLine(0)
        }
    }

    private fun playText(text: String) {
        playerJob?.cancel()
        playerJob = viewModelScope.launch {
            state = state.copy(audioLoading = true)
            try {
                audioPlayer.playMp3(speechRepository.getMp3(text, state.speed.toDouble()))
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


    private fun diff(original: String, modified: String): String {
        val generator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(false)
            .oldTag { _ -> "~~" }
            .newTag { _ -> "**" }
            .mergeOriginalRevised(true)
            .reportLinesUnchanged(true)
            .build()

        val rows = generator.generateDiffRows(
            listOf(modified),
            listOf(original),
        )
        return rows.first().oldLine
    }
}