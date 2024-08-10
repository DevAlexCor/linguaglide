package ru.softstone.linguaglide.presentation.feature.dictation

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material.RichText
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.koin.mp.KoinPlatformTools
import ru.softstone.linguaglide.presentation.core.observeCommands
import ru.softstone.linguaglide.presentation.dialog.DialogOverlay
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationCommand
import ru.softstone.linguaglide.presentation.feature.dictation.model.DictationState
import ru.softstone.linguaglide.presentation.feature.dictation.model.PreviewItemState
import java.math.RoundingMode

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
            onTextDone = viewModel::onTextDone,
            onSpeedChange = viewModel::onSpeedChange
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
    onTypedTextChange: (String) -> Unit,
    onLineSelected: (Int) -> Unit,
    onPlayClick: () -> Unit = {},
    onExplainClick: () -> Unit = {},
    onNewTextClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTextDone: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        HorizontalSplitPane(
            splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.5f),

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
                    onTextDone = onTextDone,
                    onSpeedChange = onSpeedChange
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
                        RichText(
                            modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                        ) {
                            Markdown(content = state.chatText)
                        }
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
    onTypedTextChange: (String) -> Unit,
    onLineSelected: (Int) -> Unit,
    onPlayClick: () -> Unit = {},
    onExplainClick: () -> Unit = {},
    onNewTextClick: () -> Unit = {},
    onTextDone: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {}
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            OutlinedButton(
                onClick = onNewTextClick,
            ) {
                Text("New Text")
            }

            Text(
                text = "Speed",
                modifier = Modifier.padding(start = 16.dp),
                style = TextStyle(fontSize = 20.sp)
            )
            Slider(
                value = state.speed,
                onValueChange = onSpeedChange,
                valueRange = 0.25f..1f,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            Text(
                text = state.speed.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toString(),
                modifier = Modifier.padding(end = 16.dp),
                style = TextStyle(fontSize = 20.sp)
            )
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.onBackground.copy(alpha = 0.1f))
        ) {
            Text(
                text = state.markedText.mark(
                    tagStyles = listOf(
                        TagStyle(tag = "~~", style = TextStyle(color = Color.Red)),
                        TagStyle(tag = "**", style = TextStyle(color = Color.Green))
                    )
                ),
                modifier = Modifier.padding(16.dp),
                style = TextStyle(fontSize = 16.sp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.typedText,
                    textStyle = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace),
                    onValueChange = onTypedTextChange,
                    singleLine = true,
                    modifier = Modifier
                        .onKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp) {
                                    onTextDone()
                                }
                                true
                            } else {
                                false
                            }
                        }
                        .wrapContentHeight()
                        .weight(1f)
                        .padding(16.dp)
                )
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

fun String.mark(tagStyles: List<TagStyle>): AnnotatedString {
    var source = this
    var currentTagStyle: TagStyle? = null
    return buildAnnotatedString {
        while (true) {
            val tagIndex = source.indexOfAny(tagStyles.map { it.tag })
            val substring = if (tagIndex == -1) {
                source
            } else {
                source.substring(0, tagIndex)
            }
            val currentStyle = currentTagStyle?.style ?: TextStyle.Default
            withStyle(currentStyle.toSpanStyle()) {
                append(substring)
            }
            if (tagIndex == -1) {
                break
            }
            val tagStyle = tagStyles.find { source.startsWith(it.tag, tagIndex) }
            currentTagStyle = if (currentTagStyle != tagStyle) {
                tagStyle
            } else {
                null
            }
            source = source.substring(tagIndex + tagStyle!!.tag.length)
        }
    }
}

data class TagStyle(val tag: String, val style: TextStyle)

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
            typedText = "Hello, World!",
        ),
        onTypedTextChange = {},
        onLineSelected = {},
        listState = rememberLazyListState()
    )
}