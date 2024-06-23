package ru.softstone.linguaglide.presentation.feature.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.koin.mp.KoinPlatformTools
import ru.softstone.linguaglide.presentation.core.observeCommands
import ru.softstone.linguaglide.presentation.dialog.DialogOverlay
import ru.softstone.linguaglide.presentation.feature.main.model.MainCommand
import ru.softstone.linguaglide.presentation.feature.main.model.MainState
import ru.softstone.linguaglide.presentation.feature.main.model.PreviewItemState
import ru.softstone.linguaglide.presentation.feature.main.model.TextState

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel { KoinPlatformTools.defaultContext().get().get<MainViewModel>() },
    onNavigateToPrepareText: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val state: MainState by viewModel.stateFlow.collectAsState()

    val listState = rememberLazyListState()

    viewModel.observeCommands {
        when (it) {
            is MainCommand.ScrollToItem -> listState.animateScrollToItem(it.item)
            MainCommand.NavigateToPrepareText -> onNavigateToPrepareText()
            MainCommand.NavigateToSettings -> onNavigateToSettings()
        }
    }

    Box {
        MainScreenContent(
            state = state,
            onTypedTextChange = viewModel::onTypedTextChange,
            listState = listState,
            onLineSelected = {
                viewModel.onLineSelected(it)
            },
            onShowClick = viewModel::onShowClick,
            onPlayClick = viewModel::onPlayClick,
            onExplainClick = viewModel::onExplainClick,
            onNewTextClick = viewModel::onNewTextClick,
            onSettingsClick = onNavigateToSettings,
        )
        DialogOverlay(
            controller = viewModel.dialogDelegate,
            modifier = Modifier.fillMaxSize()
        )
    }

}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun MainScreenContent(
    state: MainState,
    listState: LazyListState,
    onTypedTextChange: (TextState) -> Unit,
    onLineSelected: (Int) -> Unit,
    onShowClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onExplainClick: () -> Unit = {},
    onNewTextClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        HorizontalSplitPane(
            splitPaneState = rememberSplitPaneState(initialPositionPercentage = 1f),

            ) {
            first(360.dp) {
                FirstPanel(
                    state = state,
                    listState = listState,
                    onTypedTextChange = onTypedTextChange,
                    onLineSelected = onLineSelected,
                    onShowClick = onShowClick,
                    onPlayClick = onPlayClick,
                    onExplainClick = onExplainClick,
                    onNewTextClick = onNewTextClick,
                )
            }
            second(360.dp) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Info",
                            modifier = Modifier.padding(16.dp),
                            style = TextStyle(fontSize = 20.sp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                    SelectionContainer {
                        Text(
                            text = state.chatText,
                            modifier = Modifier.padding(16.dp),
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FirstPanel(
    state: MainState,
    listState: LazyListState,
    onTypedTextChange: (TextState) -> Unit,
    onLineSelected: (Int) -> Unit,
    onShowClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onExplainClick: () -> Unit = {},
    onNewTextClick: () -> Unit = {},
) {
    Column {
        OutlinedButton(
            onClick = onNewTextClick,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            Text("New Text")
        }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.previews, key = { it.id }) { previewState ->
                PreviewItem(
                    preview = previewState,
                    onClick = onLineSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.onBackground.copy(alpha = 0.1f))
        ) {
            TrainTextField(
                textState = state.textState,
                textStyle = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace),
                onValueChange = onTypedTextChange,
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(16.dp)
            )
            IconButton(
                onClick = onShowClick,
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "show"
                )
            }
            IconButton(
                onClick = onPlayClick,
                enabled = !state.audioLoading,
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play"
                )
            }
            IconButton(
                onClick = onExplainClick,
                enabled = !state.chatLoading,
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Play"
                )
            }
        }
    }
}

@Composable
private fun PreviewItem(
    modifier: Modifier,
    preview: PreviewItemState,
    onClick: (Int) -> Unit = {}
) {
    var isReviled by remember { mutableStateOf(false) }
    Card(
        border = if (preview.isSelected) {
            BorderStroke(1.dp, MaterialTheme.colors.onSurface)
        } else {
            null
        },
        modifier = modifier.clickable { onClick(preview.id) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isReviled) preview.text else preview.text.letterToSquare(),
                modifier = Modifier.padding(8.dp).weight(1f),
            )
            IconButton(
                onClick = {
                    isReviled = !isReviled
                },
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "show"
                )
            }
        }
    }
}

/**
 * Text field that shows text you have to type and marks the text you already typed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrainTextField(
    textState: TextState,
    textStyle: TextStyle = MaterialTheme.typography.body1.copy(fontFamily = FontFamily.Monospace),
    onValueChange: (TextState) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Clean up code of this function, implement proper state handling

    var currentState by remember { mutableStateOf(textState) }
    val focusRequester = remember { FocusRequester() }

    SideEffect {
        currentState = textState
    }

    BasicTextField(
        value = textState.typedText,
        onValueChange = { newValue ->
            onValueChange(textState.copy(typedText = newValue))
        },
        textStyle = textStyle,
        modifier = modifier
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
        ),
        decorationBox = { _ ->
            val typedPartStyle = textStyle.copy(color = MaterialTheme.colors.secondary)
            val remainingPartStyle = textStyle.copy(color = MaterialTheme.colors.onSurface)
            val selectedRangeStyle = textStyle.copy(
                background = MaterialTheme.colors.primary.copy(alpha = 0.3f)
            )

            var selectedRange by remember(textState) { mutableStateOf(textState.selectedRange) }

            val stringBuilder = AnnotatedString.Builder().apply {
                withStyle(typedPartStyle.toSpanStyle()) {
                    append(textState.typedText.spaceToDot())
                }
                withStyle(remainingPartStyle.toSpanStyle()) {
                    append(textState.text.substring(textState.typedText.length).spaceToDot().letterToSquare())
                }
            }

            selectedRange?.let { (start, end) ->
                stringBuilder.addStyle(
                    style = selectedRangeStyle.toSpanStyle(),
                    start = start,
                    end = end
                )
            }

            stringBuilder.toAnnotatedString().let { annotatedString ->
                var dragPosition by remember { mutableStateOf(Offset(0f, 0f)) }
                val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                val pressIndicator = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragPosition = offset
                                layoutResult.value?.let { layoutResult ->
                                    val selectedCharacterPosition = layoutResult.getOffsetForPosition(offset)
                                    selectedRange = selectedCharacterPosition to selectedCharacterPosition
                                }
                                onValueChange(currentState.copy(selectedRange = selectedRange))
                                focusRequester.requestFocus()
                            },
                            onDrag = { offset ->
                                dragPosition += offset
                                layoutResult.value?.let { layoutResult ->
                                    val selectedCharacterPosition = layoutResult.getOffsetForPosition(dragPosition)
                                    selectedRange = if (selectedCharacterPosition < (selectedRange?.first ?: 0)) {
                                        selectedCharacterPosition to (selectedRange?.second ?: 0)
                                    } else if (selectedCharacterPosition > (selectedRange?.second ?: 0)) {
                                        (selectedRange?.first ?: 0) to selectedCharacterPosition
                                    } else {
                                        selectedRange
                                    }
                                    onValueChange(currentState.copy(selectedRange = selectedRange))
                                }
                            },
                            onDragEnd = {
                                onValueChange(currentState.copy(selectedRange = selectedRange))
                            }
                        )
                    }
                    .pointerInput(onValueChange) {
                        detectTapGestures(
                            onPress = {
                                focusRequester.requestFocus()
                                selectedRange = null
                                onValueChange(currentState.copy(selectedRange = selectedRange))
                            },
                        )
                    }

                BasicText(
                    text = annotatedString,
                    modifier = modifier.then(pressIndicator),
                    style = textStyle,
                    onTextLayout = {
                        layoutResult.value = it
                    }
                )
            }
        }
    )
}

private fun String.spaceToDot(): String {
    return replace(" ", "·")
}

private fun String.letterToSquare(): String {
    return replace(Regex("[a-zA-Z]"), "■")
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreenContent(
        state = MainState(
            chatText = "Counter: 0",
            previews = listOf(
                PreviewItemState(id = 1, text = "Hello, World!"),
                PreviewItemState(id = 2, text = "Hello, World!"),
                PreviewItemState(id = 3, text = "Hello, World!"),
                PreviewItemState(id = 4, text = "Hello, World!"),
                PreviewItemState(id = 5, text = "Hello, World!"),
            ),
            textState = TextState(
                text = "Hello, World!",
                typedText = "Hello",
                selectedRange = 3 to 7
            )
        ),
        onTypedTextChange = {},
        onLineSelected = {},
        listState = rememberLazyListState()
    )
}