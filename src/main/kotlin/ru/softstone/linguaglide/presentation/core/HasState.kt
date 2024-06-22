package ru.softstone.linguaglide.presentation.core

import kotlinx.coroutines.flow.StateFlow

interface HasState<T> {
    val stateFlow: StateFlow<T>
    var state: T
}