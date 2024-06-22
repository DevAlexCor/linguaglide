package ru.softstone.linguaglide.presentation.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class CommandDelegate<T> : HasCommand<T> {
    private val _commandFlow = MutableSharedFlow<T>()
    override val commandFlow: Flow<T> get() = _commandFlow

    override suspend fun sendCommand(command: T) {
        _commandFlow.emit(command)
    }
}