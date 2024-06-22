package ru.softstone.linguaglide.presentation.core

import kotlinx.coroutines.flow.Flow

interface HasCommand<T> {
    val commandFlow: Flow<T>
    suspend fun sendCommand(command: T)
}