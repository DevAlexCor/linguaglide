package ru.softstone.linguaglide.presentation.feature.prepare

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.mp.KoinPlatformTools
import ru.softstone.linguaglide.presentation.core.observeCommands
import ru.softstone.linguaglide.presentation.dialog.DialogOverlay
import ru.softstone.linguaglide.presentation.feature.prepare.model.PrepareTextCommand
import ru.softstone.linguaglide.presentation.feature.prepare.model.PrepareTextState

@Composable
fun PrepareTextScreen(
    viewModel: PrepareTextViewModel = viewModel {
        KoinPlatformTools.defaultContext().get().get<PrepareTextViewModel>()
    },
    onNavigateNext: () -> Unit
) {
    val state: PrepareTextState by viewModel.stateFlow.collectAsState()

    viewModel.observeCommands { command ->
        when (command) {
            is PrepareTextCommand.NavigateNext -> onNavigateNext()
        }
    }
    Box {
        PrepareTextScreenContent(
            state = state,
            onTypedTextChange = viewModel::onTypedTextChange,
            onNext = viewModel::onNext,
            onFormatText = viewModel::onFormatText,
        )
        DialogOverlay(
            controller = viewModel.dialogDelegate,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PrepareTextScreenContent(
    state: PrepareTextState,
    onTypedTextChange: (String) -> Unit,
    onNext: () -> Unit,
    onFormatText: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = state.text,
                placeholder = { Text("Insert your text here") },
                onValueChange = onTypedTextChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.textLength}/${state.maxLength}",
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                ) {
                    OutlinedButton(
                        onClick = onFormatText,
                        enabled = !state.loading,
                    ) {
                        Text("Format Text")
                    }
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center).size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onNext,
                    enabled = !state.loading,
                ) {
                    Text("Done")
                }
            }
        }
    }
}


@Preview
@Composable
fun AddTextScreenPreview() {
    PrepareTextScreenContent(
        state = PrepareTextState(
            text = "Hello"
        ),
        onTypedTextChange = {},
        onNext = {},
        onFormatText = {},
    )
}