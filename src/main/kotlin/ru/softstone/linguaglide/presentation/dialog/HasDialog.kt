package ru.softstone.linguaglide.presentation.dialog

import ru.softstone.linguaglide.presentation.core.HasState

interface HasDialog : HasState<DialogState> {
    fun showDialog(
        title: String,
        message: String,
        positiveButton: String? = null,
        negativeButton: String? = null,
        onPositiveButtonClick: () -> Unit = {},
        onNegativeButtonClick: () -> Unit = {},
        onDismiss: () -> Unit = {}
    )

    fun onPositiveButtonClick()
    fun onNegativeButtonClick()
    fun onDismiss()
}