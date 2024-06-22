package ru.softstone.linguaglide.presentation.dialog

data class DialogState(
    val title: String = "",
    val message: String = "",
    val positiveButton: String? = null,
    val negativeButton: String? = null,
    val isShowing: Boolean = false
)

