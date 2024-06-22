package ru.softstone.linguaglide.presentation.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StateDelegate<T>(initialState: T) : HasState<T> {
    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<T> get() = _stateFlow

    override var state: T
        get() = _stateFlow.value
        set(value) {
            _stateFlow.value = value
        }
}