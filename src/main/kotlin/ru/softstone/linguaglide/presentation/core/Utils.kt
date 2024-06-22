package ru.softstone.linguaglide.presentation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <T> HasCommand<T>.observeCommands(onCommand: suspend (T) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            commandFlow.collect { command ->
                onCommand(command)
            }
        }
    }
}