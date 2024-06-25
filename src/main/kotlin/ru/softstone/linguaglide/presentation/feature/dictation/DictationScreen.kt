package ru.softstone.linguaglide.presentation.feature.dictation

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
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationCommand
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationState
import ru.softstone.linguaglide.presentation.feature.dictation.model.PreviewItemState
import ru.softstone.linguaglide.presentation.feature.dictation.model.TextState

@Composable
fun DictationScreen(
    viewModel: DictationViewModel = viewModel { KoinPlatformTools.defaultContext().get().get<DictationViewModel>() },
    onNavigateToPrepareText: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val state: DictationState by viewModel.stateFlow.collectAsState()

    val listState = rememberLazyListState()

    viewModel.observeCommands {
        when (it) {
            is DictationCommand.ScrollToItem -> listState.animateScrollToItem(it.item)
            DictationCommand.NavigateToPrepareText -> onNavigateToPrepareText()
            DictationCommand.NavigateToSettings -> onNavigateToSettings()
        }
    }

    Box {
        DictationScreenContent(
            state = state,
            onTypedTextChange = viewModel::onTypedTextChange,
            listState = listState,
            onLineSelected = {
                viewModel.onLineSelected(it)
            },
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
private fun DictationScreenContent(
    state: DictationState,
    listState: LazyListState,
    onTypedTextChange: (TextState) -> Unit,
    onLineSelected: (Int) -> Unit,
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
    state: DictationState,
    listState: LazyListState,
    onTypedTextChange: (TextState) -> Unit,
    onLineSelected: (Int) -> Unit,
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
//            TextField(
//                value = state.textState.typedText,
//                textStyle = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace),
//                onValueChange = onTypedTextChange,
//                modifier = Modifier
//                    .wrapContentHeight()
//                    .weight(1f)
//                    .padding(16.dp)
//            )
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
                    imageVector = Icons.Default.Search,
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
    Card(
        border = if (preview.isSelected) {
            BorderStroke(1.dp, MaterialTheme.colors.onSurface)
        } else {
            null
        },
        modifier = modifier.clickable { onClick(preview.id) }
    ) {
        Text(
            text = preview.text,
            modifier = Modifier.padding(8.dp),
        )
    }
}


private fun String.spaceToDot(): String {
    return replace(" ", "·")
}

@Preview
@Composable
fun DictationScreenPreview() {
    DictationScreenContent(
        state = DictationState(
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