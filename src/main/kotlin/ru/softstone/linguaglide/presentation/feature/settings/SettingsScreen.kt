package ru.softstone.linguaglide.presentation.feature.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.mp.KoinPlatformTools
import ru.softstone.linguaglide.presentation.core.observeCommands
import ru.softstone.linguaglide.presentation.dialog.DialogOverlay
import ru.softstone.linguaglide.presentation.feature.settings.model.SettingsCommand
import ru.softstone.linguaglide.presentation.feature.settings.model.SettingsState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel {
        KoinPlatformTools.defaultContext().get().get<SettingsViewModel>()
    },
    onNavigateBack: () -> Unit
) {
    val state: SettingsState by viewModel.stateFlow.collectAsState()

    viewModel.observeCommands { command ->
        when (command) {
            is SettingsCommand.NavigateBack -> onNavigateBack()
        }
    }

    Box {
        SettingsScreenContent(
            state = state,
            onDone = viewModel::onDone,
            onTokenChange = viewModel::onTokenChange,
            onTeacherPromptChange = viewModel::onTeacherPromptChange,
            onFormatterPromptChange = viewModel::onFormatterPromptChange,
        )
        DialogOverlay(
            controller = viewModel.dialogDelegate,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    onDone: () -> Unit,
    onTokenChange: (String) -> Unit,
    onTeacherPromptChange: (String) -> Unit,
    onFormatterPromptChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.h5,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
            ) {
                OutlinedTextField(
                    value = state.token,
                    onValueChange = onTokenChange,
                    label = { Text("OpenAI API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.teacherPrompt,
                    onValueChange = onTeacherPromptChange,
                    label = { Text("Teacher Prompt") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.textFormatterPrompt,
                    onValueChange = onFormatterPromptChange,
                    label = { Text("Text Formatter Prompt") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDone,
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
    SettingsScreenContent(
        state = SettingsState(),
        onDone = {},
        onTokenChange = {},
        onTeacherPromptChange = {},
        onFormatterPromptChange = {},
    )
}